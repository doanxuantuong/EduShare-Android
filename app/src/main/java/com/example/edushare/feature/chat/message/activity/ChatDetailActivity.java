package com.example.edushare.feature.chat.message.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.PostSummaryResponse;
import com.example.edushare.feature.chat.conversation.dto.request.ConversationCreateRequest;
import com.example.edushare.feature.chat.message.adapter.MessageAdapter;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.MessageStatusResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ChatDetailActivity extends AppCompatActivity {

    // =======================================
    // PHẦN 1: Nút quay về + Tên người nhận tin nhắn
    private ImageButton btnBack;
    private TextView tvPartnerName;

//    private ImageView ivPartnerAvatar;

    // =======================================
    // PHẦN 2:  Tóm tắt thông tin của bài đăng
    private ConstraintLayout layoutProductHeader; // Sửa tên biến cho giống XML
    private ImageView imgPost;
    private TextView tvPostTitle, tvPostPrice;
    private ImageButton btnCloseProduct; // Thêm khai báo nút X
    private int postId;

    // =======================================
    // PHẦN 3:  Nội dung của đoạn hội thoại
    private RecyclerView rvMessages;
    private TextView tvMessage, tvTime;
    private ChatRepository chatRepository;
    private MessageAdapter messageAdapter;

    // Danh sách tin nhắn lấy từ API, Repository
    private List<MessageResponse> messageList;

    // DATA LẤY TỪ ACTIVITY TRUYỀN QUA
    private int conversationId;
    private String recipientName;
    private String recipientAvatarUrl;
    private String senderId;
    private String recipientId;

    // Biến cho Phân trang
    private int currentPage = 0;
    private final int PAGE_SIZE = 20;
    private boolean isLoading = false; // Đang call API thì không cho call đè
    private boolean isLastPage = false; // Hết tin cũ rồi thì không gọi API nữa

    // =======================================
    // PHẦN 4: Thanh soạn tin nhắn và gửi tin nhắn
    private EditText etMessage;

    private ImageButton btnSend;

    private StompClient stompClient;

    private Button btnViewProduct;


    // Hủy kết nối WebSocket khi Activity bị hủy
    // Tránh tốn RAM nếu WebSocket vẫn còn mở
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Call API lấy danh sách đoạn tin nhắn của conversation
        chatRepository = new ChatRepository(this);

        // Khởi tạo các View: Lấy các thành phần trong layout
        initViews();

        // Lấy data từ intent được ConversationAdapter gửi qua
        getIntentData();

        // Khởi tạo Adapter và RecyclerView
        setupRecyclerView();

        btnBack.setOnClickListener(l -> {
            finish();
        });

        btnSend.setOnClickListener(l -> {
            String message = etMessage.getText().toString();
            if (!message.isEmpty()) {

                if (conversationId == -1) {
                    ConversationCreateRequest conversationCreateRequest = new ConversationCreateRequest();
                    conversationCreateRequest.setSenderId(senderId);
                    conversationCreateRequest.setRecipientId(recipientId);
                    conversationCreateRequest.setPostId(postId);
                    conversationCreateRequest.setContent(message);
                    fetchSendFirstMessage(conversationCreateRequest);

                    etMessage.setText("");
                } else {
                    MessageRequest messageRequest = new MessageRequest();
                    messageRequest.setContent(message);
                    messageRequest.setConversationId(conversationId);
                    messageRequest.setSenderId(senderId);

                    fetchCreateMessageFromApi(conversationId, messageRequest);

                    etMessage.setText("");
                }
            }
        });

        if (postId != -1) {
            fetchPostSummaryFromApi(postId);
        }

        // Kết nối WebSocket
        connectWebSocket();
    }

    private void initViews() {
        // PHẦN 1: Nút quay về + Tên người nhận tin nhắn
        btnBack = findViewById(R.id.btnBack);
        tvPartnerName = findViewById(R.id.tvPartnerName);

        //ivPartnerAvatar = findViewById(R.id.ivPartnerAvatar);

        // PHẦN 3:  Thông tin cơ bản của Post
        layoutProductHeader = findViewById(R.id.layoutProductHeader);
        imgPost = findViewById(R.id.ivProductThumbnail);
        tvPostTitle = findViewById(R.id.tvProductTitle);
        tvPostPrice = findViewById(R.id.tvProductPrice);
        btnCloseProduct = findViewById(R.id.btnCloseProduct); // Ánh xạ nút X
        btnViewProduct = findViewById(R.id.btnViewProduct);

        btnViewProduct.setOnClickListener(view -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("POST_ID", postId);
            startActivity(intent);
        });

        // Xử lý nút "Dấu X"
        btnCloseProduct.setOnClickListener(v -> {
            layoutProductHeader.setVisibility(View.GONE);
        });

        // PHẦN 2:  Nội dung của đoạn hội thoại
        rvMessages = findViewById(R.id.rvMessages);

        // PHẦN 3: Thanh soạn tin nhắn và gửi tin nhắn
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
    }

    private void getIntentData() {
        // LẤY DATA ĐƯỢC TRUYỀN TỪ BÊN item_chat qua (bên ConversationActivity)
        Intent intent = getIntent();

        if (intent != null) {
            conversationId = intent.getIntExtra("CONVERSATION_ID", -1);
            senderId = UserSessionHelper.getUserId(this);
            recipientId = intent.getStringExtra("RECIPIENT_ID");
            recipientName = intent.getStringExtra("RECIPIENT_NAME");
            recipientAvatarUrl = intent.getStringExtra("RECIPIENT_AVATAR");
            postId = intent.getIntExtra("POST_ID", -1);
            checkExistingConversation(senderId, recipientId, postId);

            if (postId != -1) {
                // TRƯỜNG HỢP: ĐI TỪ SẢN PHẨM VÀO -> Hiện Header và set dữ liệu
                layoutProductHeader.setVisibility(View.VISIBLE);

                String title = getIntent().getStringExtra("POST_TITLE");
                String price = getIntent().getStringExtra("POST_PRICE");
                String imageUrl = getIntent().getStringExtra("POST_IMAGE_URL");

                tvPostTitle.setText(title);
                tvPostPrice.setText(price);

                // Load ảnh bằng Glide (Nhớ import thư viện)
                Glide.with(this).load(imageUrl).into(imgPost);

            } else {
                // TRƯỜNG HỢP: ĐI TỪ DANH SÁCH CHAT BÌNH THƯỜNG -> Giấu Header đi
                layoutProductHeader.setVisibility(View.GONE);
            }

            if (recipientName != null) {
                tvPartnerName.setText(recipientName);
            }
        }
    }

    private void setupRecyclerView() {
        // Khởi tạo danh sách tin nhắn
        messageList = new ArrayList<>();

        // Khởi tạo Adapter và gắn vào RecyclerView
        messageAdapter = new MessageAdapter(messageList, senderId, recipientAvatarUrl);
        rvMessages.setAdapter(messageAdapter);

        // Cấu hình LinearLayoutManager đảo ngược để hiên thị tin nhắn mới nhất ở dưới
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Bắt các item xếp chồng từ dưới lên
        layoutManager.setStackFromEnd(true);

        // Gắn layoutManager đã cấu hình vào RecyclerView
        rvMessages.setLayoutManager(layoutManager);

        // Bắt sự kiện vuốt lên trên cùng để load tin nhắn cũ
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    // Lấy vị trí của item đang hiển thị ở vị trí trên cùng của màn hình
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    // Nếu vị trí đó là 0 (nghĩa là người dùng đã cuộn chạm nóc trên cùng)
                    if (firstVisibleItem == 0) {
                        // Kiểm tra xem có đang load dở không, và đã hết trang chưa
                        if (!isLoading && !isLastPage) {
                            currentPage++;
                            fetchMessagesByConversationIdFromApi(currentPage);
                        }
                    }
                }
            }
        });

    }


    private void fetchMessagesByConversationIdFromApi(int page) {
        // Bên Server đang trả về theo thứ tự là DESC tức là mới nhất
        // Và chỉ lấy 20 tin nhắn
        try {
            isLoading = true;
            chatRepository.getMessages(conversationId, page, PAGE_SIZE, new OnActionCallback<PageResponse<MessageResponse>>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(PageResponse<MessageResponse> data) {
                    isLoading = false;
                    isLastPage = data.isLast();

                    List<MessageResponse> newMessages = data.getContent();

                    if (newMessages != null && !newMessages.isEmpty()) {
                        // Cái API cũ lấy theo ngày tạo ASC nhưng khi làm phân trang
                        // API đổi lại theo DESC ngày tạo (để lấy tin nhắn mới nhất)
                        // Do cách hiển thị cũ đang xếp theo ASC nên cần đảo lại
                        Collections.reverse(newMessages);
                        if (page == 0) {
                            messageList.clear();
                            messageList.addAll(newMessages);

                            messageAdapter.notifyDataSetChanged();

                            // Đánh dấu là đã đọc tin nhắn cuối cùng
                            markConversationAsRead();
                            // Kéo xuống vị trí cuối
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        } else {
                            // Chèn mảng tin cũ vào vị trí trên cùng (index 0) của danh sách hiện tại
                            messageList.addAll(0, newMessages);
                            // Báo cho Adapter biết có 1 cụm mới được nhét vào đầu
                            messageAdapter.notifyItemRangeInserted(0, newMessages.size());
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    isLoading = false;
                    Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchCreateMessageFromApi(int conversationId, MessageRequest messageRequest) {
        chatRepository.sendMessage(conversationId, messageRequest, new OnActionCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                addNewMessage(data);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPostSummaryFromApi(int postId) {
        chatRepository.getPostSummary(postId, new OnActionCallback<PostSummaryResponse>() {

            @Override
            public void onSuccess(PostSummaryResponse data) {

                tvPostTitle.setText(data.getTitle());

                if (data.getPrice() != null) {
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvPostPrice.setText(formatter.format(data.getPrice()) + "đ");
                } else {
                    tvPostPrice.setText("Liên hệ");
                }

                Glide.with(ChatDetailActivity.this)
                        .load(data.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(imgPost);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    private void markConversationAsRead() {
        chatRepository.markMessageAsRead(conversationId, senderId, recipientId, new OnActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {

            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatDetail", "Mark read failed: " + error);
            }
        });
    }

    private void fetchSendFirstMessage(ConversationCreateRequest conversationCreateRequest) {
        // Gọi API đặc biệt (Bạn nhớ thêm hàm sendFirstMessage bên ChatRepository nhé)
        chatRepository.sendFirstMessage(conversationCreateRequest, new OnActionCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                // Cập nhật lại ID phòng thật từ Server trả về
                conversationId = data.getConversationId();
                addNewMessage(data);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChatDetailActivity.this, "Lỗi tạo phòng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Khi có 1 tin nhắn mới được thêm vào list
    public void addNewMessage(MessageResponse newMessage) {
        //1. KIỂM TRA TRÙNG LẶP: Quét xem tin nhắn này đã tồn tại trên giao diện chưa
        for (MessageResponse msg : messageList) {
            if (msg.getId() == newMessage.getId()) {
                return; // Đã có rồi thì thoát ngay, không add thêm nữa!
            }
        }

        int oldSize = messageList.size();
        messageList.add(newMessage);

        if (oldSize > 0) {
            // 2. Cập nhật lại tin nhắn ngay trước đó (để nó ẩn thời gian/avatar đi vì không còn là tin cuối)
            messageAdapter.notifyItemChanged(oldSize - 1);
        }

        // 3. Thông báo thêm tin nhắn mới
        messageAdapter.notifyItemInserted(oldSize);

        // 4. Cuộn xuống cuối cùng
        rvMessages.scrollToPosition(oldSize);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLastMessageStatus() {
        boolean isUpdated = false;

        // Duyệt ngược từ dưới lên (vì thường chỉ có vài tin nhắn cuối là chưa xem)
        for (int i = messageList.size() - 1; i >= 0; i--) {
            MessageResponse messageResponse = messageList.get(i);

            boolean isRead = messageResponse.getRead();
            if (!isRead) {
                messageResponse.setRead(true);
                isUpdated = true;
            } else if (isRead) {
                break;
            }
        }

        if (isUpdated) {
            messageAdapter.notifyDataSetChanged();
        }
    }

    public void checkExistingConversation(String senderId, String recipientId, int postId) {
        chatRepository.checkExistingConversation(senderId, recipientId, postId, new OnActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                if (data != null && data > 0) {
                    conversationId = data; // Phòng thật
                    currentPage = 0;
                    fetchMessagesByConversationIdFromApi(currentPage);
                    Log.d("ChatDetail", "Conversation ID: " + conversationId);
                } else {
                    conversationId = -1; // Phòng ảo
                    Log.d("ChatDetail", "Conversation ID: " + conversationId);
                }
            }

            @Override
            public void onFailure(String error) {
                conversationId = -1; // Lỗi mạng thì cũng coi như phòng ảo luôn
            }
        });
    }

    @SuppressLint("CheckResult")
    private void connectWebSocket() {
        // 1. Tạo URL kết nối (Nhớ đổi 10.0.2.2 thành IP LAN nếu test trên điện thoại thật)
        // Phải có đuôi /websocket vì cổng thuần của Spring Boot yêu cầu thế
        String wsUrl = "ws://10.0.2.2:8080/ws-edushare/websocket";

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

        String myTopic = "/topic/messages/" + senderId;
        receiveNewMessage(myTopic);

        String destinationStatus = "/topic/receipts/" + senderId;
        receiveNewMessageStatus(destinationStatus);

        // 2. Lắng nghe trạng thái kết nối (Để debug xem có dính lỗi mạng không)
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WebSocket", "Connection opened");
                    break;
                case ERROR:
                    Log.d("WebSocket", "Lỗi kết nối", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("SOCKET", "Đã ngắt kết nối");
                    break;
            }
        }, throwable -> {
            Log.e("WebSocket", "Lifecycle subscribe error", throwable);
        });

        stompClient.connect();

    }

    @SuppressLint("CheckResult")
    private void receiveNewMessage(String myTopic) {
        stompClient.topic(myTopic).subscribe(stompMessage -> {
            try {
                // 1. IN RA CHUỖI JSON GỐC
                String jsonPayload = stompMessage.getPayload();
                Log.d("SOCKET_DATA", "Dữ liệu thô nhận được: " + jsonPayload);

                // 2. Ép kiểu JSON
                Gson gson = new Gson();
                MessageResponse newMessage = gson.fromJson(jsonPayload, MessageResponse.class);

                runOnUiThread(() -> {
                    // Nhận tin nhắn nếu nó khớp ID phòng, HOẶC nếu phòng hiện tại đang là phòng ảo (-1)
                    if (newMessage.getConversationId() == conversationId || conversationId == -1) {

                        // Nếu là phòng ảo mà nhận được tin đầu tiên, phải cập nhật lại ID phòng thật ngay!
                        if (conversationId == -1) {
                            conversationId = newMessage.getConversationId();
                        }

                        addNewMessage(newMessage);
                    }
                });
            } catch (Exception e) {
                // Nếu Gson đọc không được, nó chỉ báo lỗi ở đây
                Log.e("SOCKET_DATA", "Lỗi Parse JSON tin nhắn mới", e);
            }
        }, throwable -> {
            Log.e("WebSocket", "Lỗi đường truyền tin nhắn mới", throwable);
        });
    }

    @SuppressLint("CheckResult")
    private void receiveNewMessageStatus(String receiptTopic) {
        stompClient.topic(receiptTopic).subscribe(stompMessage -> {
            try {
                String jsonPayload = stompMessage.getPayload();
                Log.d("SOCKET_DATA", "Trạng thái nhận được: " + jsonPayload);

                Gson gson = new Gson();
                MessageStatusResponse newMessage = gson.fromJson(jsonPayload, MessageStatusResponse.class);

                runOnUiThread(() -> {
                    if (conversationId == newMessage.getConversationId() && newMessage.getRead()) {
                        updateLastMessageStatus();
                    }
                });
            } catch (Exception e) {
                Log.e("SOCKET_DATA", "Lỗi Parse JSON trạng thái", e);
            }
        }, throwable -> {
            Log.e("WebSocket", "Lỗi đường truyền trạng thái", throwable);
        });
    }

}
