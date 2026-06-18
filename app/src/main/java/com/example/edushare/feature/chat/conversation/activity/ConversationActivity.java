package com.example.edushare.feature.chat.conversation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.conversation.adapter.ConversationAdapter;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
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
    private ChatRepository chatRepository;
    private String currentUserId = "fire-base-uid-test-12";

    private StompClient stompClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Chỉ định giao diện hiển thị cho Activity
        setContentView(R.layout.activity_chat_list);

        //Khởi tạo sự kiện cho các View
        initViews();
        setupRecyclerView();

        // Khởi tạo Repository
        chatRepository = new ChatRepository();

        // Gọi API lấy dữ liệu thực
        fetchConversationsFromApi(currentUserId);

        connectWebSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi màn hình này hiện lên (kể cả lúc từ phòng chat quay ra),
        // bắt buộc phải gọi lại API để lấy danh sách lastMessage mới nhất.
        if (currentUserId != null) {
            fetchConversationsFromApi(currentUserId);
        }
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
    }

    private void setupRecyclerView() {
        // Tạo danh sách các đoạn hội thoại (conversationList)
        conversationList = new ArrayList<>();
        // Thêm dữ liệu qua bên Adapter cho Adapter load lên
        adapter = new ConversationAdapter(conversationList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        //Gắn Adapter vào RecyclerView để hiển thị danh sách
        rvChatList.setAdapter(adapter);
    }

    private void fetchConversationsFromApi(String currentUserId) {
        chatRepository.getConversations(currentUserId, new OnActionCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                // Xóa danh sách cũ và thêm dữ liệu mới từ API
                conversationList.clear();
                conversationList.addAll(data);

                // Thông báo cho adapter cập nhật giao diện
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                // Hiển thị thông báo lỗi bằng Toast
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
            }else {
                // Đã ở sẵn trên đầu rồi thì chỉ cần vẽ lại vị trí 0
                adapter.notifyItemChanged(0);
            }

            // Đẩy RecyclerView cuộn mượt mà lên vị trí trên cùng
            rvChatList.smoothScrollToPosition(0);

        } else {
            fetchConversationsFromApi(currentUserId);
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
                    break;
                case ERROR:
                    Log.d("WebSocket", "Lỗi kết nối", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("SOCKET", "Đã ngắt kết nối");
                    break;
            }
        });

        String myTopic = "/topic/messages/" + currentUserId;
        stompClient.topic(myTopic).subscribe(stompMessage -> {
            String jsonPayload = stompMessage.getPayload();

            Gson gson = new Gson();
            MessageResponse newMessage = gson.fromJson(jsonPayload, MessageResponse.class);

            runOnUiThread(() -> {
                updateConversationList(newMessage);
            });
        });
    }
}
