package com.example.edushare.feature.chat.conversation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.chat.conversation.model.Conversation;

import java.util.List;

/*
* Adapter:
* Dùng để load danh sách động có thể thay đổi liên tujc
* */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversationList;

    public ConversationAdapter(List<Conversation> conversationList) {
        this.conversationList = conversationList;
    }

    /*
     * Bước 1:
     * - lấy file item_chat.xml (khuôn mẫu cho 1 dòng tin nhắn)
     * - "thổi phồng" (inflate) nó lên thành một đối tượng View có thể chạm vào được trong Java
     * */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    /*
     * Bước 2:
     * - Cái list conversationList truyền vào giống như xô mực. Adapter lôi từng tin nhắn ra,
     * - Lấy tên (getPartnerName()), nội dung (getLastMessage())
     * - Dán vào các <TextView> tương ứng trên cái khuôn vừa tạo.
     * - Dùng thư viện Glide để tải hình ảnh
     * */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.tvName.setText(conversation.getPartnerName());
        holder.tvLastMessage.setText(conversation.getLastMessage());
        holder.tvTime.setText(conversation.getUpdatedAt());

        // Glide for image loading
        // Glide.with(...) giống như một thẻ <img> của HTML nhưng xịn hơn.
        Glide.with(holder.itemView.getContext())
                //Đưa nó một URL (đường dẫn ảnh),
                .load(conversation.getPartnerAvatarUrl())
                //Ảnh mặc định
                .placeholder(android.R.drawable.ic_menu_report_image) // Default avatar placeholder
                //Nó tự tải ảnh từ mạng về, tự bóp méo thành hình tròn (circleCrop())
                .circleCrop()
                //Gắn vào cái khung ảnh (ivAvatar).
                .into(holder.ivAvatar);

        // Optional: Badge logic (showing it for some items as requested)
        if (position % 2 == 0) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText(String.valueOf(position + 1));
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    //ViewHolder sinh ra để bạn không phải đi "tìm kiếm" giao diện nhiều lần,
    // giúp ứng dụng chạy mượt mà dù danh sách có dài đến vô tận.
    // Lưu lại các địa chỉ để chèn data
    // Khi muốn load thêm chỉ cần lấy các địa chỉ đã lưu để load chứ không phải đi kiếm nữa
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Hàm này CHỈ CHẠY 1 LẦN lúc mới tạo ra cái hộp
            // Tìm và lưu giữ chúng lại ngay lập tức
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
