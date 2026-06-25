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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.common.helper.WebSocketManager;
import com.example.edushare.feature.chat.repository.ChatRepository;
import com.example.edushare.feature.chat.message.adapter.MessageAdapter;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.MessageStatusResponse;
import com.example.edushare.feature.chat.message.view_model.ChatDetailViewModel;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
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
    private MessageAdapter messageAdapter;

    // DATA LẤY TỪ ACTIVITY TRUYỀN QUA
    private int conversationId;
    private String recipientName;
    private String recipientAvatarUrl;
    private String senderId;
    private String recipientId;


    // =======================================
    // PHẦN 4: Thanh soạn tin nhắn và gửi tin nhắn
    private EditText etMessage;

    private ImageButton btnSend;
    private Disposable messageDisposable;
    private Disposable statusDisposable;

    private Button btnViewProduct;
    private ChatDetailViewModel chatDetailViewModel;

    private ProgressBar progressBar;


    // Hủy kết nối WebSocket khi Activity bị hủy
    // Tránh tốn RAM nếu WebSocket vẫn còn mở
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageDisposable != null && !messageDisposable.isDisposed()) {
            messageDisposable.dispose();
        }
        if (statusDisposable != null && !statusDisposable.isDisposed()) {
            statusDisposable.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        initViews();

        initViewModel();

        getIntentData();

        setupRecyclerView();

        setupListeners();

        chatDetailViewModel.initChat(conversationId, senderId, recipientId, postId);

        if (postId != -1) {
            chatDetailViewModel.fetchPostSummary(postId);
        }

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

        // PHẦN 2:  Nội dung của đoạn hội thoại
        rvMessages = findViewById(R.id.rvMessages);

        // PHẦN 3: Thanh soạn tin nhắn và gửi tin nhắn
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // PHẦN 4: ProgressBar
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        ChatRepository chatRepository = new ChatRepository(this);

        // Khởi tạo ViewModel
        chatDetailViewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new ChatDetailViewModel(chatRepository))
        ).get(ChatDetailViewModel.class);

        // Quan sát sự thay đổi của danh sách message và cập nhật sang adapter
        chatDetailViewModel.getMessageListLiveData().observe(this, messages -> {
            messageAdapter.updateData(messages);
        });

        // Các lỗi
        chatDetailViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Để cuộn xuống bên dưới với tin nhắn mới nhất
        chatDetailViewModel.getScrollToPosition().observe(this, position -> {
            if (position != null && position >= 0) {
                rvMessages.scrollToPosition(position);
            }
        });

        chatDetailViewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        chatDetailViewModel.getConversationIdLiveData().observe(this, id -> {
            if (id != null) {
                conversationId = id;
            }
        });

        // THÔNG TIN TÓM TẮT CỦA BÀI ĐĂNG
        chatDetailViewModel.getPostSummaryLiveData().observe(this, data -> {
            if (data == null) return;

            tvPostTitle.setText(data.getTitle());

            if (data.getPrice() != null) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPostPrice.setText(formatter.format(data.getPrice()) + "đ");
            } else {
                tvPostPrice.setText("Liên hệ");
            }

            Glide.with(this)
                    .load(data.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(imgPost);
        });
    }

    // SETUP TOÀN BỘ CÁC NÚT BẤM VÀ CÁC THAO TÁC
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                chatDetailViewModel.sendMessage(message);
                etMessage.setText("");
            }
        });

        btnViewProduct.setOnClickListener(view -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("POST_ID", postId);
            startActivity(intent);
        });

        btnCloseProduct.setOnClickListener(v -> {
            layoutProductHeader.setVisibility(View.GONE);
        });
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
            chatDetailViewModel.checkExistingConversation(senderId, recipientId, postId);

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
        messageAdapter = new MessageAdapter(new ArrayList<>(), senderId, recipientAvatarUrl);
        rvMessages.setAdapter(messageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    if (firstVisibleItem == 0 && chatDetailViewModel != null && chatDetailViewModel.canLoadMore()) {
                        chatDetailViewModel.loadNextPage();
                    }
                }
            }
        });
    }


    @SuppressLint("CheckResult")
    private void connectWebSocket() {
        // 1. Gọi Manager để đảm bảo "Trạm bơm" chính đã hoạt động
        WebSocketManager.getInstance().connectWebSocket();

        // 2. Lấy ID phòng và bắt đầu cắm ống nghe
        String myTopic = "/topic/messages/" + senderId;
        receiveNewMessage(myTopic);

        String destinationStatus = "/topic/receipts/" + senderId;
        receiveNewMessageStatus(destinationStatus);

    }

    @SuppressLint("CheckResult")
    private void receiveNewMessage(String myTopic) {
        // Lấy ống dùng chung
        StompClient sharedClient = WebSocketManager.getInstance().getStompClient();

        // Nhận thẻ đăng ký (Disposable) để sau này hủy
        messageDisposable = sharedClient.topic(myTopic).subscribe(stompMessage -> {
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

                        chatDetailViewModel.addNewMessage(newMessage);
                    }
                });
            } catch (Exception e) {
                Log.e("SOCKET_DATA", "Lỗi Parse JSON tin nhắn mới", e);
            }
        }, throwable -> {
            Log.e("WebSocket", "Lỗi đường truyền tin nhắn mới", throwable);
        });
    }

    @SuppressLint("CheckResult")
    private void receiveNewMessageStatus(String receiptTopic) {
        // Lấy ống dùng chung
        StompClient sharedClient = WebSocketManager.getInstance().getStompClient();

        // Nhận thẻ đăng ký (Disposable) để sau này hủy
        statusDisposable = sharedClient.topic(receiptTopic).subscribe(stompMessage -> {
            try {
                String jsonPayload = stompMessage.getPayload();
                Log.d("SOCKET_DATA", "Trạng thái nhận được: " + jsonPayload);

                Gson gson = new Gson();
                MessageStatusResponse newMessage = gson.fromJson(jsonPayload, MessageStatusResponse.class);

                runOnUiThread(() -> {
                    if (conversationId == newMessage.getConversationId() && newMessage.getRead()) {
                        chatDetailViewModel.updateLastMessageStatus(newMessage);
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
