package com.example.edushare.feature.transaction.activity;

import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.edushare.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TransactionHistoryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RadioGroup rgRoleSelector;
    private String currentRole = "BUYER"; // Mặc định hiển thị đơn tôi đi mua trước

    private final String[] tabTitles = {"Chờ xác nhận", "Đã duyệt", "Từ chối/Hủy"};
    private final String[] statuses = {"PENDING", "IN-PROGRESS", "SUCCESS", "REJECTED", "CANCEL"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initViews();
        setupViewPagerAndTabs();

        // Lắng nghe sự kiện đổi từ "Đơn tôi mua" sang "Đơn tôi bán"
        rgRoleSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBuyer) {
                currentRole = "BUYER";
            } else if (checkedId == R.id.rbSeller) {
                currentRole = "SELLER";
            }
            // Khởi tạo lại ViewPager để load lại danh sách theo đúng vai trò mới chọn
            setupViewPagerAndTabs();
        });
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        rgRoleSelector = findViewById(R.id.rgRoleSelector);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupViewPagerAndTabs() {
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Trả về Fragment tương ứng với Status của Tab đó và Role hiện tại
                return TransactionListFragment.newInstance(statuses[position], currentRole);
            }

            @Override
            public int getItemCount() {
                return tabTitles.length;
            }
        };

        viewPager.setAdapter(adapter);

        // Liên kết TabLayout với ViewPager2 (Cài đặt hiển thị text cho từng Tab giống Shopee)
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }
}