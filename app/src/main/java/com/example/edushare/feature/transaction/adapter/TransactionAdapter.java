package com.example.edushare.feature.transaction.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.chat.message.activity.ChatDetailActivity;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;
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
    private String currentRole;

    public TransactionAdapter(List<TransactionResponseDTO> transactionList, String currentRole, OnTransactionActionListener onTransactionActionListener) {
        this.transactionList = transactionList;
        this.currentRole = currentRole;
        this.actionListener = onTransactionActionListener;
    }

    public void updateData(List<TransactionResponseDTO> newList, String newRole) {
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

        Context context = holder.itemView.getContext();
        String currentUserId = UserSessionHelper.getUserId(context);

        if (currentUserId != null && currentUserId.equals(trans.getSellerId())) {
            holder.tvPartnerName.setText("Người mua: " + (trans.getBuyerName() != null ? trans.getBuyerName() : "Ẩn danh"));

            Glide.with(context)
                    .load(trans.getBuyerAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.ivPartnerAvatar);
        } else {
            holder.tvPartnerName.setText("Người bán: " + (trans.getSellerName() != null ? trans.getSellerName() : "Unknown"));

            Glide.with(context)
                    .load(trans.getSellerAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
                    .into(holder.ivPartnerAvatar);
        }

        // Gán Mã giao dịch
        holder.tvOrderCode.setText(String.format("Mã giao dịch: #%05d", trans.getId()));

        // Gán thông tin Bài đăng (Ảnh + Tiêu đề)
        holder.tvProductTitle.setText(trans.getPostTitle() != null ? trans.getPostTitle() : "Sản phẩm không xác định");

        Glide.with(context)
                .load(trans.getPostImage())
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(holder.ivProductImage);

        // --- Xử lý loại giao dịch & giá tiền bài viết ---
        String type = trans.getTransactionType() != null ? trans.getTransactionType().toUpperCase() : "";
        switch (type) {
            case "FREE":
                holder.tvTransactionType.setText("Loại: Tặng miễn phí");
                holder.tvTransactionType.setTextColor(Color.parseColor("#10B981")); // Xanh lá
                break;
            case "SALE":
                // 🔥 Sửa lỗi ghi đè text: Gom chung lại thành một chuỗi duy nhất để hiển thị số tiền!
                String priceString = (trans.getPrice() != null) ? trans.getPrice().intValue() + " đ" : "Liên hệ";
                holder.tvTransactionType.setText("Bán: " + priceString);
                holder.tvTransactionType.setTextColor(Color.parseColor("#F5A623")); // Cam
                break;
            case "EXCHANGE":
                holder.tvTransactionType.setText("Loại: Trao đổi vật phẩm");
                holder.tvTransactionType.setTextColor(Color.parseColor("#8B5CF6")); // Tím
                break;
            default:
                holder.tvTransactionType.setText("Loại: Không xác định");
                holder.tvTransactionType.setTextColor(Color.parseColor("#888888")); // Xám
                break;
        }

        // Xử lý hiển thị Trạng thái (Text + Màu sắc)
        setupStatusDisplay(holder.tvStatus, trans.getStatus());

        // Reset trạng thái các nút trước khi check điều kiện nhằm tránh bug hiển thị sai khi scroll biệt lập
        holder.layoutActionButtons.setVisibility(View.GONE);
        holder.btnAccept.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);

        if ("PENDING".equalsIgnoreCase(trans.getStatus())) {
            if ("SELLER".equals(currentRole)) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Chấp nhận");
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Từ chối");
            } else if ("BUYER".equals(currentRole)) {
                holder.layoutActionButtons.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy yêu cầu");
            }
        }
        else if ("IN_PROGRESS".equalsIgnoreCase(trans.getStatus())) {
            holder.layoutActionButtons.setVisibility(View.VISIBLE);
            if ("BUYER".equals(currentRole)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Đã nhận hàng");
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy giao dịch");
            } else if ("SELLER".equals(currentRole)) {
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText("Hủy giao dịch");
            }
        }

        // Bắt sự kiện click nút Chấp nhận / Đã nhận hàng
        holder.btnAccept.setOnClickListener(v -> {
            if (actionListener != null) {
                if ("IN_PROGRESS".equalsIgnoreCase(trans.getStatus())) {
                    actionListener.onCompleteClick(trans.getId());
                } else {
                    actionListener.onAcceptClick(trans.getId());
                }
            }
        });

        // Bắt sự kiện click nút Từ chối / Hủy đơn
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                String currentText = holder.btnReject.getText().toString();
                if (currentText.contains("Hủy")) {
                    actionListener.onCancelClick(trans.getId());
                } else {
                    actionListener.onRejectClick(trans.getId());
                }
            }
        });

        // Bắt sự kiện click nút Nhắn tin -> Chuyển sang ChatDetailActivity
        holder.btnChat.setOnClickListener(v -> {

            // Xác định đối tác (partner) là buyer hay seller dựa vào user hiện tại
            String partnerId;
            String partnerName;
            String partnerAvatar;
            if (currentUserId != null && currentUserId.equals(trans.getSellerId())) {
                // Mình là seller -> đối tác là buyer
                partnerId = trans.getBuyerId();
                partnerName = trans.getBuyerName() != null ? trans.getBuyerName() : "Ẩn danh";
                partnerAvatar = trans.getBuyerAvatar();
            } else {
                // Mình là buyer -> đối tác là seller
                partnerId = trans.getSellerId();
                partnerName = trans.getSellerName() != null ? trans.getSellerName() : "Ẩn danh";
                partnerAvatar = trans.getSellerAvatar();
            }

            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("CONVERSATION_ID", -1); // Sẽ tự tìm hoặc tạo mới conversation
            intent.putExtra("RECIPIENT_ID", partnerId);
            intent.putExtra("RECIPIENT_NAME", partnerName);
            intent.putExtra("RECIPIENT_AVATAR", partnerAvatar);
            intent.putExtra("POST_ID", trans.getPostId());
            intent.putExtra("POST_TITLE", trans.getPostTitle());
            intent.putExtra("POST_IMAGE_URL", trans.getPostImage());
            context.startActivity(intent);
        });

        // Bắt sự kiện click nút Chi tiết -> Xem chi tiết bài đăng của giao dịch
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("POST_ID", trans.getPostId());
            context.startActivity(intent);
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
                tvStatus.setTextColor(Color.parseColor("#F5A623"));
                break;
            case "IN_PROGRESS":
                tvStatus.setText("ĐANG XỬ LÝ");
                tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                break;
            case "SUCCESS":
                tvStatus.setText("THÀNH CÔNG");
                tvStatus.setTextColor(Color.parseColor("#10B981"));
                break;
            case "REJECTED":
                tvStatus.setText("TỪ CHỐI");
                tvStatus.setTextColor(Color.parseColor("#EF4444"));
                break;
            case "CANCELED":
                tvStatus.setText("ĐÃ HỦY đơn");
                tvStatus.setTextColor(Color.parseColor("#9CA3AF"));
                break;
            default:
                tvStatus.setText("KHÔNG XÁC ĐỊNH");
                tvStatus.setTextColor(Color.parseColor("#000000"));
                break;
        }
    }

    // Class quản lý View
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvStatus, tvProductTitle, tvTransactionType, tvPartnerName;
        ImageView ivProductImage, ivPartnerAvatar;
        LinearLayout layoutActionButtons;
        Button btnReject, btnAccept, btnChat, btnDetail;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvTransactionType = itemView.findViewById(R.id.tvTransactionType);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);

            // 🔥 Đã bổ sung findViewById cho các View đối tác mới thêm từ file XML
            tvPartnerName = itemView.findViewById(R.id.tvPartnerName);
            ivPartnerAvatar = itemView.findViewById(R.id.ivPartnerAvatar);

            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}