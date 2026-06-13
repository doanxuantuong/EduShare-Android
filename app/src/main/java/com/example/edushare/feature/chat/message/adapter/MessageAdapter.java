package com.example.edushare.feature.chat.message.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.chat.message.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Message> messageList;
    private final String currentUserId;
    private final String receiverAvatarUrl;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public MessageAdapter(List<Message> messageList, String currentUserId, String receiverAvatarUrl) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.receiverAvatarUrl = receiverAvatarUrl;
    }

    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (TYPE_SENT == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if(holder.getItemViewType() == TYPE_SENT) {
            SentMessageViewHolder sentMessageViewHolder = (SentMessageViewHolder) holder;
            sentMessageViewHolder.tvMessage.setText(message.getContent());
            sentMessageViewHolder.tvTime.setText(message.getCreatedAt().toString());
        } else {
            ReceivedMessageViewHolder receivedMessageViewHolder = (ReceivedMessageViewHolder) holder;
            receivedMessageViewHolder.tvMessage.setText(message.getContent());
            receivedMessageViewHolder.tvTime.setText(message.getCreatedAt().toString());

            Glide.with(holder.itemView.getContext())
                    .load(receiverAvatarUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .circleCrop()
                    .into(receivedMessageViewHolder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        }
        return 0;
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvMessage;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvTime, tvMessage;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }

}
