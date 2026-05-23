package com.example.edushare.feature.users.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.RegisterValidator;
import com.example.edushare.feature.users.User;
import com.example.edushare.feature.users.UserRepository;

public class RegisterActivity extends AppCompatActivity {
    EditText teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword;
    Button btnCreate;

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

        btnCreate.setOnClickListener(v -> {
            userRepository = new UserRepository();

            //Gọi Validator để kiểm tra dữ liệu
            if (RegisterValidator.validate(teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword)) {

                //Nếu hợp lệ, tạo Object User
                User userRequest = new User();
                userRequest.setId(teEmail.getText().toString().trim());
                userRequest.setFullName(teFullname.getText().toString().trim());
                userRequest.setEmail(teEmail.getText().toString().trim());
                userRequest.setStudentCode(teStudentCode.getText().toString().trim()); // Set nếu có dữ liệu

                //Gửi lên Backend
                userRepository.register(userRequest, new OnActionCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
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
