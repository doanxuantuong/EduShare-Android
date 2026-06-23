package com.example.edushare.feature.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.widget.SimpleTextWatcher;
import com.example.edushare.feature.auth.request.RegisterRequest;
import com.example.edushare.feature.auth.request.SendOtpRequest;
import com.example.edushare.feature.auth.request.VerifyOtpRequest;
import com.example.edushare.feature.users.UserRepository;

public class CheckMailActivity extends AppCompatActivity {

    private EditText[] codeBoxes = new EditText[6];
    private Button btnConfirm;
    private UserRepository userRepository;

    private String fullName, email, phone, studentCode, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_mail);

        userRepository = new UserRepository(this);

        // Nhận dữ liệu truyền sang
        Intent intent = getIntent();
        fullName = intent.getStringExtra("fullName");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
        studentCode = intent.getStringExtra("studentCode");
        password = intent.getStringExtra("password");
        confirmPassword = intent.getStringExtra("confirmPassword");

        if (email == null || password == null) {
            Toast.makeText(this, "Không nhận được dữ liệu đăng ký", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Ánh xạ View và xử lý auto-focus (Giữ nguyên phần code cũ của bạn)
        codeBoxes[0] = findViewById(R.id.edt_code_1);
        codeBoxes[1] = findViewById(R.id.edt_code_2);
        codeBoxes[2] = findViewById(R.id.edt_code_3);
        codeBoxes[3] = findViewById(R.id.edt_code_4);
        codeBoxes[4] = findViewById(R.id.edt_code_5);
        codeBoxes[5] = findViewById(R.id.edt_code_6);
        btnConfirm = findViewById(R.id.btn_confirm_code);

        for (int i = 0; i < codeBoxes.length - 1; i++) {
            int next = i + 1;
            int current = i;
            codeBoxes[current].addTextChangedListener(
                    new SimpleTextWatcher(() -> {
                        if (codeBoxes[current].getText().toString().length() == 1) {
                            codeBoxes[next].requestFocus();
                        }
                    })
            );
        }

        btnConfirm.setOnClickListener(v -> handleRegisterWithOtp());
    }

    // 1. Bạn khai báo thêm một biến cờ ở trên cùng của Class CheckMailActivity (cùng chỗ với các biến fullName, email...)
    private boolean isSubmitting = false;

    // 2. Cập nhật lại toàn bộ hàm xử lý khi bấm nút Xác nhận dưới đây:
    private void handleRegisterWithOtp() {
        // Nếu đang trong quá trình gửi request, lập tức bỏ qua mọi cú click tiếp theo
        if (isSubmitting) {
            return;
        }

        StringBuilder otpBuilder = new StringBuilder();
        for (EditText box : codeBoxes) {
            otpBuilder.append(box.getText().toString().trim());
        }
        String otp = otpBuilder.toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // BẬT CỜ: Đánh dấu hệ thống bắt đầu xử lý, khóa luồng gọi API
        isSubmitting = true;
        btnConfirm.setEnabled(false); // Vô hiệu hóa nút trên giao diện

        RegisterRequest registerRequest = new RegisterRequest(
                fullName, email, phone, studentCode, password, confirmPassword, otp
        );

        userRepository.register(registerRequest, new OnActionCallback<String>() {
            @Override
            public void onSuccess(String msg) {
                // Đăng ký thành công, chuẩn bị chuyển màn hình nên không cần reset cờ
                Toast.makeText(CheckMailActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                Intent loginIntent = new Intent(CheckMailActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                // HẠ CỜ + BẬT NÚT: Chỉ khi thất bại (Ví dụ: Sai OTP) mới cho phép bấm lại
                isSubmitting = false;
                btnConfirm.setEnabled(true);
                Toast.makeText(CheckMailActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
