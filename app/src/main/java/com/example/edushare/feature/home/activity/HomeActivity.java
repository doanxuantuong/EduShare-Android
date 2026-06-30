package com.example.edushare.feature.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.edushare.feature.home.repository.HomeRepository;
import com.example.edushare.feature.home.adapter.MarketplaceItemAdapter;
import com.example.edushare.feature.home.view_model.HomeViewModel;
import com.example.edushare.feature.locations.activity.MapActivity;
import com.example.edushare.feature.notification.repository.NotificationRepository;
import com.example.edushare.feature.notification.activity.NotificationActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private static final int LOAD_MORE_THRESHOLD_PX = 120;
    private static final int STAT_CARD_SELECTED_STROKE_DP = 3;
    private static final int STAT_CARD_SELECTED_ELEVATION_DP = 4;

    private MarketplaceItemAdapter itemAdapter;
    private HomeViewModel homeViewModel;
    private EditText edtHomeSearch;
    // private TextView tvItemCount;
    private TextView tvCategoryAll;
    private TextView tvCategoryTextbooks;
    private TextView tvCategoryLabEquipment;
    private TextView tvCategoryElectronics;
    private TextView tvActiveItemsCount;
    private TextView tvFreeItemsCount;
    private TextView tvSaleItemsCount;
    private MaterialCardView cardActiveItems;
    private MaterialCardView cardFreeItems;
    private MaterialCardView cardSaleItems;
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
        setupStatCardFilters();
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
        tvActiveItemsCount = findViewById(R.id.tvActiveItemsCount);
        tvFreeItemsCount = findViewById(R.id.tvFreeItemsCount);
        tvSaleItemsCount = findViewById(R.id.tvSaleItemsCount);
        cardActiveItems = findViewById(R.id.cardActiveItems);
        cardFreeItems = findViewById(R.id.cardFreeItems);
        cardSaleItems = findViewById(R.id.cardSaleItems);
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

        homeViewModel.getActiveItemCount().observe(this, count ->
                tvActiveItemsCount.setText(formatStatCount(count)));
        homeViewModel.getFreeItemCount().observe(this, count ->
                tvFreeItemsCount.setText(formatStatCount(count)));
        homeViewModel.getSaleItemCount().observe(this, count ->
                tvSaleItemsCount.setText(formatStatCount(count)));
        homeViewModel.getSelectedStatFilter().observe(this, this::updateSelectedStatCard);
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
                selectCategory(HomeViewModel.CATEGORY_TEXTBOOKS, tvCategoryTextbooks));
        tvCategoryLabEquipment.setOnClickListener(view ->
                selectCategory(HomeViewModel.CATEGORY_LAB_EQUIPMENT, tvCategoryLabEquipment));
        tvCategoryElectronics.setOnClickListener(view ->
                selectCategory(HomeViewModel.CATEGORY_ELECTRONICS, tvCategoryElectronics));
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


    private void setupStatCardFilters() {
        cardActiveItems.setOnClickListener(view -> homeViewModel.selectActiveItems());
        cardFreeItems.setOnClickListener(view -> homeViewModel.selectFreeItems());
        cardSaleItems.setOnClickListener(view -> homeViewModel.selectSaleItems());
    }


    private void updateSelectedStatCard(String selectedFilter) {
        updateStatCardStyle(cardActiveItems, HomeViewModel.STAT_FILTER_ACTIVE.equals(selectedFilter), R.color.home_stat_active_border);
        updateStatCardStyle(cardFreeItems, HomeViewModel.STAT_FILTER_FREE.equals(selectedFilter), R.color.home_stat_free_border);
        updateStatCardStyle(cardSaleItems, HomeViewModel.STAT_FILTER_SALE.equals(selectedFilter), R.color.home_stat_sale_border);
    }

    private void updateStatCardStyle(MaterialCardView card, boolean selected, int selectedStrokeColorRes) {
        card.setStrokeWidth(selected ? dpToPx(STAT_CARD_SELECTED_STROKE_DP) : 0);
        card.setStrokeColor(getColor(selectedStrokeColorRes));
        card.setCardElevation(selected ? dpToPx(STAT_CARD_SELECTED_ELEVATION_DP) : 0f);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
    private String formatStatCount(Long count) {
        return String.valueOf(count != null ? count : 0L);
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

        // Ẩn footer khi bàn phím xuất hiện
        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            android.graphics.Rect r = new android.graphics.Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                if (footerLayout != null && footerLayout.getVisibility() != View.GONE) {
                    footerLayout.setVisibility(View.GONE);
                }
            } else {
                if (footerLayout != null && footerLayout.getVisibility() != View.VISIBLE) {
                    footerLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
