package com.example.edushare.feature.home.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.home.model.MarketplaceItem;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketplaceItemAdapter extends RecyclerView.Adapter<MarketplaceItemAdapter.ItemViewHolder> {
    private final List<MarketplaceItem> items = new ArrayList<>();

    public MarketplaceItemAdapter(List<MarketplaceItem> items) {
        if (items != null) {
            this.items.addAll(items);
        }
    }

    public void replaceItems(List<MarketplaceItem> newItems) {
        items.clear();
        items.addAll(newItems == null ? Collections.emptyList() : newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        MarketplaceItem item = items.get(position);
        resetItemView(holder);

        String status = item.getCondition();
        if ("Hidden".equalsIgnoreCase(status)) {
            hideItemView(holder);
            return;
        }

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.ivItemPhoto.getContext())
                    .load(item.getImageUrl())
                    .placeholder(item.getImageResource())
                    .error(item.getImageResource())
                    .into(holder.ivItemPhoto);
        } else {
            holder.ivItemPhoto.setImageResource(item.getImageResource());
        }

        if ("Sold".equalsIgnoreCase(status)) {
            showSoldBadge(holder);
        }

        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemLocation.setText(item.getLocation());
        holder.tvItemPrice.setText(item.getPrice());
        holder.ivVerified.setVisibility(item.isVerified() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("POST_ID", item.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void resetItemView(ItemViewHolder holder) {
        // RecyclerView tái sử dụng view, nên phải trả card về trạng thái mặc định trước khi bind item mới.
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(layoutParams);
        }

        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(null);
        holder.ivItemPhoto.setAlpha(1.0f);
        holder.tvCondition.setVisibility(View.GONE);
        holder.tvCondition.setText("");
        holder.tvCondition.setTextColor(holder.itemView.getContext().getColor(R.color.home_text_primary));
        holder.tvCondition.setBackgroundResource(R.drawable.bg_home_condition_badge);
    }

    private void hideItemView(ItemViewHolder holder) {
        // Item bị ẩn vẫn có thể còn trong response, nên thu nhỏ view để không chiếm chỗ trên lưới.
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = 0;
            layoutParams.height = 0;
            holder.itemView.setLayoutParams(layoutParams);
        }
        holder.itemView.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(null);
    }

    private void showSoldBadge(ItemViewHolder holder) {
        int verticalPadding = (int) (2 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        int horizontalPadding = (int) (6 * holder.itemView.getContext().getResources().getDisplayMetrics().density);

        holder.tvCondition.setVisibility(View.VISIBLE);
        holder.tvCondition.setText("ĐÃ BÁN");
        holder.tvCondition.setTextColor(Color.WHITE);
        holder.tvCondition.setBackgroundColor(Color.parseColor("#EF4444"));
        holder.tvCondition.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        holder.ivItemPhoto.setAlpha(0.4f);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivItemPhoto;
        private final ImageView ivVerified;
        private final TextView tvCondition;
        private final TextView tvItemTitle;
        private final TextView tvItemLocation;
        private final TextView tvItemPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemPhoto = itemView.findViewById(R.id.ivItemPhoto);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvItemLocation = itemView.findViewById(R.id.tvItemLocation);
            tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
        }
    }
}