package com.example.edushare.feature.transaction.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;
import com.example.edushare.feature.transaction.repository.TransactionRepository;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionViewModel extends ViewModel {
    private final TransactionRepository transactionRepository;

    // Các LiveData quản lý trạng thái UI giống hệt bên Profile
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> transactionSuccess = new MutableLiveData<>();

    public TransactionViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MutableLiveData<Boolean> getLoading() { return loading; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
    public MutableLiveData<Boolean> getTransactionSuccess() { return transactionSuccess; }

    // Hàm xử lý nghiệp vụ gọi API tạo Giao dịch
    public void createTransaction(TransactionRequestDTO requestDTO) {
        loading.setValue(true); // Bật trạng thái Loading trên UI

        transactionRepository.createTransaction(requestDTO, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loading.postValue(false); // Tắt loading
                if (response.isSuccessful()) {
                    transactionSuccess.postValue(true); // Báo UI biết là thành công
                } else {
                    errorMessage.postValue("Lỗi hệ thống mã: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loading.postValue(false); // Tắt loading
                errorMessage.postValue("Lỗi kết nối: " + t.getMessage()); // Báo lỗi mạng
            }
        });
    }
}