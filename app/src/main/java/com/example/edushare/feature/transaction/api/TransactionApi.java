package com.example.edushare.feature.transaction.api;

import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TransactionApi {
    @POST("transactions/request")
    Call<ResponseBody> createTransaction(@Body TransactionRequestDTO requestDTO);
}

