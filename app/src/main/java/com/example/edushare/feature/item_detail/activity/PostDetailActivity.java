package com.example.edushare.feature.item_detail.activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback; // Thêm import này
import com.example.edushare.feature.item_detail.adapter.TagAdapter;
import com.example.edushare.feature.item_detail.repository.PostDetailRepository;

import java.util.ArrayList;
import java.util.List;

import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
public class PostDetailActivity extends AppCompatActivity {
    private PostDetailRepository postDetailRepository;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvShortLocation;

    @Override // Bổ sung @Override cho đúng vòng đời Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        postDetailRepository = new PostDetailRepository(this);

        // ĐÃ SỬA: Bỏ kiểu dữ liệu ở đầu để gán trực tiếp vào các biến Global đã khai báo trên kia
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvShortLocation = findViewById(R.id.tvShortLocation);
        RecyclerView rvTags = findViewById(R.id.rvTags);

        int postId = getIntent().getIntExtra("POST_ID", -1);

        if (postId != -1) {
            loadPostDetailFromServer(postId);
        } else {
            Toast.makeText(this, "Không nhận được ID sản phẩm!", Toast.LENGTH_SHORT).show();
        }

        List<String> mockTags = new ArrayList<>();
        mockTags.add("Lab Equipment");
        mockTags.add("Excellent");
        TagAdapter tagAdapter = new TagAdapter(mockTags);
        rvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTags.setAdapter(tagAdapter);

        setupClickListeners();
    } // ĐÃ SỬA: Xóa bỏ bớt một dấu đóng ngoặc dư thừa gây lỗi bẻ đôi Class ở đây

    private void loadPostDetailFromServer(int postId) {
        postDetailRepository.getPostDetail(postId, new OnActionCallback<PostDetailResponseDTO>() {
            @Override // Bổ sung @Override cho chuẩn Interface Anonymous Class
            public void onSuccess(PostDetailResponseDTO data) {
                // Đổ dữ liệu thật từ API Server trả về lên giao diện
                tvProductTitle.setText(data.getTitle());
                tvProductPrice.setText(data.getPrice() + "đ");
                tvShortLocation.setText(data.getLocationName());

                // Load hình ảnh từ URL server bằng Glide
                if (data.getImageUrl() != null && !data.getImageUrl().isEmpty()) {
                    Glide.with(PostDetailActivity.this)
                            .load(data.getImageUrl())
                            .placeholder(R.drawable.img_organic_chemistry_textbook)
                            .into(ivProductImage);
                }

                // Tọa độ vĩ độ/kinh độ sẵn sàng cho bạn xử lý tiếp (ví dụ truyền sang MapActivity)
                Double lat = data.getLatitude();
                Double lng = data.getLongitude();
            }

            @Override // Bổ sung @Override
            public void onFailure(String message) {
                Toast.makeText(PostDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Cách 1: Nếu layoutHeader của bạn chứa một nút quay lại tùy biến (Ví dụ đặt tên ID nút là btnBack)
        View headerLayout = findViewById(R.id.layoutHeader);
        if (headerLayout != null) {
            // Hãy kiểm tra lại ID chính xác của nút bấm quay lại trong XML của bạn (thay R.id.btnBack nếu khác)
            View btnBack = headerLayout.findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            } else {
                // Nếu không tìm thấy nút cụ thể, cho phép click vào nguyên vùng header để quay lại tạm thời
                headerLayout.setOnClickListener(v -> finish());
            }
        }

        // Nút Favorite (Yêu thích) - Kiểm tra null trước khi gán sự kiện
        View btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v ->
                    Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show()
            );
        }

        // Nút Nhắn tin
        View sellerLayout = findViewById(R.id.layoutSellerInfo);
        if (sellerLayout != null) {
            View btnMessage = sellerLayout.findViewById(R.id.btnMessage);
            if (btnMessage != null) {
                btnMessage.setOnClickListener(v -> {
                    Toast.makeText(this, "Opening chat with seller...", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
