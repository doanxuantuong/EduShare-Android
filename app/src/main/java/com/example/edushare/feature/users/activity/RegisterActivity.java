package com.example.edushare.feature.users.activity;

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

public class RegisterActivity extends AppCompatActivity {

    EditText teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword;
    Button btnCreate;

    private LocalUserRepository localUserRepository;

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

        btnCreate.setOnClickListener(v -> {

            localUserRepository = new LocalUserRepository(this);

            // Validate dữ liệu
            if (RegisterValidator.validate(teFullname, teEmail, teStudentCode, tePhone, tePassword,
                    teRewritePassword)) {

                // Tạo User object
                User userRequest = new User();

                userRequest.setId(teEmail.getText().toString().trim());

                userRequest.setFullName(teFullname.getText().toString().trim());

                userRequest.setEmail(teEmail.getText().toString().trim());

                userRequest.setStudentCode(teStudentCode.getText().toString().trim());

                userRequest.setPhone(tePhone.getText().toString().trim());

                // Register local bằng Room
                localUserRepository.register(userRequest, tePassword.getText().toString().trim(),
                        new OnActionCallback<String>() {

                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });
    }
}