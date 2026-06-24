package com.example.edushare.feature.users.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.edushare.R;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.users.UserRepository;
import com.example.edushare.feature.users.dto.request.UserProfileDetailRequest;
import com.example.edushare.feature.users.view_model.ProfileDetailViewModel;

public class ProfileDetailActivity extends AppCompatActivity {
    private ImageView ivBack;
    private ImageView ivDetailAvatar;
    private ImageView ivChangeAvatar;
    private EditText etDetailName;
    private EditText etDetailStudentCode;
    private EditText etDetailPhone;
    private Button btnUpdateProfile; // Khai báo thêm nút cập nhật ở cuối Layout của bạn

    private ProfileDetailViewModel profileDetailViewModel;
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        token = UserSessionHelper.getToken(this);
        userId = UserSessionHelper.getUserId(this);

        initViews();
        initViewModel();
        observeViewModel();
        setupClickListeners();

        // Tự động load dữ liệu cũ lên form khi vừa vào màn hình
        if (token != null && !token.isEmpty()) {
            profileDetailViewModel.loadCurrentUser(token);
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivDetailAvatar = findViewById(R.id.ivDetailAvatar);
        ivChangeAvatar = findViewById(R.id.ivChangeAvatar);
        etDetailName = findViewById(R.id.etDetailName);
        etDetailStudentCode = findViewById(R.id.etDetailStudentCode);
        etDetailPhone = findViewById(R.id.etDetailPhone);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile); // Hãy chắc chắn id này khớp với nút ở XML nhé
    }

    private void initViewModel() {
        UserRepository repository = new UserRepository(this);
        profileDetailViewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new ProfileDetailViewModel(repository))
        ).get(ProfileDetailViewModel.class);
    }

    private void observeViewModel() {
        // Đổ dữ liệu từ Server lên các ô EditText
        profileDetailViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                etDetailName.setText(user.getFullName());
                etDetailStudentCode.setText(user.getStudentCode());
                etDetailPhone.setText(user.getPhone());
            }
        });

        // Báo lỗi bằng Toast
        profileDetailViewModel.getErrorMessage().observe(this, message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Báo cập nhật thành công
        profileDetailViewModel.getUpdateSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình Profile chính
            }
        });
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Bấm nút cập nhật thông tin cá nhân
        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> {
                String name = etDetailName.getText().toString().trim();
                String code = etDetailStudentCode.getText().toString().trim();
                String phone = etDetailPhone.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Không được bỏ trống Họ tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserProfileDetailRequest request = new UserProfileDetailRequest();
                request.setFullName(name);
                request.setStudentCode(code);
                request.setPhone(phone);

                // Giữ nguyên avatar cũ, tính năng đẩy ảnh xử lý sau
                if (profileDetailViewModel.getUserProfile().getValue() != null) {
                    request.setAvatarUrl(profileDetailViewModel.getUserProfile().getValue().getAvatarUrl());
                }

                if (userId != null && !userId.isEmpty()) {
                    profileDetailViewModel.updateProfile(userId, request);
                }
            });
        }
    }
}