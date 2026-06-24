package com.example.edushare.feature.transaction.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.edushare.feature.transaction.dto.response.TransactionResponseDTO;
import com.example.edushare.feature.transaction.repository.TransactionRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryViewModel extends ViewModel {

    private final TransactionRepository repository;

    // Các kênh phát sóng (LiveData)
    private final MutableLiveData<List<TransactionResponseDTO>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TransactionHistoryViewModel(TransactionRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<TransactionResponseDTO>> getTransactionsLiveData() { return transactionsLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Hàm gọi API
    public void fetchHistory(String token, String userId, String role, String status) {
        isLoading.setValue(true);
        // Truyền token vào đây
        repository.getTransactionHistory(token, role, status, new Callback<List<TransactionResponseDTO>>() {
            @Override
            public void onResponse(Call<List<TransactionResponseDTO>> call, Response<List<TransactionResponseDTO>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    transactionsLiveData.setValue(response.body());
                } else {
                    errorMessage.setValue("Lỗi lấy dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponseDTO>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}