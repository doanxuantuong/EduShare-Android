package com.example.edushare.feature.users.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.auth.activity.LoginActivity;

public class ProfileDetailActivity extends AppCompatActivity {
    private ImageView ivBack;
    private ImageView ivDetailAvatar;
    private ImageView ivChangeAvatar;
    private EditText etDetailName;
    private EditText etDetailStudentCode;
    private EditText etDetailPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Ánh xạ các thành phần giao diện của trang Profile Detail
        ivBack = findViewById(R.id.ivBack);
        ivDetailAvatar = findViewById(R.id.ivDetailAvatar);
        ivChangeAvatar = findViewById(R.id.ivChangeAvatar);
        etDetailName = findViewById(R.id.etDetailName);
        etDetailStudentCode = findViewById(R.id.etDetailStudentCode);
        etDetailPhone = findViewById(R.id.etDetailPhone);
    }

    private void setupClickListeners() {
        // Sự kiện nhấn vào nút mũi tên quay lại
        ivBack.setOnClickListener(v -> {
            finish(); // Đóng Activity này để quay về màn hình Profile trước đó
        });
    }
}
