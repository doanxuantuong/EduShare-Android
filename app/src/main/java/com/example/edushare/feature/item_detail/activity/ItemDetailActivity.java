package com.example.edushare.feature.item_detail.avtivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edushare.R;
import com.example.edushare.feature.item_detail.adapter.TagAdapter;
import com.example.edushare.feature.home.model.MarketplaceItem;
import java.util.ArrayList;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // 1. Hứng data từ Home chuyển sang
        MarketplaceItem item = (MarketplaceItem) getIntent().getSerializableExtra("SELECTED_ITEM");

        // 2. Ánh xạ các View
        ImageView ivProductImage = findViewById(R.id.ivProductImage);
        TextView tvProductTitle = findViewById(R.id.tvProductTitle);
        TextView tvProductPrice = findViewById(R.id.tvProductPrice);
        TextView tvShortLocation = findViewById(R.id.tvShortLocation);
        RecyclerView rvTags = findViewById(R.id.rvTags);

        // 3. Nếu có data từ Home sang thì đắp vào View (Bảo vệ app không bị crash nếu null)
        if (item != null) {
            ivProductImage.setImageResource(item.getImageResource());
            tvProductTitle.setText(item.getTitle());
            tvProductPrice.setText(item.getPrice());
            tvShortLocation.setText(item.getLocation());
        }

        // 4. Tạo data Tag giả lập (Mock Data cho Tag)
        List<String> mockTags = new ArrayList<>();
        mockTags.add("Lab Equipment");
        mockTags.add("Excellent");
        mockTags.add("Olympus");
        mockTags.add("Biology");

        // 5. Cài đặt RecyclerView hiển thị Tag cuộn ngang
        TagAdapter tagAdapter = new TagAdapter(mockTags);
        rvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTags.setAdapter(tagAdapter);

        // 6. Cài đặt các nút bấm tương tác giả lập (Mock Click)
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Nút quay lại (Header)
        findViewById(R.id.layoutHeader).findViewById(android.R.id.home).setOnClickListener(v -> finish());

        // Nút Favorite (Yêu thích) - Bấm vào đổi màu hoặc hiện thông báo
        findViewById(R.id.btnFavorite).setOnClickListener(v ->
                Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show()
        );

        // Nút Nhắn tin (Nằm trong layout_item_seller_info)
        // Lưu ý: nếu nút nhắn tin nằm trong include, bạn cần check kĩ ID của nó
        View sellerLayout = findViewById(R.id.layoutSellerInfo);
        if (sellerLayout != null) {
            sellerLayout.findViewById(R.id.btnMessage).setOnClickListener(v -> {
                // Sau này sẽ mở ConversationActivity, giờ hiện Toast trước
                Toast.makeText(this, "Opening chat with seller...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}