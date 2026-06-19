package com.example.edushare.feature.users.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edushare.R;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;

public class ProfileActivity extends AppCompatActivity {
    private View footerLayout;

    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        loadSession();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    private void loadSession() {
        tvName.setText(UserSessionHelper.getFullName(this));
        tvEmail.setText(UserSessionHelper.getEmail(this));

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