package com.example.edushare.feature.transaction.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.transaction.dto.response.TransactionResponseDTO;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    public interface OnTransactionActionListener {
        void onAcceptClick(int transactionId);
        void onRejectClick(int transactionId);
        void onCompleteClick(int transactionId);
        void onCancelClick(int transactionId);
    }
    private OnTransactionActionListener actionListener;
    private List<TransactionResponseDTO> transactionList;
    private String currentRole; // Biến này để biết đang ở tab "Đơn mua" hay "Đơn bán"

    public TransactionAdapter(List<TransactionResponseDTO> transactionList, String currentRole, OnTransactionActionListener onTransactionActionListener) {
        this.transactionList = transactionList;
        this.currentRole = currentRole;
        this.actionListener = onTransactionActionListener;
    }

    // Hàm này dùng để Fragment đẩy data mới vào khi gọi API xong
    public void updateData(List<TransactionResponseDTO> newList,String newRole) {
        this.currentRole = newRole;
        this.transactionList.clear();
        this.transactionList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionResponseDTO trans = transactionList.get(position);

        // 1. Gán Mã giao dịch (Tận dụng ID như đã thống nhất)
        holder.tvOrderCode.setText(String.format("Mã giao dịch: #%05d", trans.getId()));

        // 2. Gán thông tin Bài đăng (Ảnh + Tiêu đề)
        holder.tvProductTitle.setText(trans.getPostTitle() != null ? trans.getPostTitle() : "Sản phẩm không xác định");
        if (trans.getPostImage() != null && !trans.getPostImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(trans.getPostImage())
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray) // Ảnh mặc định nếu đang load
                    .into(holder.ivProductImage);
        }

        String type = trans.getTransactionType() != null ? trans.getTransactionType().toUpperCase() : "";
        switch (type) {
            case "FREE":
                holder.tvTransactionType.setText("Loại: Tặng miễn phí");
                holder.tvTransactionType.setTextColor(Color.parseColor("#10B981")); // Xanh lá
                break;
            case "SALE":
                // CHÚ Ý: Cần có trường price từ DTO
                // Giả sử DTO của ông có getPrice()
                // holder.tvTransactionType.setText("Bán: " + trans.getPrice() + " đ");
                holder.tvTransactionType.setText("Loại: Mua Bán");
                holder.tvTransactionType.setTextColor(Color.parseColor("#F5A623")); // Cam
                break;
            case "EXCHANGE":
                // CHÚ Ý: Cần có trường exchangeItem từ DTO
                // holder.tvTransactionType.setText("Trao đổi: " + trans.getExchangeItemName());
                holder.tvTransactionType.setText("Loại: Trao đổi vật phẩm");
                holder.tvTransactionType.setTextColor(Color.parseColor("#8B5CF6")); // Tím
                break;
            default:
                holder.tvTransactionType.setText("Loại: Không xác định");
                holder.tvTransactionType.setTextColor(Color.parseColor("#888888")); // Xám
                break;
        }
        // 4. Xử lý hiển thị Trạng thái (Text + Màu sắc)
        setupStatusDisplay(holder.tvStatus, trans.getStatus());
        holder.layoutActionButtons.setVisibility(View.GONE);
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        if ("PENDING".equalsIgnoreCase(trans.getStatus())) {
            if ("SELLER".equals(currentRole)) {
                // Người bán: Hiện Duyệt và Từ chối
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Chấp nhận");
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Từ chối");
            } else if ("BUYER".equals(currentRole)) {
                // Người mua: Chỉ hiện nút Hủy yêu cầu (Tận dụng nút btnReject)
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy yêu cầu");
            }
        }
        // Trường hợp đơn ĐANG XỬ LÝ (IN_PROGRESS)
        else if ("IN_PROGRESS".equalsIgnoreCase(trans.getStatus())) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            if ("BUYER".equals(currentRole)) {
                // Người mua: Hiện "Đã nhận hàng" và "Hủy giao dịch"
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Đã nhận hàng");
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy giao dịch");
            } else if ("SELLER".equals(currentRole)) {
                // Người bán: Chỉ hiện nút "Hủy giao dịch" (Tận dụng btnReject)
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy giao dịch");
            }
        }

        // 2. Bắt sự kiện click cho nút Chính (Chấp nhận HOẶC Hoàn thành)
        holder.btnAccept.setOnClickListener(v -> {
            if (actionListener != null) {
                if ("IN_PROGRESS".equalsIgnoreCase(trans.getStatus())) {
                    actionListener.onCompleteClick(trans.getId());
                } else {
                    actionListener.onAcceptClick(trans.getId());
                }
            }
        });

        // 3. Bắt sự kiện click cho nút Phụ (Từ chối HOẶC Hủy)
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                // Nếu chữ trên nút đang là Hủy thì gọi hàm Cancel, ngược lại gọi Reject của người bán
                String currentText = holder.btnReject.getText().toString();
                if (currentText.contains("Hủy")) {
                    actionListener.onCancelClick(trans.getId());
                } else {
                    actionListener.onRejectClick(trans.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    private void setupStatusDisplay(TextView tvStatus, String status) {
        if (status == null) status = "";

        switch (status.toUpperCase()) {
            case "PENDING":
                tvStatus.setText("CHỜ XÁC NHẬN");
                tvStatus.setTextColor(Color.parseColor("#F5A623")); // Cam
                break;
            case "IN_PROGRESS": // Đã sửa dấu gạch dưới
                tvStatus.setText("ĐANG XỬ LÝ");
                tvStatus.setTextColor(Color.parseColor("#3B82F6")); // Xanh dương
                break;
            case "SUCCESS": // Thêm hiển thị cho tab Thành công
                tvStatus.setText("THÀNH CÔNG");
                tvStatus.setTextColor(Color.parseColor("#10B981")); // Xanh lá
                break;
            case "REJECTED":
                tvStatus.setText("TỪ CHỐI");
                tvStatus.setTextColor(Color.parseColor("#EF4444")); // Đỏ
                break;
            case "CANCELED": // Đổi từ CANCEL thành CANCELED cho khớp Enum Backend
                tvStatus.setText("ĐÃ HỦY đơn");
                tvStatus.setTextColor(Color.parseColor("#9CA3AF")); // Xám
                break;
            default:
                tvStatus.setText("KHÔNG XÁC ĐỊNH");
                tvStatus.setTextColor(Color.parseColor("#000000"));
                break;
        }
    }

    // Class quản lý View
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvStatus, tvProductTitle, tvTransactionType;
        ImageView ivProductImage;
        LinearLayout layoutActionButtons;
        Button btnReject, btnAccept;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvTransactionType = itemView.findViewById(R.id.tvTransactionType);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);

            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}