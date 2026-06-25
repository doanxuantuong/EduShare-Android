package com.example.edushare.feature.my_items.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.view_model.BaseViewModelFactory;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;
import com.example.edushare.feature.my_items.MyItemsRepository;
import com.example.edushare.feature.my_items.adapter.MyItemAdapter;
import com.example.edushare.feature.my_items.model.MyItem;
import com.example.edushare.feature.my_items.view_model.MyItemsViewModel;
import com.example.edushare.feature.post.activity.PostItemActivity;

public class MyItemsActivity extends AppCompatActivity implements MyItemAdapter.OnMyItemActionListener {
    private RecyclerView rvMyItems;
    private MyItemAdapter myItemAdapter;
    private MyItemsViewModel myItemsViewModel;
    private TextView tvActiveTab;
    private TextView tvSoldTab;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private View btnAddItem;
    private View footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);

        initViews();
        initRecyclerView();
        initViewModel();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myItemsViewModel != null) {
            myItemsViewModel.loadItems();
        }
    }

    private void initViews() {
        rvMyItems = findViewById(R.id.rvMyItems);
        tvActiveTab = findViewById(R.id.tvActiveItemsTab);
        tvSoldTab = findViewById(R.id.tvSoldItemsTab);
        tvEmptyState = findViewById(R.id.tvMyItemsEmptyState);
        progressBar = findViewById(R.id.progressBar);
        btnAddItem = findViewById(R.id.btnAddMyItem);
        footerLayout = findViewById(R.id.footer_layout);

        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_profile);
    }

    private void initRecyclerView() {
        myItemAdapter = new MyItemAdapter(this);
        rvMyItems.setLayoutManager(new LinearLayoutManager(this));
        rvMyItems.setAdapter(myItemAdapter);
    }

    private void initViewModel() {
        MyItemsRepository repository = new MyItemsRepository(this);
        myItemsViewModel = new ViewModelProvider(this, new BaseViewModelFactory<>(
                () -> new MyItemsViewModel(repository))
        ).get(MyItemsViewModel.class);
    }

    private void setupClickListeners() {
        // Nút cộng dùng lại màn Post hiện có để người dùng đăng thêm item từ My Items.
        btnAddItem.setOnClickListener(view -> {
            Intent intent = new Intent(MyItemsActivity.this, PostItemActivity.class);
            startActivity(intent);
        });

        tvActiveTab.setOnClickListener(view -> myItemsViewModel.showActiveItems());
        tvSoldTab.setOnClickListener(view -> myItemsViewModel.showSoldItems());
    }

    private void observeViewModel() {
        myItemsViewModel.getDisplayedItems().observe(this, items -> {
            myItemAdapter.setData(items);
            tvEmptyState.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        myItemsViewModel.getActiveItemCount().observe(this, count ->
                tvActiveTab.setText(getString(R.string.my_items_active_tab_format, count)));

        myItemsViewModel.getSoldItemCount().observe(this, count ->
                tvSoldTab.setText(getString(R.string.my_items_sold_tab_format, count)));

        myItemsViewModel.getShowingSoldItems().observe(this, this::updateTabStyle);

        myItemsViewModel.getErrorMessage().observe(this, message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show());

        myItemsViewModel.getLoading().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                tvEmptyState.setVisibility(View.GONE);
            }
        });
    }

    private void updateTabStyle(Boolean showingSoldItems) {
        boolean isSoldSelected = Boolean.TRUE.equals(showingSoldItems);

        if (isSoldSelected) {
            tvActiveTab.setBackgroundColor(Color.TRANSPARENT);
            tvSoldTab.setBackgroundResource(R.drawable.bg_my_items_segment_active);
        } else {
            tvActiveTab.setBackgroundResource(R.drawable.bg_my_items_segment_active);
            tvSoldTab.setBackgroundColor(Color.TRANSPARENT);
        }

        tvActiveTab.setTextColor(getColor(isSoldSelected ? R.color.my_items_text_secondary : R.color.my_items_text_primary));
        tvSoldTab.setTextColor(getColor(isSoldSelected ? R.color.my_items_text_primary : R.color.my_items_text_secondary));
    }

    @Override
    public void onViewItem(MyItem item) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", item.getId());
        startActivity(intent);
    }

    @Override
    public void onEditItem(MyItem item) {
        Toast.makeText(this, R.string.my_items_edit_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteItem(MyItem item) {
        Toast.makeText(this, R.string.my_items_delete_coming_soon, Toast.LENGTH_SHORT).show();
    }
}
