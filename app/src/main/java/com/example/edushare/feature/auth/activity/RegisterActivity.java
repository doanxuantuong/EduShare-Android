package com.example.edushare.feature.auth.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private final ActivityResultLauncher<Intent> ocrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String studentCode = result.getData().getStringExtra("STUDENT_CODE");
                    String fullName = result.getData().getStringExtra("FULL_NAME");

                    // Tự động điền dữ liệu quét được vào Form đăng ký
                    if (studentCode != null) teStudentCode.setText(studentCode);
                    if (fullName != null) teFullname.setText(fullName);

                    // Tự sinh email dựa trên mã số sinh viên quét được
                    if (studentCode != null) {
                        teEmail.setText(studentCode + "@st.hcmuaf.edu.vn");
                    }
                }
            }
    );

    @SuppressLint("ClickableViewAccessibility")
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

        // Bắt sự kiện click vào icon Máy ảnh ở ô MSSV để kích hoạt mở OCR quét thẻ
        teStudentCode.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (teStudentCode.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int iconWidth = teStudentCode.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    if (event.getRawX() >= (teStudentCode.getRight() - iconWidth - teStudentCode.getPaddingEnd())) {

                        // Sử dụng ocrLauncher để hứng dữ liệu trả về sau khi quét
                        Intent intent = new Intent(RegisterActivity.this, OcrActivity.class);
                        ocrLauncher.launch(intent);
                        return true;
                    }
                }
            }
            // Vì ta cấu hình focusable=false, nên click vào bất kỳ vùng nào của ô này cũng có thể mở OCR cho tiện
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Intent intent = new Intent(RegisterActivity.this, OcrActivity.class);
                ocrLauncher.launch(intent);
                return true;
            }
            return false;
        });

        btnCreate.setOnClickListener(v -> {
            if (RegisterValidator.validate(teFullname, teEmail, teStudentCode, tePhone, tePassword, teRewritePassword)) {
                String email = teEmail.getText().toString().trim();
                btnCreate.setEnabled(false);

                userRepository.sendOtp(new SendOtpRequest(email), new OnActionCallback<String>() {
                    @Override
                    public void onSuccess(String message) {
                        btnCreate.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Mã OTP đã gửi đến Mail của bạn!", Toast.LENGTH_SHORT).show();

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

        tvRegisterLogin.setOnClickListener(v -> navigateToLogin());
        tvSubLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}