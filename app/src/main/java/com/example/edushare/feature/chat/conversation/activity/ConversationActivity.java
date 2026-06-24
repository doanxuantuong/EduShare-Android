package com.example.edushare.feature.chat.conversation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.common.helper.WebSocketManager;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.conversation.adapter.ConversationAdapter;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.conversation.view_model.ConversationViewModel;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.common.firebase.user_fcm_token.FcmTokenManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
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
    private View footerLayout;
    private EditText edtSearchChat;
    private String currentUserId;
    private boolean isFirstOpen = true;

    private ConversationViewModel viewModel;
    private Disposable topicDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId = UserSessionHelper.getUserId(this);

        initViews();
        initViewModel();
        initRecyclerView();
        setupSearch();

        // Lần đầu mở app -> Gọi load data
        viewModel.loadConversations(currentUserId, true);

        connectWebSocket();

        FcmTokenManager fcmTokenManager = new FcmTokenManager(this);
        fcmTokenManager.getAndSaveFcmToken(currentUserId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            if (isFirstOpen) {
                isFirstOpen = false;
            } else {
                // Đi đâu đó quay lại thì Refresh danh sách
                viewModel.loadConversations(currentUserId, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy lắng nghe tin nhắn để giải phóng hoàn toàn RAM cho màn hình
        if (topicDisposable != null && !topicDisposable.isDisposed()) {
            topicDisposable.dispose();
        }
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
        edtSearchChat = findViewById(R.id.etSearch);
        footerLayout = findViewById(R.id.footer_layout);
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_chat);
    }

    private void initViewModel() {
        ChatRepository repository = new ChatRepository(this);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new ConversationViewModel(repository))
        ).get(ConversationViewModel.class);

        // 1. Lắng nghe Danh sách thay đổi
        viewModel.getConversationList().observe(this, conversations -> {
            // Adapter bây giờ updateData(List)
            adapter.updateData(conversations);
        });

        // 2. Lắng nghe Lỗi
        viewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void initRecyclerView() {
        adapter = new ConversationAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChatList.setLayoutManager(layoutManager);
        rvChatList.setAdapter(adapter);

        rvChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Kiểm tra xem người dùng có đang vuốt màn hình từ dưới lên
                if (dy > 0) {
                    // Số lượng đoạn conversation đang hiển thị
                    int visibleItemCount = layoutManager.getChildCount();
                    // Số lượng conversation đã bị cuộn qua và khuất lên phía trên
                    int totalItemCount = layoutManager.getItemCount();
                    // Tổng số lượng conversation mà Adapter đang cầm trong tay
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    // Lấy số cái đang nhìn thấy (6) + số cái đã khuất ở trên (14).
                    // Nếu tổng này lớn hơn hoặc bằng tổng số (20),
                    // nghĩa là chạm tới đáy của danh sách hiện tại.
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        viewModel.loadConversations(currentUserId, false);
                    }
                }
            }
        });
    }

    private void setupSearch() {
        edtSearchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString()); // Chuyền keyword qua cho ViewModel xử
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @SuppressLint("CheckResult")
    private void connectWebSocket() {

        WebSocketManager.getInstance().connectWebSocket();

        StompClient sharedClient = WebSocketManager.getInstance().getStompClient();

        topicDisposable = sharedClient.topic("/topic/messages/" + currentUserId).subscribe(stompMessage -> {
            try {
                String jsonPayload = stompMessage.getPayload();
                Log.d("SOCKET_DATA", "ChatList nhận JSON: " + jsonPayload);

                MessageResponse newMessage = new Gson().fromJson(jsonPayload, MessageResponse.class);

                // Đẩy qua cho Thư ký (ViewModel) xử lý ngầm
                runOnUiThread(() -> viewModel.handleNewMessage(newMessage, currentUserId));
            } catch (Exception e) {
                Log.e("SOCKET_DATA", "Lỗi Parse JSON ở ChatList", e);
            }
        });
    }

}
