package com.example.edushare.feature.notification.view_model;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.feature.chat.message.dto.PageResponse;
import com.example.edushare.feature.notification.NotificationRepository;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<NotificationResponseDto>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private boolean isLastPage = false; // Cờ báo hiệu đã tải hết sạch data chưa
    private boolean isFetching = false; // Cờ chặn spam API khi user vuốt quá nhanh

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

    public void loadNextPage(String userId) {
        // Nếu đã tải hết trang cuối, HOẶC đang trong quá trình chờ API trả về
        // -> Dừng lại, không gọi tiếp
        if (isLastPage || isFetching) {
            return;
        }

        isFetching = true;
        loading.setValue(true);

        notificationRepository.getUserNotification(userId, currentPage, PAGE_SIZE, new OnActionCallback<PageResponse<NotificationResponseDto>>() {
            @Override
            public void onSuccess(PageResponse<NotificationResponseDto> data) {
                isFetching = false;
                loading.postValue(false);
                List<NotificationResponseDto> currentList = notifications.getValue();

                if (currentList == null) {
                    currentList = new ArrayList<>(); // Nếu là lần đầu tiên, tạo list mới rỗng
                }

                currentList.addAll(data.getContent());

                // Cập nhật lại UI
                notifications.postValue(currentList);

                // Đọc cờ "last" từ JSON của Spring Boot
                isLastPage = data.isLast();
                if (!isLastPage) {
                    // Nếu chưa phải trang cuối, tăng biến đếm lên 1 cho lần cuộn tiếp theo
                    currentPage++;
                }
            }

            @Override
            public void onFailure(String message) {
                isFetching = false;
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void markAllAsRead(String userId) {
        notificationRepository.markNotificationsAsRead(userId, new OnActionCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Thành công! Không cần update giao diện vì user đang thoát ra Home rồi.
            }

            @Override
            public void onFailure(String message) {
                // Log lỗi ra chứ không báo Toast để tránh phiền user lúc họ đang thoát
                errorMessage.postValue("Lỗi ẩn: " + message);
            }
        });
    }

}
