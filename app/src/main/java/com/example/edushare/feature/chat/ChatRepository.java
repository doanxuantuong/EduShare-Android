package com.example.edushare.feature.chat;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi chatApi;

    public ChatRepository() {
        // RetrofitClient.createService: Sử dụng kỹ thuật Dynamic Proxy.
        // Retrofit sẽ tự tạo ra một class thực thi cho interface ChatApi lúc Runtime.
        // Giống như việc Spring tự tạo Implementation cho các JpaRepository.
        this.chatApi = RetrofitClient.createService(ChatApi.class);
    }

    public void getConversations(String userId, OnActionCallback<List<Conversation>> callback) {
        // .enqueue(): ĐÂY LÀ ĐIỂM MẤU CHỐT.
        // Trong Web, khi gọi DB, luồng xử lý sẽ đứng đợi (Block) cho đến khi có dữ liệu.
        // Trong Android, nếu luồng chính (UI Thread) đứng đợi, App sẽ bị treo (ANR).
        // enqueue() sẽ đẩy việc gọi mạng xuống luồng phụ (Background Thread) để UI vẫn mượt mà.
        chatApi.getConversations(userId).enqueue(new Callback<List<Conversation>>() {

            // onResponse: Tự động được gọi khi Server trả về phản hồi (kể cả lỗi 404, 500).
            // Tương đương với việc nhận được HttpServletResponse.
            @Override
            public void onResponse(@NonNull Call<List<Conversation>> call, @NonNull Response<List<Conversation>> response) {
                // response.isSuccessful(): Kiểm tra HTTP Code có nằm trong khoảng 200-299 không.
                if (response.isSuccessful() && response.body() != null) {
                    // response.body(): JSON đã được Gson chuyển thành List<Conversation> một cách kỳ diệu.
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            // onFailure: Gọi khi không thể kết nối tới Server (Mất mạng, sai địa chỉ IP, Server sập).
            @Override
            public void onFailure(@NonNull Call<List<Conversation>> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getMessages(int conversationId, OnActionCallback<List<Message>> callback) {
        try {
            chatApi.getMessages(conversationId).enqueue(new Callback<List<Message>>() {
                @Override
                public void onResponse(@NonNull Call<List<Message>> call, @NonNull Response<List<Message>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onFailure("Lỗi Server: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Message>> call,@NonNull Throwable t) {
                    callback.onFailure("Lỗi kết nối: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}