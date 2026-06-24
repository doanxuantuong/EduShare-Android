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
import com.example.edushare.feature.auth.activity.LoginActivity;

public class ProfileActivity extends AppCompatActivity {
    private View footerLayout;

    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;
    private View menuLogout;
    private View mainInfo; // Khai báo biến cho vùng thông tin chính

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadSession();
        setupLogoutListener();

        // Cài đặt sự kiện click cho vùng thông tin chính
        setupNavigationListeners();
    }

    private void initViews() {
        footerLayout = findViewById(R.id.footer_layout);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        menuLogout = findViewById(R.id.menuLogout);

        // Ánh xạ id vùng thông tin chính từ XML của Profile
        mainInfo = findViewById(R.id.main_info);

        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_profile);
    }

    private void loadSession() {
        tvName.setText(UserSessionHelper.getFullName(this));
        tvEmail.setText(UserSessionHelper.getEmail(this));
    }

    private void setupNavigationListeners() {
        // Khi click vào vùng thông tin cá nhân thì chuyển sang trang ProfileDetailActivity
        mainInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileDetailActivity.class);
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