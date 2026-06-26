package com.example.edushare.feature.auth.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.auth.request.LoginRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.users.repository.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnVerify;
    private UserRepository userRepository;
    private TextView tvSignUp, tvSubSignUp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnVerify = findViewById(R.id.btn_verify);
        tvSignUp = findViewById(R.id.tv_sign_up);
        tvSubSignUp = findViewById(R.id.tv_sub_sign_up);

        userRepository = new UserRepository(this);

        btnVerify.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);

            userRepository.login(request, new OnActionCallback<LoginResponse>() {
                @Override
                public void onSuccess(LoginResponse response) {
                    UserSessionHelper.saveSession(LoginActivity.this,
                            response.getToken(),
                            response.getUserId(),
                            response.getFullName(),
                            response.getEmail()
                    );
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });

        // icon máy ảnh nằm bên phải ô nhập liệu Email
        edtEmail.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // Chỉ số mảng đại diện cho drawableEnd/drawableRight

            if (edtEmail.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int iconWidth = edtEmail.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();

                    if (event.getRawX() >= (edtEmail.getRight() - iconWidth - edtEmail.getPaddingEnd())) {
                        Intent intent = new Intent(LoginActivity.this, OcrActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            }
            return false;
        });

        tvSignUp.setOnClickListener(v -> navigateToSignUp());
        tvSubSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}