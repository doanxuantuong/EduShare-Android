package com.example.edushare.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.helper.RegisterValidator;
import com.example.edushare.feature.auth.request.SendOtpRequest;
import com.example.edushare.feature.users.room.LocalUserRepository;
import com.example.edushare.feature.users.repository.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword;
    private Button btnCreate;
    private TextView tvRegisterLogin, tvSubLogin;

    private LocalUserRepository localUserRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        teFullname = findViewById(R.id.edt_fullname);
        teEmail = findViewById(R.id.edt_register_email);
        teStudentCode = findViewById(R.id.edt_student_code);
        tePhone = findViewById(R.id.edt_phone);
        tePassword = findViewById(R.id.edt_register_password);
        teRewritePassword = findViewById(R.id.edt_register_password_rewrite);

        btnCreate = findViewById(R.id.btn_creaedt_account);

        tvRegisterLogin = findViewById(R.id.tv_register_login);
        tvSubLogin = findViewById(R.id.tv_sub_login);

        localUserRepository = new LocalUserRepository(this);
        userRepository = new UserRepository(this);

        btnCreate.setOnClickListener(v -> {
            if (RegisterValidator.validate(teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword)) {

                String email = teEmail.getText().toString().trim();
                btnCreate.setEnabled(false); // Tránh spam click

                // Bước 1: Gọi API gửi OTP ngay tại trang Đăng Ký
                userRepository.sendOtp(new SendOtpRequest(email), new OnActionCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        btnCreate.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Mã OTP đã gửi đến Mail của bạn!", Toast.LENGTH_SHORT).show();

                        // Bước 2: Gửi OTP thành công thì mới chuyển sang trang CheckMail
                        Intent intent = new Intent(RegisterActivity.this, CheckMailActivity.class);
                        intent.putExtra("fullName", teFullname.getText().toString().trim());
                        intent.putExtra("email", email);
                        intent.putExtra("phone", tePhone.getText().toString().trim());
                        intent.putExtra("studentCode", teStudentCode.getText().toString().trim());
                        intent.putExtra("password", tePassword.getText().toString().trim());
                        intent.putExtra("confirmPassword", teRewritePassword.getText().toString().trim());
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String error) {
                        btnCreate.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Gửi OTP thất bại: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        tvRegisterLogin.setOnClickListener(v -> {
            navigateToLogin();
        });

        tvSubLogin.setOnClickListener(v -> {
            navigateToLogin();
        });

    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}
