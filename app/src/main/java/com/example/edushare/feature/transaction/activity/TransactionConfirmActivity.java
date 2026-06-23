package com.example.edushare.feature.transaction.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;

public class TransactionConfirmActivity extends AppCompatActivity {

    // Dữ liệu nhận từ màn hình Chi tiết
    private int postId;
    private String postTitle, postImage, postType, sellerName, sellerAvatar;

    // Các View hiển thị thông tin chung
    private ImageView ivConfirmImage, ivConfirmSellerAvatar;
    private TextView tvConfirmTitle, tvConfirmType, tvConfirmSellerName;
    private EditText edtTransactionMessage;

    // Các khối điều khiển nghiệp vụ (Ẩn/Hiện linh hoạt)
    private LinearLayout layoutExchangeBlock;
    private LinearLayout layoutPaymentBlock;
    private CardView cvSelectMyItem;
    private TextView tvSelectedExchangeItem;
    private RadioGroup rgPaymentMethod;

    // ID bài viết cá nhân mang đi đổi (Nếu có chọn đồ đổi)
    private int selectedMyItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirm);

        initViews();
        setupToolbar();
        receiveIntentData();
        setupBusinessLayout();
        setupClickListeners();
    }

    // 1. Ánh xạ toàn bộ View từ XML sang Java
    private void initViews() {
        ivConfirmImage = findViewById(R.id.ivConfirmImage);
        tvConfirmTitle = findViewById(R.id.tvConfirmTitle);
        tvConfirmType = findViewById(R.id.tvConfirmType);
        ivConfirmSellerAvatar = findViewById(R.id.ivConfirmSellerAvatar);
        tvConfirmSellerName = findViewById(R.id.tvConfirmSellerName);
        edtTransactionMessage = findViewById(R.id.edtTransactionMessage);

        // Các khối riêng biệt cho Exchange và Sale
        layoutExchangeBlock = findViewById(R.id.layoutExchangeBlock);
        layoutPaymentBlock = findViewById(R.id.layoutPaymentBlock);
        cvSelectMyItem = findViewById(R.id.cvSelectMyItem);
        tvSelectedExchangeItem = findViewById(R.id.tvSelectedExchangeItem);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
    }

    // 2. Thiết lập thanh Toolbar và nút Quay lại (Back)
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarConfirm);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác nhận giao dịch");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // 3. Nhận dữ liệu truyền sang từ màn hình PostDetailActivity
    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            postId = intent.getIntExtra("POST_ID", -1);
            postTitle = intent.getStringExtra("POST_TITLE");
            postImage = intent.getStringExtra("POST_IMAGE");
            postType = intent.getStringExtra("POST_TYPE");
            sellerName = intent.getStringExtra("SELLER_NAME");
            sellerAvatar = intent.getStringExtra("SELLER_AVATAR");

            // Đổ dữ liệu chung lên UI
            tvConfirmTitle.setText(postTitle != null ? postTitle : "Sản phẩm không xác định");
            tvConfirmType.setText(postType != null ? postType.toUpperCase() : "FREE");
            tvConfirmSellerName.setText(sellerName != null ? sellerName : "Người bán ẩn danh");

            if (postImage != null && !postImage.isEmpty()) {
                Glide.with(this).load(postImage).into(ivConfirmImage);
            }
            if (sellerAvatar != null && !sellerAvatar.isEmpty()) {
                Glide.with(this).load(sellerAvatar).circleCrop().into(ivConfirmSellerAvatar);
            }
        }
    }

    // 4. Kiểm tra Type để cấu hình giao diện biến hình thông minh
    private void setupBusinessLayout() {
        if (postType != null) {
            switch (postType.toUpperCase()) {
                case "EXCHANGE":
                    // Hiện phần chọn đồ đổi, ẩn phần thanh toán tiền
                    layoutExchangeBlock.setVisibility(View.VISIBLE);
                    layoutPaymentBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#3B82F6")); // Màu xanh dương
                    break;
                case "SELL":
                case "SALE":
                    // Hiện phần chọn MoMo, VNPAY,... ẩn phần đổi đồ
                    layoutPaymentBlock.setVisibility(View.VISIBLE);
                    layoutExchangeBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#EF4444")); // Màu đỏ
                    break;
                case "FREE":
                default:
                    // Đồ Free thì ẩn sạch cả 2 khối cho tinh gọn
                    layoutExchangeBlock.setVisibility(View.GONE);
                    layoutPaymentBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#059669")); // Màu xanh lá
                    break;
            }
        }
    }

    // 5. Cấu hình các sự kiện Click (Hủy, Chọn đồ đổi, Gửi yêu cầu)
    private void setupClickListeners() {
        // Nút Hủy bỏ -> Thoát màn hình quay về trang cũ
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Bấm chọn bài đăng cá nhân để đem đi Đổi (Chỉ chạy khi type = EXCHANGE)
        cvSelectMyItem.setOnClickListener(v -> {
            Toast.makeText(this, "Mở danh sách kho đồ của bạn để chọn...", Toast.LENGTH_SHORT).show();
            // TODO: Bạn có thể mở một Activity hoặc BottomSheet hiển thị danh sách Post của chính User tại đây.
            // Ví dụ sau khi chọn xong, bạn cập nhật lại:
            // selectedMyItemId = bài_viết_được_chọn.getId();
            // tvSelectedExchangeItem.setText(bài_viết_được_chọn.getTitle());
        });

        // Nút Gửi yêu cầu cuối cùng
        findViewById(R.id.btnSubmitTransaction).setOnClickListener(v -> {
            String message = edtTransactionMessage.getText().toString().trim();

            // Kiểm tra ràng buộc cho từng loại giao dịch cụ thể trước khi gửi đi
            if (postType != null && postType.equalsIgnoreCase("EXCHANGE") && selectedMyItemId == -1) {
                // Bạn có thể bật thông báo bắt buộc chọn đồ đổi nếu muốn bảo mật chặt chẽ hơn:
                // Toast.makeText(this, "Vui lòng chọn một sản phẩm của bạn để trao đổi!", Toast.LENGTH_SHORT).show();
                // return;
            }

            // Đọc phương thức thanh toán được chọn (Chỉ quan trọng khi type = SELL)
            String selectedPayment = getSelectedPaymentMethod();

            // TODO: Gọi API Repository/Retrofit tạo giao dịch tại đây
            // Các tham số truyền đi: postId (bài người bán), selectedMyItemId (bài người mua đổi - nếu có), selectedPayment (PTTT), message (lời nhắn)

            String logResult = "Gửi thành công! Type: " + postType + " | Bài viết ID: " + postId + " | PTTT: " + selectedPayment;
            Toast.makeText(this, logResult, Toast.LENGTH_LONG).show();

            finish(); // Hoặc chuyển sang màn hình thông báo thành công / màn chat với người bán.
        });
    }

    // Hàm phụ trợ bóc tách Phương thức thanh toán được tích chọn trong RadioGroup
    private String getSelectedPaymentMethod() {
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMomo) return "MOMO";
        if (checkedId == R.id.rbZaloPay) return "ZALOPAY";
        if (checkedId == R.id.rbVNPay) return "VNPAY";
        if (checkedId == R.id.rbBankTransfer) return "BANK_TRANSFER";
        return "CASH"; // Mặc định là tiền mặt (rbCash)
    }
}