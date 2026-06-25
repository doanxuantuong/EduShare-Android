package com.example.edushare.feature.item_detail.view_model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel; // 🔥 Chuyển sang import cái này
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.item_detail.repository.PostDetailRepository;

// Đổi từ 'extends ViewModel' sang 'extends AndroidViewModel'
public class PostDetailViewModel extends AndroidViewModel {

    private final PostDetailRepository repository;
    private final MutableLiveData<PostDetailResponseDTO> postDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // 🔥 Sửa hàm khởi tạo nhận tham số Application như thế này:
    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        // Tự tạo repository trực tiếp bằng application context
        this.repository = new PostDetailRepository(application);
    }

    public LiveData<PostDetailResponseDTO> getPostDetailLiveData() { return postDetailLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadPostDetail(int postId) {
        isLoading.setValue(true);
        repository.getPostDetail(postId, new OnActionCallback<PostDetailResponseDTO>() {
            @Override
            public void onSuccess(PostDetailResponseDTO data) {
                isLoading.setValue(false);
                if (data != null) {
                    postDetailLiveData.setValue(data);
                } else {
                    errorMessage.setValue("Dữ liệu bài đăng rỗng!");
                }
            }

            @Override
            public void onFailure(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}