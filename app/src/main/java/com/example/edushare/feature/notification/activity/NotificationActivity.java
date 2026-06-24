package com.example.edushare.feature.notification.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.notification.NotificationRepository;
import com.example.edushare.feature.notification.adapter.NotificationAdapter;
import com.example.edushare.feature.notification.view_model.NotificationViewModel;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private NotificationViewModel notificationViewModel;
    private ImageButton btnBack;
    private String userId;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        initView();
        initRecyclerView();
        initViewModel();
        observeViewModel();

        userId = UserSessionHelper.getUserId(this);

        if (userId != null && !userId.isEmpty()) {
            notificationViewModel.loadNextPage(userId);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 1. Chạy lén API đánh dấu đã đọc trước khi thoát
                if (userId != null && !userId.isEmpty()) {
                    notificationViewModel.markAllAsRead(userId);
                }

                finish();
            }
        });

        btnBack.setOnClickListener(view -> {
            // Lệnh này sẽ tự động kích hoạt cái handleOnBackPressed() ở Bước 2
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void initView() {
        rvNotifications = findViewById(R.id.rvNotifications);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initRecyclerView() {
        notificationAdapter = new NotificationAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvNotifications.setLayoutManager(layoutManager);
        rvNotifications.setAdapter(notificationAdapter);

        rvNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // dy > 0 nghĩa là người dùng đang vuốt TỪ DƯỚI LÊN (cuộn xuống xem bài cũ)
                if (dy > 0) {
                    // Lấy ra số lượng item đang hiển thị trên màn hình
                    int visibleItemCount = layoutManager.getChildCount();
                    // Lấy ra tổng số item đang có trong Adapter
                    int totalItemCount = layoutManager.getItemCount();
                    // Lấy ra vị trí của item đầu tiên đang hiển thị trên cùng
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    // CÔNG THỨC KIỂM TRA ĐÁY MÀN HÌNH:
                    // Nếu (số item nhìn thấy + số item đã cuộn qua) >= tổng số item
                    // Có nghĩa là đã chạm tới đáy của danh sách!
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        if (userId != null && !userId.isEmpty()) {
                            // Gọi ViewModel kéo trang tiếp theo về
                            notificationViewModel.loadNextPage(userId);
                        }
                    }
                }
            }
        });
    }

    private void initViewModel() {
        NotificationRepository repository = new NotificationRepository(this);

        notificationViewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new NotificationViewModel(repository))
        ).get(NotificationViewModel.class);
    }

    private void observeViewModel() {
        notificationViewModel.getNotifications().observe(this, notifications -> {
            notificationAdapter.setData(notifications);
        });

        notificationViewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        notificationViewModel.getErrorMessage().observe(this, message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

}
