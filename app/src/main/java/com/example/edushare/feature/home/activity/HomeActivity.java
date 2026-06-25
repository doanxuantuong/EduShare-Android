package com.example.edushare.feature.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.home.HomeRepository;
import com.example.edushare.feature.home.adapter.MarketplaceItemAdapter;
import com.example.edushare.feature.home.view_model.HomeViewModel;
import com.example.edushare.feature.locations.activity.MapActivity;
import com.example.edushare.feature.notification.NotificationRepository;
import com.example.edushare.feature.notification.activity.NotificationActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private static final int LOAD_MORE_THRESHOLD_PX = 120;

    private MarketplaceItemAdapter itemAdapter;
    private HomeViewModel homeViewModel;
    private EditText edtHomeSearch;
    // private TextView tvItemCount;
    private TextView tvCategoryAll;
    private TextView tvCategoryTextbooks;
    private TextView tvCategoryLabEquipment;
    private TextView tvCategoryElectronics;
    private View footerLayout;
    private FrameLayout btnNotification;
    private String userId;
    private View viewUnreadDot;
    private NotificationRepository notificationRepository;
    private ProgressBar progressBar;
    private NestedScrollView homeScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupMarketplaceItems();
        initViewModel();
        observeViewModel();
        setupCategoryFilters();
        setupSearch();
        setupPaginationScroll();
        setupBottomNavigation();
        setupHeaderActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeViewModel != null) {
            homeViewModel.reloadPosts();
            homeViewModel.checkUnreadNotification(userId);
        }
    }

    private void initViews() {
        edtHomeSearch = findViewById(R.id.edtHomeSearch);
        homeScrollView = findViewById(R.id.homeScrollView);
        tvCategoryAll = findViewById(R.id.tvCategoryAll);
        tvCategoryTextbooks = findViewById(R.id.tvCategoryTextbooks);
        tvCategoryLabEquipment = findViewById(R.id.tvCategoryLabEquipment);
        tvCategoryElectronics = findViewById(R.id.tvCategoryElectronics);
        btnNotification = findViewById(R.id.btnNotification);
        userId = UserSessionHelper.getUserId(this);
        viewUnreadDot = findViewById(R.id.viewUnreadDot);

        progressBar = findViewById(R.id.progressBar);

        footerLayout = findViewById(R.id.footer_layout);

        viewUnreadDot.setVisibility(View.GONE);
    }

    private void setupMarketplaceItems() {
        RecyclerView rvMarketplaceItems = findViewById(R.id.rvMarketplaceItems);
        itemAdapter = new MarketplaceItemAdapter(new ArrayList<>());
        rvMarketplaceItems.setLayoutManager(new GridLayoutManager(this, 2));

        // RecyclerView nằm trong NestedScrollView, nên ScrollView cha chịu trách nhiệm bắt phân trang.
        rvMarketplaceItems.setNestedScrollingEnabled(false);
        rvMarketplaceItems.setAdapter(itemAdapter);
    }

    private void initViewModel() {
        HomeRepository homeRepository = new HomeRepository(this);
        NotificationRepository notificationRepository = new NotificationRepository(this);

        homeViewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new HomeViewModel(homeRepository, notificationRepository))
        ).get(HomeViewModel.class);
    }

    private void observeViewModel() {
        homeViewModel.getMarketplaceItems().observe(this, items -> itemAdapter.replaceItems(items));

        homeViewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        homeViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        homeViewModel.getHasUnreadNotification().observe(this, hasUnread ->
                viewUnreadDot.setVisibility(Boolean.TRUE.equals(hasUnread) ? View.VISIBLE : View.GONE));
    }

    private void setupHeaderActions() {
        btnNotification.setOnClickListener(view ->
                startActivity(new Intent(this, NotificationActivity.class)));

        findViewById(R.id.btnLocation).setOnClickListener(view ->
                startActivity(new Intent(this, MapActivity.class)));
    }

    private void setupPaginationScroll() {
        homeScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (scrollView, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY <= oldScrollY || scrollView.getChildCount() == 0) {
                return;
            }

            View contentView = scrollView.getChildAt(0);
            int distanceToBottom = contentView.getMeasuredHeight() - scrollView.getMeasuredHeight() - scrollY;
            if (distanceToBottom <= LOAD_MORE_THRESHOLD_PX) {
                homeViewModel.loadNextPage();
            }
        });
    }

    private void setupCategoryFilters() {
        tvCategoryAll.setOnClickListener(view -> selectCategory("All", tvCategoryAll));
        tvCategoryTextbooks.setOnClickListener(view ->
                selectCategory(getString(R.string.home_category_textbooks), tvCategoryTextbooks));
        tvCategoryLabEquipment.setOnClickListener(view ->
                selectCategory(getString(R.string.home_category_lab_equipment), tvCategoryLabEquipment));
        tvCategoryElectronics.setOnClickListener(view ->
                selectCategory(getString(R.string.home_category_electronics), tvCategoryElectronics));
    }

    private void selectCategory(String category, TextView selectedView) {
        resetCategoryStyle(tvCategoryAll);
        resetCategoryStyle(tvCategoryTextbooks);
        resetCategoryStyle(tvCategoryLabEquipment);
        resetCategoryStyle(tvCategoryElectronics);
        selectedView.setBackgroundResource(R.drawable.bg_home_category_active);
        selectedView.setTextColor(getColor(R.color.white));
        homeViewModel.selectCategory(category);
    }

    private void resetCategoryStyle(TextView categoryView) {
        categoryView.setBackgroundResource(R.drawable.bg_home_category_inactive);
        categoryView.setTextColor(getColor(R.color.home_text_primary));
    }

    private void setupSearch() {
        edtHomeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                homeViewModel.updateSearchQuery(text.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupBottomNavigation() {
        // Truyền cả cụm footerLayout và làm sáng nút Home ở thanh điều hướng dưới.
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_home);
    }
}
