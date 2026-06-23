package com.example.edushare.feature.notification.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.notification.NotificationRepository;
import com.example.edushare.feature.notification.adapter.NotificationAdapter;
import com.example.edushare.feature.notification.view_model.NotificationViewModel;
import com.example.edushare.feature.notification.view_model.NotificationViewModelFactory;

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
            notificationViewModel.loadNotification(userId);
        }

        btnBack.setOnClickListener(view -> {
            finish();
        });
    }

    private void initView() {
        rvNotifications = findViewById(R.id.rvNotifications);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initRecyclerView() {
        notificationAdapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void initViewModel() {
        // 1. Tạo Repository và nhét Context (chính là chữ 'this' của Activity) vào
        NotificationRepository repository = new NotificationRepository(this);

        // 2. Nhét Repository vào Factory
        NotificationViewModelFactory factory = new NotificationViewModelFactory(repository);

        // 3. Khởi tạo ViewModel thông qua Factory
        notificationViewModel = new ViewModelProvider(this, factory).get(NotificationViewModel.class);
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
