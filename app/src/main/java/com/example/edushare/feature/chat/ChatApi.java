package com.example.edushare.feature.chat;


import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {
    // @GET: Khai báo phương thức HTTP là GET.
    // Tương đương @GetMapping("/conversations") trong Spring Boot.
    @GET("/conversations")

    // Call<List<Conversation>>: Đây là một "hợp đồng" sẽ thực hiện trong tương lai.
    // Call: Là đối tượng bao bọc (Wrapper). Nó chưa chứa dữ liệu ngay mà đợi lệnh thực thi.
    // List<Conversation>: Kiểu dữ liệu mong muốn sau khi JSON được parse tự động bởi Gson.

    // @Query("userId"): Tham số truyền trên URL (?userId=...).
    // Tương đương với request.getParameter("userId") trong Servlet hoặc @RequestParam trong Spring.
    Call<List<Conversation>> getConversations(@Query("userId") String userId);

    @GET("/conversations/messages")
    Call<List<MessageResponse>> getMessages(@Query("conversationId") int conversationId);

    @POST("/conversations/messages")
    Call<MessageResponse> sendMessage(@Body MessageRequest messageRequest);

    @PUT("/conversations/messages/{conversationId}/read")
    Call<Integer> markMessageAsRead(@Path("conversationId") int conversationId, @Query("userId") String userId, @Query("recipientId") String recipientId);
}