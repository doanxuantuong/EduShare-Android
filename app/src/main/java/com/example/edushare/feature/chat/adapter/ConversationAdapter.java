package com.example.edushare.feature.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.chat.model.Conversation;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversationList;

    public ConversationAdapter(List<Conversation> conversationList) {
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.tvName.setText(conversation.getPartnerName());
        holder.tvLastMessage.setText(conversation.getLastMessage());
        holder.tvTime.setText(conversation.getUpdatedAt());

        // Glide for image loading
        Glide.with(holder.itemView.getContext())
                .load(conversation.getPartnerAvatarUrl())
                .placeholder(android.R.drawable.ic_menu_report_image) // Default avatar placeholder
                .circleCrop()
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
