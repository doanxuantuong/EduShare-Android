package com.example.edushare.feature.notification.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.common.utils.DateTimeUtils;
import com.example.edushare.feature.notification.dto.NotificationResponseDto;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<NotificationResponseDto> notificationList = new ArrayList<>();
    private OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationResponseDto notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<NotificationResponseDto> list) {
        notificationList.clear();

        if (list != null) {
            notificationList.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        NotificationResponseDto notificationResponseDto = notificationList.get(position);

        holder.tvNotificationTitle.setText(notificationResponseDto.getTitle());
        holder.tvNotificationContent.setText(notificationResponseDto.getContent());

        if (notificationResponseDto.getRead()) {
            holder.viewUnreadDot.setVisibility(View.GONE);
        } else {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
        }

        holder.tvNotificationTime.setText(DateTimeUtils.getRelativeTime(notificationResponseDto.getCreatedAt()));

        Glide.with(holder.itemView.getContext())
                .load(notificationResponseDto.getAvatarUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNotificationClick(notificationResponseDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvNotificationTitle, tvNotificationContent, tvNotificationTime;
        View viewUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationContent = itemView.findViewById(R.id.tvNotificationContent);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
