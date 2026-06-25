package com.example.edushare.feature.users.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.common.helper.WebSocketManager;
import com.example.edushare.feature.auth.activity.LoginActivity;
import com.example.edushare.feature.transaction.activity.TransactionHistoryActivity;
import com.example.edushare.feature.my_items.activity.MyItemsActivity;

public class ProfileActivity extends AppCompatActivity {
    private View footerLayout;
    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;
    private View menuMyItems;
    private View menuLogout;
    private View mainInfo;
    private View menuTransactionHistory;

    // 1. Khai báo thêm biến menu thông báo
    private View menuNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadSession();
        setupMyItemsListener();
        setupLogoutListener();
        setupNavigationListeners();
    }

    private void initViews() {
        footerLayout = findViewById(R.id.footer_layout);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        menuLogout = findViewById(R.id.menuLogout);
        menuTransactionHistory = findViewById(R.id.menuTransactionHistory);
        mainInfo = findViewById(R.id.main_info);
        menuMyItems = findViewById(R.id.menuMyItems);

        // 2. Ánh xạ id chính xác từ file XML profile
        menuNotifications = findViewById(R.id.menuNotifications);

        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_profile);
    }

    private void setupNavigationListeners() {
        mainInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileDetailActivity.class);
            startActivity(intent);
        });

        menuTransactionHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        // 3. Thêm sự kiện click chuyển sang màn hình danh sách thông báo
        menuNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.edushare.feature.notification.activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void loadSession() {
        tvName.setText(UserSessionHelper.getFullName(this));
        tvEmail.setText(UserSessionHelper.getEmail(this));
    }

    private void setupMyItemsListener() {
        // Mở danh sách item của chính người dùng từ menu Profile.
        menuMyItems.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, MyItemsActivity.class);
            startActivity(intent);
        });
    }

    private void setupLogoutListener() {
        menuLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi EduShare không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        UserSessionHelper.clearSession(ProfileActivity.this);
                        Toast.makeText(ProfileActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                        WebSocketManager.getInstance().disconnect();

                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}