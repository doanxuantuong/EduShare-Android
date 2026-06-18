package com.example.edushare.feature.chat.message.activity;

import static ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.message.adapter.MessageAdapter;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.MessageStatusResponse;
import com.example.edushare.feature.chat.message.model.Message;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ChatDetailActivity extends AppCompatActivity {

    // =======================================
    // PHẦN 1: Nút quay về + Tên người nhận tin nhắn
    private ImageButton btnBack;
    private TextView tvPartnerName;

//    private ImageView ivPartnerAvatar;

    // =======================================
    // PHẦN 2:  Nội dung của đoạn hội thoại
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

    // =======================================
    // PHẦN 3: Thanh soạn tin nhắn và gửi tin nhắn
    private EditText etMessage;

    private ImageButton btnSend;

    private StompClient stompClient;

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
                MessageRequest messageRequest = new MessageRequest();
                messageRequest.setContent(message);
                messageRequest.setConversationId(conversationId);
                messageRequest.setSenderId(senderId);

                fetchCreateMessageFromApi(messageRequest);

                etMessage.setText("");
            }
        });

        // Call API lấy danh sách đoạn tin nhắn của conversation
        chatRepository = new ChatRepository();
        fetchMessagesByConversationIdFromApi(conversationId);
        // Kết nối WebSocket
        connectWebSocket();

    }

    private void initViews() {
        // PHẦN 1: Nút quay về + Tên người nhận tin nhắn
        btnBack = findViewById(R.id.btnBack);
        tvPartnerName = findViewById(R.id.tvPartnerName);

        //ivPartnerAvatar = findViewById(R.id.ivPartnerAvatar);

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
            senderId = intent.getStringExtra("SENDER_ID");
            recipientId = intent.getStringExtra("RECIPIENT_ID");
            recipientName = intent.getStringExtra("RECIPIENT_NAME");
            recipientAvatarUrl = intent.getStringExtra("RECIPIENT_AVATAR");

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

    }


    private void fetchMessagesByConversationIdFromApi(int conversationId) {
        try {
            chatRepository.getMessages(conversationId, new OnActionCallback<List<MessageResponse>>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(List<MessageResponse> data) {
                    messageList.clear();
                    messageList.addAll(data);

                    messageAdapter.notifyDataSetChanged();

                    // Đánh dấu là đã đọc tin nhắn cuối cùng
                    markConversationAsRead();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchCreateMessageFromApi(MessageRequest messageRequest) {
        chatRepository.sendMessage(messageRequest, new OnActionCallback<MessageResponse>() {
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

    // Khi có 1 tin nhắn mới được thêm vào list
    public void addNewMessage(MessageResponse newMessage) {
        int oldSize = messageList.size();
        messageList.add(newMessage);

        if (oldSize > 0) {
            // 1. Cập nhật lại tin nhắn ngay trước đó (để nó ẩn thời gian/avatar đi vì không còn là tin cuối)
            messageAdapter.notifyItemChanged(oldSize - 1);
        }

        // 2. Thông báo thêm tin nhắn mới
        messageAdapter.notifyItemInserted(oldSize);

        // 3. Cuộn xuống cuối cùng
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

    @SuppressLint("CheckResult")
    private void connectWebSocket() {
        // 1. Tạo URL kết nối (Nhớ đổi 10.0.2.2 thành IP LAN nếu test trên điện thoại thật)
        // Phải có đuôi /websocket vì cổng thuần của Spring Boot yêu cầu thế
        String wsUrl = "ws://10.0.2.2:8080/ws-edushare/websocket";

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);
        stompClient.connect();

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
        });

        // 3. Đăng ký nhận tin nhắn từ kênh của riêng mình (Dùng ID đăng nhập của user hiện tại)
        String myTopic = "/topic/messages/" + senderId;
        receiveNewMessage(myTopic);

        String destinationStatus = "/topic/receipts/" + senderId;
        receiveNewMessageStatus(destinationStatus);

    }

    @SuppressLint("CheckResult")
    private void receiveNewMessage(String myTopic) {
        stompClient.topic(myTopic).subscribe(stompMessage -> {
            // Xử lý tin nhắn nhận được
            String jsonPayload = stompMessage.getPayload();

            // Parse JSON thành Object MessageResponse bằng Gson
            Gson gson = new Gson();
            MessageResponse newMessage = gson.fromJson(jsonPayload, MessageResponse.class);

            // BẮT BUỘC: Android quy định chỉ Main Thread mới được phép cập nhật Giao diện
            runOnUiThread(() -> {
                // Kiểm tra xem tin nhắn này có thuộc về cuộc hội thoại đang mở không
                if (newMessage.getConversationId() == conversationId) {
                    addNewMessage(newMessage);
                }
            });
        });
    }

    @SuppressLint("CheckResult")
    private void receiveNewMessageStatus(String receiptTopic) {
        stompClient.topic(receiptTopic).subscribe(stompMessage -> {
            // Xử lý tin nhắn nhận được
            String jsonPayload = stompMessage.getPayload();

            // Parse JSON thành Object MessageResponse bằng Gson
            Gson gson = new Gson();
            MessageStatusResponse newMessage = gson.fromJson(jsonPayload, MessageStatusResponse.class);

            // BẮT BUỘC: Android quy định chỉ Main Thread mới được phép cập nhật Giao diện
            runOnUiThread(() -> {
                // Kiểm tra xem tin nhắn này có thuộc về cuộc hội thoại đang mở không
                if (conversationId == newMessage.getConversationId() && newMessage.getRead()) {
                    updateLastMessageStatus();
                }
            });
        });
    }

}
