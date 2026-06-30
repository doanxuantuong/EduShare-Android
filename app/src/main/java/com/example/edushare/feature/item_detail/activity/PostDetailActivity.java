package com.example.edushare.feature.item_detail.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.chat.message.activity.ChatDetailActivity;
import com.example.edushare.feature.item_detail.adapter.TagAdapter;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.item_detail.view_model.PostDetailViewModel;
import com.example.edushare.feature.locations.activity.MapActivity;
import com.example.edushare.feature.transaction.activity.TransactionConfirmActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private PostDetailViewModel viewModel;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvShortLocation, tvFreeBadge;
    private ImageView ivSellerAvatar, ivVerifyTick;
    private TextView tvSellerName, tvSellerStatus;
    private View btnMessage, btnRequestTransaction;
    private PostDetailResponseDTO postDetail;

    private MapView miniMap;
    private View viewMiniMapOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        initViews();
        setupViewModel();
        setupMockTags();
        setupClickListeners();

        int postId = getIntent().getIntExtra("POST_ID", -1);
        if (postId != -1) {
            viewModel.loadPostDetail(postId);
        } else {
            Toast.makeText(this, "Không nhận được ID sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvShortLocation = findViewById(R.id.tvShortLocation);
        tvFreeBadge = findViewById(R.id.tvFreeBadge);
        btnRequestTransaction = findViewById(R.id.btnRequestTransaction);
        viewMiniMapOverlay = findViewById(R.id.viewMiniMapOverlay);

        View sellerInfoLayout = findViewById(R.id.layoutSellerInfo);
        if (sellerInfoLayout != null) {
            ivSellerAvatar = sellerInfoLayout.findViewById(R.id.ivSellerAvatar);
            ivVerifyTick = sellerInfoLayout.findViewById(R.id.ivVerifyTick);
            tvSellerName = sellerInfoLayout.findViewById(R.id.tvSellerName);
            tvSellerStatus = sellerInfoLayout.findViewById(R.id.tvSellerStatus);
            btnMessage = sellerInfoLayout.findViewById(R.id.btnMessage);
        }

        miniMap = findViewById(R.id.miniMap);

        // Setup osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        File osmdroidDir = new File(getCacheDir(), "osmdroid");
        Configuration.getInstance().setOsmdroidTileCache(osmdroidDir);

        // Setup mini map
        miniMap.setTileSource(TileSourceFactory.MAPNIK);
        miniMap.setMultiTouchControls(false); // tắt gesture để không conflict scroll
        miniMap.setClickable(true);          // tắt click trên map, dùng overlay bên ngoài
        miniMap.getController().setZoom(17.0);
    }
    private void showMiniMap(double latitude, double longitude, String locationName) {
        if (miniMap == null) return;

        GeoPoint point = new GeoPoint(latitude, longitude);
        miniMap.getController().setCenter(point);

        // Đặt marker
        Marker marker = new Marker(miniMap);
        marker.setPosition(point);
        marker.setTitle(locationName);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        miniMap.getOverlays().add(marker);
        miniMap.invalidate();

        // Click vào mini map → mở MapActivity
        viewMiniMapOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this,
                    MapActivity.class);

            intent.putExtra("POST_ID", postDetail.getId());
            intent.putExtra("TARGET_LAT", latitude);
            intent.putExtra("TARGET_LNG", longitude);
            intent.putExtra("TARGET_NAME", locationName);

            startActivity(intent);
        });
    }

    private void setupViewModel() {
        // KHỞI TẠO THẲNG KHÔNG CẦN FACTORY NỮA
        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        // Lắng nghe dữ liệu bài đăng đổ về
        viewModel.getPostDetailLiveData().observe(this, data -> {
            if (data != null) {
                this.postDetail = data;
                displayPostDetail(data);
            }
        });

        // Lắng nghe thông báo lỗi
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe trạng thái Loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            // Thao tác ẩn hiện ProgressBar của ông tại đây nếu cần
        });
    }

    private void displayPostDetail(PostDetailResponseDTO data) {
        if (tvProductTitle != null) tvProductTitle.setText(data.getTitle());
        if (tvProductPrice != null) tvProductPrice.setText(data.getPrice() + "đ");
        if (tvShortLocation != null) tvShortLocation.setText(data.getLocationName());

        String type = data.getTransactionType();
        if (type != null && !type.isEmpty() && tvFreeBadge != null) {
            tvFreeBadge.setText(type.toUpperCase());
            tvFreeBadge.setVisibility(View.VISIBLE);

            switch (type.toUpperCase()) {
                case "FREE":
                    tvFreeBadge.setBackgroundColor(android.graphics.Color.parseColor("#059669"));
                    if (tvProductPrice != null) tvProductPrice.setText("Miễn phí");
                    break;
                case "EXCHANGE":
                    tvFreeBadge.setBackgroundColor(android.graphics.Color.parseColor("#3B82F6"));
                    if (tvProductPrice != null) tvProductPrice.setText("Trao đổi đồ");
                    break;
                default:
                    tvFreeBadge.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"));
                    break;
            }
        }

        if (data.getImageUrl() != null && !data.getImageUrl().isEmpty() && ivProductImage != null) {
            Glide.with(this).load(data.getImageUrl()).placeholder(R.drawable.img_organic_chemistry_textbook).into(ivProductImage);
        }

        if (tvSellerName != null) tvSellerName.setText(data.getAuthorName() != null ? data.getAuthorName() : "Unknown");

        if (ivVerifyTick != null && tvSellerStatus != null) {
            if (data.isVerified()) {
                ivVerifyTick.setVisibility(View.VISIBLE);
                tvSellerStatus.setText("Verified Student");
                tvSellerStatus.setTextColor(android.graphics.Color.parseColor("#059669"));
            } else {
                ivVerifyTick.setVisibility(View.GONE);
                tvSellerStatus.setText("Unverified Account");
                tvSellerStatus.setTextColor(android.graphics.Color.parseColor("#6B7280"));
            }
        }

        if (ivSellerAvatar != null) {
            if (data.getAvatarUrl() != null && !data.getAvatarUrl().isEmpty()) {
                Glide.with(this).load(data.getAvatarUrl()).circleCrop().placeholder(R.drawable.ic_avatar).error(R.drawable.ic_avatar).into(ivSellerAvatar);
            } else {
                ivSellerAvatar.setImageResource(R.drawable.ic_avatar);
            }
        }

        handleButtonVisibilities(data);

        // Hiện mini map nếu có tọa độ
        if (data.getLatitude() != null && data.getLongitude() != null) {
            showMiniMap(data.getLatitude(), data.getLongitude(),
                    data.getLocationName());
        }
    }

    private void handleButtonVisibilities(PostDetailResponseDTO data) {
        String currentUserId = UserSessionHelper.getUserId(this);
        String sellerId = data.getAuthorId();
        String postStatus = data.getStatus();

        if (currentUserId.equals(sellerId) || "SOLD".equalsIgnoreCase(postStatus)) {
            if (btnRequestTransaction != null) {
                btnRequestTransaction.setVisibility(View.GONE);
            }
            if (btnMessage != null) {
                btnMessage.setVisibility(currentUserId.equals(sellerId) ? View.GONE : View.VISIBLE);
            }
        } else {
            if (btnRequestTransaction != null) {
                btnRequestTransaction.setVisibility(View.VISIBLE);
            }
            if (btnMessage != null) {
                btnMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupClickListeners() {
        if (btnRequestTransaction != null) {
            btnRequestTransaction.setOnClickListener(v -> {
                if (postDetail != null) {
                    Intent intent = new Intent(PostDetailActivity.this, TransactionConfirmActivity.class);
                    intent.putExtra("POST_ID", postDetail.getId());
                    intent.putExtra("POST_TITLE", postDetail.getTitle());
                    intent.putExtra("POST_IMAGE", postDetail.getImageUrl());
                    intent.putExtra("POST_TYPE", postDetail.getTransactionType());
                    intent.putExtra("SELLER_NAME", postDetail.getAuthorName());
                    intent.putExtra("SELLER_AVATAR", postDetail.getAvatarUrl());
                    intent.putExtra("SELLER_ID", postDetail.getAuthorId());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Dữ liệu chưa tải xong!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> {
                if (postDetail != null) {
                    Intent intent = new Intent(PostDetailActivity.this, ChatDetailActivity.class);
                    intent.putExtra("RECIPIENT_ID", postDetail.getAuthorId());
                    intent.putExtra("RECIPIENT_NAME", postDetail.getAuthorName());
                    intent.putExtra("SENDER_ID", UserSessionHelper.getUserId(this));
                    intent.putExtra("POST_ID", postDetail.getId());
                    intent.putExtra("POST_TITLE", postDetail.getTitle());
                    intent.putExtra("POST_IMAGE_URL", postDetail.getImageUrl());
                    String priceString = (postDetail.getPrice() != null) ? postDetail.getPrice() + "đ" : "Liên hệ";
                    intent.putExtra("POST_PRICE", priceString);
                    startActivity(intent);
                }
            });
        }

        View headerLayout = findViewById(R.id.layoutHeader);
        if (headerLayout != null) {
            View btnBack = headerLayout.findViewById(R.id.btnBack);
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        }

        View btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupMockTags() {
        RecyclerView rvTags = findViewById(R.id.rvTags);
        List<String> mockTags = new ArrayList<>();
        mockTags.add("Lab Equipment");
        mockTags.add("Excellent");
        TagAdapter tagAdapter = new TagAdapter(mockTags);
        if (rvTags != null) {
            rvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvTags.setAdapter(tagAdapter);
        }
    }
    private void openFullMap(double latitude, double longitude, String locationName) {
        Intent intent = new Intent(
                PostDetailActivity.this,
                com.example.edushare.feature.locations.activity.MapActivity.class
        );

        intent.putExtra("POST_ID", postDetail.getId());
        intent.putExtra("TARGET_LAT", latitude);
        intent.putExtra("TARGET_LNG", longitude);
        intent.putExtra("TARGET_NAME", locationName);

        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (miniMap != null) miniMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (miniMap != null) miniMap.onPause();
    }
}