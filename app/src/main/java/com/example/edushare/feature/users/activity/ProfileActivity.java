package com.example.edushare.feature.users.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.helper.BottomNavigationHelper;

public class ProfileActivity extends AppCompatActivity {
    private View footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo và thiết lập cụm điều hướng dùng chung
        initViews();
    }

    private void initViews() {
        // Ánh xạ thẻ include từ file activity_profile.xml
        footerLayout = findViewById(R.id.footer_layout);

        // Thiết lập sáng đèn cho icon Profile (R.id.nav_profile)
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_profile);
    }
}