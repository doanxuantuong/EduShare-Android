package com.example.edushare.feature.transaction.repository;

import android.content.Context;

import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.feature.item_detail.api.PostDetailApi;
import com.example.edushare.feature.transaction.api.TransactionApi;
import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;

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
}
