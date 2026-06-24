package com.example.edushare.feature.transaction.repository;

import android.content.Context;

import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.feature.item_detail.api.PostDetailApi;
import com.example.edushare.feature.transaction.api.TransactionApi;
import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;
import com.example.edushare.feature.transaction.dto.response.TransactionResponseDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class TransactionRepository {
    private final TransactionApi transactionApi;

    public TransactionRepository(Context context) {
        this.transactionApi = RetrofitClient.createService(TransactionApi.class, context);
    }
    public void createTransaction(TransactionRequestDTO requestDTO, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = transactionApi.createTransaction(requestDTO);
        call.enqueue(callback); // Chạy bất đồng bộ ngầm
    }
    public void getTransactionHistory(String token, String role, String status, Callback<List<TransactionResponseDTO>> callback) {
        Call<List<TransactionResponseDTO>> call = transactionApi.getTransactionHistory("Bearer " + token, role, status);
        call.enqueue(callback);
    }
    public void acceptTransaction(String token, int transactionId, retrofit2.Callback<ResponseBody> callback) {
        transactionApi.acceptTransaction(token, transactionId).enqueue(callback);
    }

    public void rejectTransaction(String token, int transactionId, retrofit2.Callback<ResponseBody> callback) {
        transactionApi.rejectTransaction(token, transactionId).enqueue(callback);
    }
    public void completeTransaction(String token, int transactionId, retrofit2.Callback<ResponseBody> callback) {
        transactionApi.completeTransaction(token, transactionId).enqueue(callback);
    }
}
