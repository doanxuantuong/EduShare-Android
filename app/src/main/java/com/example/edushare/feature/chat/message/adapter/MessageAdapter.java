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
import com.example.edushare.common.utils.DateTimeUtils;
import com.example.edushare.feature.chat.message.dto.MessageResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<MessageResponse> messageList;
    private final String currentUserId;
    private final String recipientAvatarUrl;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public MessageAdapter(List<MessageResponse> messageList, String currentUserId, String recipientAvatarUrl) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.recipientAvatarUrl = recipientAvatarUrl;
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
        // Lấy ra tin nhắn hiện tại
        MessageResponse currentMessage = messageList.get(position);
        boolean isLastInGroup = isLastMessageInGroup(position);
        boolean showTimeSeparator = shouldShowTimeSeparator(position);
        boolean isLastMessage = isLastMessage(position);

        // Nếu là người gửi
        if (holder.getItemViewType() == TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.tvMessage.setText(currentMessage.getContent());

            // Thêm thanh phân tách
            handleTimeSeparator(sentHolder.tvTimeSeparator, showTimeSeparator, currentMessage.getCreatedAt());
            // Thêm thời gian tin nhắn
            handleMessageTime(sentHolder.tvTime, isLastInGroup, currentMessage.getCreatedAt());

            handleMessageStatus(sentHolder.tvStatus, isLastMessage);

            //Nếu là người nhận
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.tvMessage.setText(currentMessage.getContent());

            // Thêm thanh phân tách
            handleTimeSeparator(receivedHolder.tvTimeSeparator, showTimeSeparator, currentMessage.getCreatedAt());
            // Thêm thời gian tin nhắn
            handleMessageTime(receivedHolder.tvTime, isLastInGroup, currentMessage.getCreatedAt());

            if (isLastInGroup) {
                Glide.with(holder.itemView.getContext())
                        .load(recipientAvatarUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .circleCrop()
                        .into(receivedHolder.ivAvatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        }
        return 0;
    }

    // ==============================
    // HANDLE TIME IN UI
    // ==============================
    // Xử lý hiển thị thanh phân tách thời gian
    private void handleTimeSeparator(TextView tvTimeSeparator, boolean show, String timeAt) {
        if (show && tvTimeSeparator != null) {
            tvTimeSeparator.setVisibility(View.VISIBLE);
            tvTimeSeparator.setText(DateTimeUtils.getSeparatorTime(timeAt));
        } else if (tvTimeSeparator != null) {
            tvTimeSeparator.setVisibility(View.GONE);
        }
    }

    //Xử lý hiển thị thời gian của tin nhắn
    private void handleMessageTime(TextView tvTime, boolean isLastInGroup, String timeAt) {
        if (isLastInGroup) {
            tvTime.setVisibility(View.VISIBLE);
            tvTime.setText(DateTimeUtils.getHourTime(timeAt));
        } else {
            tvTime.setVisibility(View.GONE);
        }
    }

    // Xử lý hiển thị trạng thái tin nhắn cuối
    private void handleMessageStatus(TextView tvStatus, boolean isLastInGroup) {
        if (isLastInGroup) {
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(showMessageStatus(messageList.size() - 1));
        } else {
            tvStatus.setVisibility(View.GONE);
        }
    }

    // ==============================
    // HANDLE LOGIN TIME SHOW
    // ==============================
    // Kiểm tra xem cần hiển thị thanh phân tách thời gian hay không
    private boolean shouldShowTimeSeparator(int position) {
        if (position == 0) return true;

        MessageResponse currentMessage = messageList.get(position);
        MessageResponse previousMessage = messageList.get(position - 1);

        return DateTimeUtils.isTimeDifferenceSignificant(currentMessage.getCreatedAt(), previousMessage.getCreatedAt());
    }

    // Kiểm tra xem tin nhắn hiện tại có phải là tin nhắn cuối cùng trong nhóm không
    private boolean isLastMessageInGroup(int position) {
        if (position == messageList.size() - 1) {
            return true;
        }

        MessageResponse currentMessage = messageList.get(position);
        MessageResponse nextMessage = messageList.get(position + 1);

        return !currentMessage.getSenderId().equals(nextMessage.getSenderId());
    }

    // Nó có phải tin nhắn cuối không
    private boolean isLastMessage(int position) {
        return position == messageList.size() - 1;
    }

    //Hiển thị trạng thái tin nhắn cuối (Đã gửi, đã xem)
    private String showMessageStatus(int position) {
        if (position == 0) return "";
        MessageResponse currentMessage = messageList.get(position);

        if (currentMessage.getRead()) {
            return "Đã xem";
        } else {
            return "Đã gửi";
        }
    }


    // ==============================
    // VIEW HOLDER
    // ==============================
    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvMessage, tvTimeSeparator, tvStatus;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimeSeparator = itemView.findViewById(R.id.tvTimeSeparator);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvTime, tvMessage, tvTimeSeparator;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimeSeparator = itemView.findViewById(R.id.tvTimeSeparator);
        }
    }

}
