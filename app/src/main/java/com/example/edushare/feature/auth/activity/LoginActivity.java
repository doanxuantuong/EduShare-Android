package com.example.edushare.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.auth.request.LoginRequest;
import com.example.edushare.feature.auth.response.LoginResponse;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.users.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnVerify;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnVerify = findViewById(R.id.btn_verify);

        userRepository = new UserRepository(this);

        // nút đăng nhập
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

        // nút ocr
        Button btnOcr = findViewById(R.id.btn_ocr);
        btnOcr.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, OcrActivity.class);
            startActivity(intent);
        });

    }
}



