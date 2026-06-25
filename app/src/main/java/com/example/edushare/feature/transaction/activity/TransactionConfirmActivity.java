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
import com.example.edushare.feature.transaction.dto.request.TransactionRequestDTO;
import com.example.edushare.feature.transaction.repository.TransactionRepository;
import com.example.edushare.feature.transaction.view_model.TransactionViewModel;

public class TransactionConfirmActivity extends AppCompatActivity {

    // Dữ liệu nhận từ màn hình Chi tiết
    private int postId;
    private String sellerId; // ✅ ĐÃ THÊM: Hứng ID người bán để gửi lên backend
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

    // ✅ ĐÃ THÊM: ViewModel quản lý logic theo chuẩn kiến trúc MVVM
    private TransactionViewModel transactionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirm);

        // ✅ KHỞI TẠO: Khởi tạo tầng dữ liệu MVVM
        TransactionRepository repository = new TransactionRepository(this);
        transactionViewModel = new TransactionViewModel(repository);

        initViews();
        setupToolbar();
        receiveIntentData();
        setupBusinessLayout();
        setupViewModelObservers(); // ✅ ĐÃ THÊM: Lắng nghe trạng thái API trả về
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
            sellerId = intent.getStringExtra("SELLER_ID"); // ✅ ĐÃ FIX: Nhận chuẩn ID người bán từ màn hình Detail truyền sang
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
                    layoutExchangeBlock.setVisibility(View.VISIBLE);
                    layoutPaymentBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#3B82F6")); // Màu xanh dương
                    break;
                case "SELL":
                case "SALE":
                    layoutPaymentBlock.setVisibility(View.VISIBLE);
                    layoutExchangeBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#EF4444")); // Màu đỏ
                    break;
                case "FREE":
                default:
                    layoutExchangeBlock.setVisibility(View.GONE);
                    layoutPaymentBlock.setVisibility(View.GONE);
                    tvConfirmType.setBackgroundColor(android.graphics.Color.parseColor("#059669")); // Màu xanh lá
                    break;
            }
        }
    }

    // 🌟 ĐÃ THÊM: Lắng nghe phản hồi từ ViewModel phát ra ngoài UI
    private void setupViewModelObservers() {
        // Quan sát trạng thái Loading để khóa/mở nút bấm, tránh người dùng bấm liên tục (Spam)
        transactionViewModel.getLoading().observe(this, isLoading -> {
            findViewById(R.id.btnSubmitTransaction).setEnabled(!isLoading);
            findViewById(R.id.btnCancel).setEnabled(!isLoading);
            if (isLoading) {
                Toast.makeText(this, "Đang gửi yêu cầu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát lỗi phát sinh từ API hệ thống
        transactionViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        // Quan sát khi backend trả lời thành công
        transactionViewModel.getTransactionSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Gửi yêu cầu giao dịch thành công!", Toast.LENGTH_LONG).show();

                // 🚀 CHUYỂN HƯỚNG SANG MÀN HÌNH LỊCH SỬ GIAO DỊCH
                Intent intentHistory = new Intent(TransactionConfirmActivity.this, TransactionHistoryActivity.class);

                // Mẹo nhỏ: Clear hết các màn hình xác nhận trước đó để khi ở màn History bấm Back sẽ về thẳng trang chủ, không bị quay lại màn xác nhận này nữa.
                intentHistory.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intentHistory);

                finish(); // Đóng màn hình Xác nhận giao dịch hiện tại
            }
        });
    }

    // 5. Cấu hình các sự kiện Click (Hủy, Chọn đồ đổi, Gửi yêu cầu)
    private void setupClickListeners() {
        // Nút Hủy bỏ -> Thoát màn hình quay về trang cũ
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Bấm chọn bài đăng cá nhân để đem đi Đổi (Chỉ chạy khi type = EXCHANGE)
        cvSelectMyItem.setOnClickListener(v -> {
            Toast.makeText(this, "Mở danh sách kho đồ của bạn để chọn...", Toast.LENGTH_SHORT).show();
        });

        // Nút Gửi yêu cầu cuối cùng
        findViewById(R.id.btnSubmitTransaction).setOnClickListener(v -> {
            String message = edtTransactionMessage.getText().toString().trim();

            // Kiểm tra ràng buộc cho giao dịch EXCHANGE
            if (postType != null && postType.equalsIgnoreCase("EXCHANGE") && selectedMyItemId == -1) {
                Toast.makeText(this, "Vui lòng chọn một sản phẩm của bạn để trao đổi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đồng bộ dữ liệu text "SELL" thành "SALE" cho đúng cấu trúc định dạng Enum trên Backend Spring
            String finalType = postType != null ? postType.toUpperCase() : "FREE";
            if (finalType.equals("SELL")) {
                finalType = "SALE";
            }

            TransactionRequestDTO requestDTO = new TransactionRequestDTO(
                    postId,
                    sellerId,
                    finalType,
                    "PENDING" // Trạng thái mặc định ban đầu gửi lên
            );

            // ✅ ĐÃ THÊM: Ra lệnh cho ViewModel xử lý API chạy ngầm
            transactionViewModel.createTransaction(requestDTO);
        });
    }

    // Hàm phụ trợ bóc tách Phương thức thanh toán được tích chọn trong RadioGroup
    private String getSelectedPaymentMethod() {
        int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMomo) return "MOMO";
        if (checkedId == R.id.rbZaloPay) return "ZALOPAY";
        if (checkedId == R.id.rbVNPay) return "VNPAY";
        if (checkedId == R.id.rbBankTransfer) return "BANK_TRANSFER";
        return "CASH";
    }
}