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

    public LiveData<Boolean> getActionSuccessLiveData() {
        return actionSuccessLiveData;
    }
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
    public void acceptTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.acceptTransaction(token, transactionId, new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    actionSuccessLiveData.setValue(true); // Báo thành công
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                        errorMessage.setValue("Lỗi: " + errorMsg);
                    } catch (Exception e) {
                        errorMessage.setValue("Duyệt đơn thất bại!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    public void rejectTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.rejectTransaction(token, transactionId, new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    actionSuccessLiveData.setValue(true); // Báo thành công
                } else {
                    errorMessage.setValue("Từ chối đơn thất bại!");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
    public void completeTransaction(String token, int transactionId) {
        isLoading.setValue(true);
        repository.completeTransaction(token, transactionId, new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    actionSuccessLiveData.setValue(true); // Kích hoạt để Fragment tự reload lại data
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Thao tác thất bại!";
                        errorMessage.setValue("Lỗi: " + errorMsg);
                    } catch (Exception e) {
                        errorMessage.setValue("Xác nhận thất bại!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
}