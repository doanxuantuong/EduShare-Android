package com.example.edushare.feature.notification.view_model;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.feature.notification.NotificationRepository;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<NotificationResponseDto>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public NotificationViewModel(NotificationRepository repository) {
        this.notificationRepository = repository;
    }
    public MutableLiveData<List<NotificationResponseDto>> getNotifications() {
        return notifications;
    }
    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void loadNotification(String userId) {
        loading.setValue(true);

        notificationRepository.getUserNotification(userId, new OnActionCallback<List<NotificationResponseDto>>() {
            @Override
            public void onSuccess(List<NotificationResponseDto> data) {
                loading.postValue(false);
                notifications.postValue(data);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

}
