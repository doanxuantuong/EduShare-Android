package com.example.edushare.feature.home.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.feature.home.HomeRepository;
import com.example.edushare.feature.home.adapter.MarketplaceItemAdapter;
import com.example.edushare.feature.home.model.MarketplaceItem;
import com.example.edushare.feature.home.model.PostListItemResponse;
import com.example.edushare.feature.locations.activity.MapActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private final List<MarketplaceItem> allItems = new ArrayList<>();
    private MarketplaceItemAdapter itemAdapter;
    private HomeRepository homeRepository;
    private EditText edtHomeSearch;
//    private TextView tvItemCount;
    private TextView tvCategoryAll;
    private TextView tvCategoryTextbooks;
    private TextView tvCategoryLabEquipment;
    private TextView tvCategoryElectronics;
    private String selectedCategory = "All";
    private View footerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeRepository = new HomeRepository(this);
        initViews();
        setupMarketplaceItems();
        setupCategoryFilters();
        setupSearch();
        setupBottomNavigation();
        fetchMarketplaceItems();
    }

    private void initViews() {
        edtHomeSearch = findViewById(R.id.edtHomeSearch);
//        tvItemCount = findViewById(R.id.tvItemCount);
        tvCategoryAll = findViewById(R.id.tvCategoryAll);
        tvCategoryTextbooks = findViewById(R.id.tvCategoryTextbooks);
        tvCategoryLabEquipment = findViewById(R.id.tvCategoryLabEquipment);
        tvCategoryElectronics = findViewById(R.id.tvCategoryElectronics);

        footerLayout = findViewById(R.id.footer_layout);

        findViewById(R.id.btnNotification).setOnClickListener(view ->
                Toast.makeText(this, R.string.home_notifications, Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnLocation).setOnClickListener(view ->
                startActivity(new Intent(this, MapActivity.class)));
    }
    private void setupMarketplaceItems() {
        RecyclerView rvMarketplaceItems = findViewById(R.id.rvMarketplaceItems);
        itemAdapter = new MarketplaceItemAdapter(allItems);
        rvMarketplaceItems.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMarketplaceItems.setAdapter(itemAdapter);
    }

    private void fetchMarketplaceItems() {
        homeRepository.getPosts(new OnActionCallback<List<PostListItemResponse>>() {
            @Override
            public void onSuccess(List<PostListItemResponse> data) {
                allItems.clear();
                for (PostListItemResponse post : data) {
                    allItems.add(toMarketplaceItem(post));
                }
                filterItems();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private MarketplaceItem toMarketplaceItem(PostListItemResponse post) {
        String category = safeText(post.getCategoryName(), "All");
        String location = safeText(post.getLocationName(), "Unknown location");
        String status = formatStatus(post.getStatus());
        String title = safeText(post.getTitle(), "Untitled item");

        return new MarketplaceItem(
                post.getId(),
                R.drawable.img_organic_chemistry_textbook,
                post.getImageUrl(),
                status,
                title,
                location,
                formatPrice(post.getPrice()),
                category,
                !safeText(post.getAuthorName(), "").isEmpty()
        );
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Available";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("SOLD".equals(normalized)) {
            return "Sold";
        }
        if ("HIDDEN".equals(normalized)) {
            return "Hidden";
        }
        return "Available";
    }

    private String formatPrice(Double price) {
        if (price == null || price <= 0) {
            return "Contact";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
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
        selectedCategory = category;
        resetCategoryStyle(tvCategoryAll);
        resetCategoryStyle(tvCategoryTextbooks);
        resetCategoryStyle(tvCategoryLabEquipment);
        resetCategoryStyle(tvCategoryElectronics);
        selectedView.setBackgroundResource(R.drawable.bg_home_category_active);
        selectedView.setTextColor(getColor(R.color.white));
        filterItems();
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
                filterItems();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void filterItems() {
        String query = edtHomeSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<MarketplaceItem> filteredItems = new ArrayList<>();

        for (MarketplaceItem item : allItems) {
            boolean matchesCategory = "All".equals(selectedCategory)
                    || selectedCategory.equals(item.getCategory());
            boolean matchesQuery = query.isEmpty()
                    || item.getTitle().toLowerCase(Locale.ROOT).contains(query);

            if (matchesCategory && matchesQuery) {
                filteredItems.add(item);
            }
        }

        itemAdapter.replaceItems(filteredItems);
//        tvItemCount.setText(getResources().getQuantityString(
//                R.plurals.home_item_count_format,
//                filteredItems.size(),
//                filteredItems.size()
//        ));
    }

    private void setupBottomNavigation() {
        // Truyền cả cụm footerLayout và làm sáng đèn nút Home (R.id.nav_home)
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_home);
    }
}
