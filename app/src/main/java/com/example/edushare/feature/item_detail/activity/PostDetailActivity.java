package com.example.edushare.feature.item_detail.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.chat.message.activity.ChatDetailActivity;
import com.example.edushare.feature.item_detail.adapter.TagAdapter;
import com.example.edushare.feature.item_detail.repository.PostDetailRepository;
import com.example.edushare.feature.item_detail.model.PostDetailResponseDTO;
import com.example.edushare.feature.transaction.activity.TransactionConfirmActivity;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private PostDetailRepository postDetailRepository;
    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvShortLocation, tvFreeBadge;
    private ImageView ivSellerAvatar, ivVerifyTick;
    private TextView tvSellerName, tvSellerStatus;
    private View btnMessage,btnRequestTransaction;
    private PostDetailResponseDTO postDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        postDetailRepository = new PostDetailRepository(this);

        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvShortLocation = findViewById(R.id.tvShortLocation);
        tvFreeBadge = findViewById(R.id.tvFreeBadge);

        RecyclerView rvTags = findViewById(R.id.rvTags);
        View sellerInfoLayout = findViewById(R.id.layoutSellerInfo);

        // FIX LỖI 1: Ánh xạ view an toàn và đóng ngoặc hợp lệ
        if (sellerInfoLayout != null) {
            ivSellerAvatar = sellerInfoLayout.findViewById(R.id.ivSellerAvatar);
            ivVerifyTick = sellerInfoLayout.findViewById(R.id.ivVerifyTick);
            tvSellerName = sellerInfoLayout.findViewById(R.id.tvSellerName);
            tvSellerStatus = sellerInfoLayout.findViewById(R.id.tvSellerStatus);
            btnMessage = sellerInfoLayout.findViewById(R.id.btnMessage);
        }
        btnRequestTransaction = findViewById(R.id.btnRequestTransaction);
        btnRequestTransaction.setOnClickListener(v -> {

            // Giả sử 'postDetail' là Object chứa dữ liệu bạn đã load lên giao diện chi tiết này
            if (postDetail != null) {

                // Khởi tạo Intent để chuyển sang màn hình Xác nhận
                Intent intent = new Intent(PostDetailActivity.this, TransactionConfirmActivity.class);

                // Đẩy toàn bộ thông tin cần thiết qua màn hình confirm (để bên kia đỡ phải gọi lại API)
                intent.putExtra("POST_ID", postDetail.getId()); // Gửi ID bài đăng lên Backend sau này
                intent.putExtra("POST_TITLE", postDetail.getTitle());
                intent.putExtra("POST_IMAGE", postDetail.getImageUrl());
                intent.putExtra("POST_TYPE", postDetail.getTransactionType()); // Chứa FREE, EXCHANGE, hoặc SELL
                intent.putExtra("SELLER_NAME", postDetail.getAuthorName());
                intent.putExtra("SELLER_AVATAR", postDetail.getAvatarUrl());

                // Thực hiện chuyển màn hình
                startActivity(intent);
            } else {
                Toast.makeText(PostDetailActivity.this, "Dữ liệu bài đăng chưa tải xong, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (rvTags != null) {
            rvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvTags.setAdapter(tagAdapter);
        }

        setupClickListeners();
    }

    private void loadPostDetailFromServer(int postId) {
        postDetailRepository.getPostDetail(postId, new OnActionCallback<PostDetailResponseDTO>() {
            @Override
            public void onSuccess(PostDetailResponseDTO data) {
                if (data == null) return;
                postDetail = data;
                // Đổ dữ liệu Post
                if (tvProductTitle != null) tvProductTitle.setText(data.getTitle());
                if (tvProductPrice != null) tvProductPrice.setText(data.getPrice() + "đ");
                if (tvShortLocation != null) tvShortLocation.setText(data.getLocationName());

                // Xử lý Badge loại giao dịch
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
                        case "SELL":
                        case "SALE":
                            tvFreeBadge.setBackgroundColor(android.graphics.Color.parseColor("#EF4444"));
                            break;
                        default:
                            tvFreeBadge.setBackgroundColor(android.graphics.Color.parseColor("#6B7280"));
                            break;
                    }
                } else if (tvFreeBadge != null) {
                    tvFreeBadge.setVisibility(View.GONE);
                }

                // Load hình ảnh sản phẩm
                if (data.getImageUrl() != null && !data.getImageUrl().isEmpty() && ivProductImage != null) {
                    Glide.with(PostDetailActivity.this)
                            .load(data.getImageUrl())
                            .placeholder(R.drawable.img_organic_chemistry_textbook)
                            .into(ivProductImage);
                }

                // Đổ dữ liệu Người bán (Seller)
                if (tvSellerName != null) {
                    tvSellerName.setText(data.getAuthorName() != null ? data.getAuthorName() : "Unknown");
                }

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
                        Glide.with(PostDetailActivity.this)
                                .load(data.getAvatarUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .error(R.drawable.ic_avatar)
                                .into(ivSellerAvatar);
                    } else {
                        ivSellerAvatar.setImageResource(R.drawable.ic_avatar);
                    }
                }

                String currentUserId = UserSessionHelper.getUserId(PostDetailActivity.this);

                String sellerId = data.getAuthorId();

                if (btnMessage != null) {
                    if (currentUserId.equals(sellerId)) {
                        btnMessage.setVisibility(View.GONE);
                        btnRequestTransaction.setVisibility(View.GONE);
                        // Hoặc nếu bạn muốn đổi nút Message thành nút "Sửa bài" thì làm thế này:
                        // if (btnMessage instanceof androidx.appcompat.widget.AppCompatButton) {
                        //     ((androidx.appcompat.widget.AppCompatButton) btnMessage).setText("Edit Post");
                        //     btnMessage.setVisibility(View.VISIBLE);
                        //     btnMessage.setOnClickListener(v -> { /* Mở màn hình chỉnh sửa bài viết */ });
                        // }
                    } else {
                        // Nếu là người khác xem -> Hiện nút nhắn tin bình thường
                        btnMessage.setVisibility(View.VISIBLE);
                        btnRequestTransaction.setVisibility(View.VISIBLE);
                        btnMessage.setOnClickListener(v -> {
                            Intent intent = new Intent(PostDetailActivity.this, ChatDetailActivity.class);

                            // 2. Dữ liệu giả của người bán (Lấy 1 mail có sẵn trong DB để lát nữa gửi tin nhắn thật được)
                            intent.putExtra("RECIPIENT_ID", postDetail.getAuthorId());
                            intent.putExtra("RECIPIENT_NAME", postDetail.getAuthorName());
                            intent.putExtra("SENDER_ID", UserSessionHelper.getUserId(PostDetailActivity.this));

                            // 3. Dữ liệu giả của Sản phẩm (Đồ án)
                            intent.putExtra("POST_ID", postDetail.getId());
                            intent.putExtra("POST_TITLE", postDetail.getTitle());
                            intent.putExtra("POST_IMAGE_URL", postDetail.getImageUrl());
                            String priceString = (postDetail.getPrice() != null) ? postDetail.getPrice() + "đ" : "Liên hệ";
                            intent.putExtra("POST_PRICE", priceString);

                            startActivity(intent);
                        });
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(PostDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        View headerLayout = findViewById(R.id.layoutHeader);
        if (headerLayout != null) {
            View btnBack = headerLayout.findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            } else {
                headerLayout.setOnClickListener(v -> finish());
            }
        }

        View btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v ->
                    Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show()
            );
        }
    }
}