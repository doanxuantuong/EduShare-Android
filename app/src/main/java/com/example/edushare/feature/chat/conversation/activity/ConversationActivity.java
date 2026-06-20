package com.example.edushare.feature.chat.conversation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.conversation.adapter.ConversationAdapter;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.notification.firebase.manager.FcmTokenManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

/*
 * Tưởng tượng bạn (file Activity) là một vị Giám đốc.
 * - RecyclerView là anh Quản đốc xưởng trực tiếp điều hành công việc.
 * - File Adapter chính là một "Cuốn sổ tay quy trình" mà bạn viết ra để hướng dẫn Quản đốc làm việc.
 * Trong cuốn sổ này có 2 trang quan trọng:
 *   + Trang 1: Cách tạo ra cái hộp (onCreateViewHolder).
 *   + Trang 2: Cách nhét dữ liệu vào hộp (onBindViewHolder).
 * - Khi bạn viết dòng code này trong Activity: rvChatList.setAdapter(adapter);
 *   + Ném cuốn sổ tay (adapter) cho anh Quản đốc (rvChatList)
 *   + Và bảo: "Làm việc đi, quy trình tôi ghi hết trong sổ rồi đấy!"
 * */
public class ConversationActivity extends AppCompatActivity {
    private RecyclerView rvChatList;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;
    private List<Conversation> originalConversationList;
    private ChatRepository chatRepository;
    private View footerLayout;
    private String currentUserId;
    private int currentPage = 0;
    private final int PAGE_SIZE = 8;
    private boolean isLoading = false; // Đang call API thì không cho call đè
    private boolean isLastPage = false; // Hết tin cũ rồi thì không gọi API nữa
    private boolean isFirstOpen = true;
    private StompClient stompClient;
    private EditText edtSearchChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Chỉ định giao diện hiển thị cho Activity
        setContentView(R.layout.activity_chat_list);

        //Khởi tạo sự kiện cho các View
        initViews();
        setupRecyclerView();

        loadSession();

        // Khởi tạo Repository
        chatRepository = new ChatRepository(this);

        // Gọi API lấy dữ liệu thực
        fetchConversationsFromApi(currentPage);

        connectWebSocket();

        setupSearch();

        FcmTokenManager fcmTokenManager = new FcmTokenManager(this);
        fcmTokenManager.getAndSaveFcmToken(currentUserId);
    }

    private void loadSession() {
        // Khởi tạo và thiết lập cụm điều hướng dùng chung
        currentUserId = UserSessionHelper.getUserId(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi màn hình này hiện lên (kể cả lúc từ phòng chat quay ra),
        // gọi lại API để lấy danh sách lastMessage mới nhất.
        if (currentUserId != null) {
            // Nếu là lần đầu tiên mở Activity (onCreate đã gọi API rồi)
            if (isFirstOpen) {
                isFirstOpen = false; // Đánh dấu lần sau quay lại không phải lần đầu nữa
            } else {
                currentPage = 0;
                isLastPage = false;
                fetchConversationsFromApi(currentPage);
            }
        }
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
        edtSearchChat = findViewById(R.id.etSearch);

        // 2. Ánh xạ thẻ <include> trong file xml (ID là footer_layout)
        footerLayout = findViewById(R.id.footer_layout);

        // 3. Kích hoạt bộ Helper: Truyền id của tab Chat (R.id.nav_chat) để nó biết đường làm sáng đèn icon Chat
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_chat);
    }

    private void setupRecyclerView() {
        // Tạo danh sách các đoạn hội thoại (conversationList)
        conversationList = new ArrayList<>();
        originalConversationList = new ArrayList<>();

        // Thêm dữ liệu qua bên Adapter cho Adapter load lên
        adapter = new ConversationAdapter(conversationList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        //Gắn Adapter vào RecyclerView để hiển thị danh sách
        rvChatList.setAdapter(adapter);

        rvChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                        int totalItemCount = conversationList.size();

                        // nếu item cuối đã chạm đáy
                        if (lastVisibleItem == conversationList.size() - 1) {
                            if (!isLoading && !isLastPage) {
                                currentPage++;
                                fetchConversationsFromApi(currentPage);
                            }
                        }
                    }
                }

            }

        });
    }

    private void fetchConversationsFromApi(int page) {
        isLoading = true;
        chatRepository.getConversations(currentUserId, page, PAGE_SIZE, new OnActionCallback<PageResponse<Conversation>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(PageResponse<Conversation> data) {

                // Kiểm tra xem có đang load dở không, và đã hết trang chưa
                isLoading = false;
                isLastPage = data.isLast();
                List<Conversation> newConversations = data.getContent();
                int sizeReceived = (newConversations != null) ? newConversations.size() : 0;

                if (newConversations != null && !newConversations.isEmpty()) {
                    if (page == 0) {
                        // Xóa danh sách cũ và thêm dữ liệu mới từ API
                        conversationList.clear();
                        conversationList.addAll(newConversations);
                        originalConversationList.addAll(newConversations);

                        // Thông báo cho adapter cập nhật giao diện
                        adapter.notifyDataSetChanged();
                    } else {
                        // TÌM VỊ TRÍ CUỐI CÙNG ĐỂ CHÈN
                        int insertIndex = conversationList.size();

                        // THÊM VÀO CUỐI DANH SÁCH
                        conversationList.addAll(newConversations);

                        // VẼ TỪ VỊ TRÍ CUỐI ĐÓ
                        adapter.notifyItemRangeInserted(insertIndex, newConversations.size());
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading = false;
                Toast.makeText(ConversationActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateConversationList(MessageResponse newMessage) {
        int existingIndex = -1;

        // 1. Quét xem đoạn chat này đã có sẵn trong danh sách hiển thị chưa
        for (int i = 0; i < conversationList.size(); i++) {
            if (conversationList.get(i).getId() == newMessage.getConversationId()) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            Conversation conversation = conversationList.get(existingIndex);
            conversation.setLastMessage(newMessage.getContent());
            conversation.setUpdatedAt(newMessage.getCreatedAt());
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);

            if (existingIndex != 0) {
                conversationList.remove(existingIndex);
                conversationList.add(0, conversation);
                adapter.notifyItemMoved(existingIndex, 0);
                adapter.notifyItemChanged(0);
            } else {
                // Đã ở sẵn trên đầu rồi thì chỉ cần vẽ lại vị trí 0
                adapter.notifyItemChanged(0);
            }

            // Đẩy RecyclerView cuộn mượt mà lên vị trí trên cùng
            rvChatList.smoothScrollToPosition(0);

        } else {
            currentPage = 0;
            isLastPage = false;
            fetchConversationsFromApi(currentPage);
        }
    }

    @SuppressLint("CheckResult")
    private void connectWebSocket() {
        String wsUrl = "ws://10.0.2.2:8080/ws-edushare/websocket";
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);
        stompClient.connect();

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WebSocket", "Connection opened");
                    subscribeChatTopic();
                    break;
                case ERROR:
                    Log.d("WebSocket", "Lỗi kết nối", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("SOCKET", "Đã ngắt kết nối");
                    break;
            }
        });
    }

    @SuppressLint("CheckResult")
    private void subscribeChatTopic() {
        String myTopic = "/topic/messages/" + currentUserId;
        stompClient.topic(myTopic).subscribe(stompMessage -> {
            String jsonPayload = stompMessage.getPayload();

            Gson gson = new Gson();
            MessageResponse newMessage = gson.fromJson(jsonPayload, MessageResponse.class);

            runOnUiThread(() -> {
                updateConversationList(newMessage);
            });
        }, throwable -> {
            Log.e("WebSocket", "Subscribe chat topic error", throwable);
        });
    }

    // Tìm kiếm đoạn chat dựa vào tên của người nhận
    private void findConversationsByReceiverName(String receiverName) {
        conversationList.clear();

        if (receiverName == null || receiverName.trim().isEmpty()) {
            conversationList.addAll(originalConversationList);
        } else {
            String keyword = receiverName.trim().toLowerCase();

            for (Conversation conversation : originalConversationList) {
                String name = conversation.getRecipientName();

                if (name != null && name.toLowerCase().contains(keyword)) {
                    conversationList.add(conversation);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    //Code setup cho việc tìm kiếm
    private void setupSearch() {
        edtSearchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findConversationsByReceiverName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
