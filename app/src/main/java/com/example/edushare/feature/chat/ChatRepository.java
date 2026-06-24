package com.example.edushare.feature.chat;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.base.RetrofitClient;
import com.example.edushare.feature.chat.conversation.dto.request.ConversationCreateRequest;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.chat.message.model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final ChatApi chatApi;

    public ChatRepository(Context context) {
        // RetrofitClient.createService: Sử dụng kỹ thuật Dynamic Proxy.
        // Retrofit sẽ tự tạo ra một class thực thi cho interface ChatApi lúc Runtime.
        // Giống như việc Spring tự tạo Implementation cho các JpaRepository.
        this.chatApi = RetrofitClient.createService(ChatApi.class, context);
    }

    public void getConversations(String userId, int currentPage, int size, OnActionCallback<PageResponse<Conversation>> callback) {
        // .enqueue(): ĐÂY LÀ ĐIỂM MẤU CHỐT.
        // Trong Web, khi gọi DB, luồng xử lý sẽ đứng đợi (Block) cho đến khi có dữ liệu.
        // Trong Android, nếu luồng chính (UI Thread) đứng đợi, App sẽ bị treo (ANR).
        // enqueue() sẽ đẩy việc gọi mạng xuống luồng phụ (Background Thread) để UI vẫn mượt mà.
        chatApi.getConversations(userId, currentPage, size).enqueue(new Callback<PageResponse<Conversation>>() {

            // onResponse: Tự động được gọi khi Server trả về phản hồi (kể cả lỗi 404, 500).
            // Tương đương với việc nhận được HttpServletResponse.
            @Override
            public void onResponse(@NonNull Call<PageResponse<Conversation>> call, @NonNull Response<PageResponse<Conversation>> response) {
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
            public void onFailure(@NonNull Call<PageResponse<Conversation>> call, @NonNull Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getMessages(int conversationId, int page, int size, OnActionCallback<PageResponse<MessageResponse>> callback) {
        try {
            chatApi.getMessages(conversationId, page, size).enqueue(new Callback<PageResponse<MessageResponse>>() {
                @Override
                public void onResponse(@NonNull Call<PageResponse<MessageResponse>> call, @NonNull Response<PageResponse<MessageResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onFailure("Lỗi Server: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PageResponse<MessageResponse>> call, @NonNull Throwable t) {
                    callback.onFailure("Lỗi kết nối: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(int conversationId, MessageRequest messageRequest, OnActionCallback<MessageResponse> callback) {
        try {
            chatApi.sendMessage(conversationId, messageRequest).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onFailure("Lỗi Server: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                    callback.onFailure("Lỗi kết nối: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void markMessageAsRead(int conversationId, String userId, String recipientId, OnActionCallback<Integer> callback) {
        chatApi.markMessageAsRead(conversationId, userId, recipientId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getPostSummary(int postId, OnActionCallback<PostSummaryResponse> callback) {
        chatApi.getPostSummary(postId).enqueue(new Callback<PostSummaryResponse>() {
            @Override
            public void onResponse(Call<PostSummaryResponse> call, Response<PostSummaryResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PostSummaryResponse> call, Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void sendFirstMessage(ConversationCreateRequest conversationCreateRequest, OnActionCallback<MessageResponse> callback) {
        chatApi.sendFirstMessage(conversationCreateRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                callback.onFailure("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    public void checkExistingConversation(String senderId, String recipientId,int postId, OnActionCallback<Integer> callback) {
        chatApi.checkExistingConversation(senderId, recipientId, postId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Lỗi Server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}