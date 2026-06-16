package com.example.edushare.feature.chat.message.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.message.adapter.MessageAdapter;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.model.Message;

import java.util.ArrayList;
import java.util.List;

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
    private String receiverName;
    private String receiverAvatarUrl;
    private String senderId;
//    private int receiverId;

    // =======================================
    // PHẦN 3: Thanh soạn tin nhắn và gửi tin nhắn
    private EditText etMessage;

    private ImageButton btnSend;

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
            if (message != null && !message.isEmpty()) {
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
//            receiverId = intent.getIntExtra("RECEIVER_ID", -1);
            receiverName = intent.getStringExtra("RECEIVER_NAME");
            receiverAvatarUrl = intent.getStringExtra("RECEIVER_AVATAR");

            if (receiverName != null) {
                tvPartnerName.setText(receiverName);
            }
        }
    }

    private void setupRecyclerView() {
        // Khởi tạo danh sách tin nhắn
        messageList = new ArrayList<>();

        // Khởi tạo Adapter và gắn vào RecyclerView
        messageAdapter = new MessageAdapter(messageList, "fire-base-uid-test-12", receiverAvatarUrl);
        rvMessages.setAdapter(messageAdapter);
//        rvMessages.setLayoutManager(new LinearLayoutManager(this));
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
                messageList.add(data);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
