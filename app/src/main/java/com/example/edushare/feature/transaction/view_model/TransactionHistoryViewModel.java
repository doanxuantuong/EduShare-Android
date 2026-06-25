package com.example.edushare.feature.transaction.view_model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.edushare.feature.transaction.dto.response.TransactionResponseDTO;
import com.example.edushare.feature.transaction.repository.TransactionRepository;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryViewModel extends ViewModel {

    private final TransactionRepository repository;

    // Các kênh phát sóng (LiveData)
    private final MutableLiveData<List<TransactionResponseDTO>> transactionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();

    public TransactionHistoryViewModel(TransactionRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<TransactionResponseDTO>> getTransactionsLiveData() { return transactionsLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getActionSuccessLiveData() { return actionSuccessLiveData; }


    public void fetchHistory(String token, String userId, String role, String status) {
        isLoading.setValue(true);
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

    private Callback<ResponseBody> createActionCallback(String actionName) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false); // Tắt loading chung
                if (response.isSuccessful()) {
                    actionSuccessLiveData.setValue(true); // Báo Fragment reload lại tab
                } else {
                    try {
                        // Đọc câu chửi lỗi chi tiết từ Backend trả về nếu có
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Thao tác thất bại!";
                        errorMessage.setValue("Lỗi: " + errorMsg);
                    } catch (Exception e) {
                        errorMessage.setValue(actionName + " thất bại!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        };
    }



    public void acceptTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.acceptTransaction(token, transactionId, createActionCallback("Duyệt đơn"));
    }

    public void rejectTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.rejectTransaction(token, transactionId, createActionCallback("Từ chối đơn"));
    }

    public void completeTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.completeTransaction(token, transactionId, createActionCallback("Xác nhận nhận hàng"));
    }

    public void cancelTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.cancelTransaction(token, transactionId, createActionCallback("Hủy giao dịch"));
    }
}