package com.example.edushare.feature.users.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.users.repository.UserRepository;
import com.example.edushare.feature.users.dto.request.UserProfileDetailRequest;
import com.example.edushare.feature.users.model.User;

public class ProfileDetailViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<User> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public ProfileDetailViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public MutableLiveData<User> getUserProfile() { return userProfile; }
    public MutableLiveData<Boolean> getLoading() { return loading; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }
    public MutableLiveData<Boolean> getUpdateSuccess() { return updateSuccess; }

    // Gọi API lấy dữ liệu hiện tại nạp vào Form nhập
    public void loadCurrentUser(String token) {
        loading.setValue(true);
        userRepository.getCurrentUser(token, new OnActionCallback<User>() {
            @Override
            public void onSuccess(User data) {
                loading.postValue(false);
                userProfile.postValue(data);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    // Gọi API cập nhật thông tin chỉnh sửa lên DB
    public void updateProfile(String id, UserProfileDetailRequest request) {
        loading.setValue(true);
        userRepository.updateProfile(id, request, new OnActionCallback<User>() {
            @Override
            public void onSuccess(User data) {
                loading.postValue(false);
                userProfile.postValue(data); // Đè dữ liệu mới cập nhật về lại UI
                updateSuccess.postValue(true);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
}