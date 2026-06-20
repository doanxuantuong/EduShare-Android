package com.example.edushare.feature.auth.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.RegisterValidator;
import com.example.edushare.feature.users.LocalUserRepository;
import com.example.edushare.feature.users.User;
import com.example.edushare.feature.users.UserRepository;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.auth.response.RegisterResponse;

public class RegisterActivity extends AppCompatActivity {

    EditText teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword;
    Button btnCreate;

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

        localUserRepository = new LocalUserRepository(this);
        userRepository = new UserRepository(this);

        btnCreate.setOnClickListener(v -> {
            if (RegisterValidator.validate(teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword)) {

                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setFullName(teFullname.getText().toString().trim());
                registerRequest.setEmail(teEmail.getText().toString().trim());
                registerRequest.setStudentCode(teStudentCode.getText().toString().trim());
                registerRequest.setPhone(tePhone.getText().toString().trim());
                registerRequest.setPassword(tePassword.getText().toString().trim());
                registerRequest.setConfirmPassword(teRewritePassword.getText().toString().trim());


                // Gọi BE để đăng ký
                userRepository.register(registerRequest, new OnActionCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Sau khi đăng ký thành công BE, lưu local bằng Room
                        localUserRepository.register(registerRequest, tePassword.getText().toString().trim(),
                                new OnActionCallback<String>() {
                                    @Override
                                    public void onSuccess(String msg) {
                                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
