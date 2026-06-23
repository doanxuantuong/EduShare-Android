package com.example.edushare.feature.users.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Import để làm hộp thoại xác nhận
import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.auth.activity.LoginActivity; // Import trang Login để chuyển màn hình

public class ProfileActivity extends AppCompatActivity {
    private View footerLayout;

    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;
    private View menuLogout; // 1. Khai báo thêm biến cho nút Đăng xuất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ toàn bộ các View lên trước
        initViews();

        // Đổ dữ liệu tài khoản từ Session lên giao diện
        loadSession();

        // Cài đặt sự kiện Click cho nút Đăng xuất
        setupLogoutListener();
    }

    private void initViews() {
        // Ánh xạ thẻ include từ file activity_profile.xml
        footerLayout = findViewById(R.id.footer_layout);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);

        // Ánh xạ id của nút Đăng xuất
        menuLogout = findViewById(R.id.menuLogout);

        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_profile);
    }

    private void loadSession() {
        tvName.setText(UserSessionHelper.getFullName(this));
        tvEmail.setText(UserSessionHelper.getEmail(this));
    }

    private void setupLogoutListener() {
        menuLogout.setOnClickListener(v -> {
            // Hiển thị một Dialog hỏi lại cho chắc chắn, tránh người dùng bấm nhầm khi đang lướt
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi EduShare không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {

                        // 1. Xóa sạch dữ liệu Session (Token, Email, Name...) đã lưu trước đó
                        UserSessionHelper.clearSession(ProfileActivity.this);

                        // Thông báo nhanh cho người dùng
                        Toast.makeText(ProfileActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                        // 2. Chuyển hướng người dùng quay về màn hình Đăng nhập (LoginActivity)
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);

                        // Xóa sạch toàn bộ "lịch sử" các Activity cũ (Stack) để ngăn người dùng nhấn nút Back quay lại trang Profile
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()) // Bấm Hủy thì đóng tắt dialog đi
                    .show();
        });
    }
}