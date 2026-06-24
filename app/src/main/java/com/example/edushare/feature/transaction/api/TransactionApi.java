package com.example.edushare.feature.transaction.api;

import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;
import com.example.edushare.feature.transaction.dto.response.TransactionResponseDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransactionApi {
    @POST("transactions/request")
    Call<ResponseBody> createTransaction(@Body TransactionRequestDTO requestDTO);

    @GET("transactions/history")
    Call<List<TransactionResponseDTO>> getTransactionHistory(
            @Header("Authorization") String token,
            @Query("role") String role,
            @Query("status") String status
    );
    @PUT("transactions/{id}/accept")
    Call<ResponseBody> acceptTransaction(
            @Header("Authorization") String token,
            @Path("id") int transactionId
    );

    @PUT("transactions/{id}/reject")
    Call<ResponseBody> rejectTransaction(
            @Header("Authorization") String token,
            @Path("id") int transactionId
    );
}

