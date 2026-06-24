package com.example.edushare.feature.transaction.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edushare.R;

// BẮT BUỘC phải có "extends AppCompatActivity" ở đây nha ông
public class TransactionHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nạp file giao diện XML mà anh em mình vừa tạo ở bước trước vào đây
        setContentView(R.layout.activity_transaction_history);

        // Đoạn này xử lý nút quay lại (Back)
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}