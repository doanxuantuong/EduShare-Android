package com.example.edushare.feature.home.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edushare.R;
import com.example.edushare.feature.home.model.MarketplaceItem;
import com.example.edushare.feature.item_detail.activity.PostDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceItemAdapter extends RecyclerView.Adapter<MarketplaceItemAdapter.ItemViewHolder> {
    private final List<MarketplaceItem> items = new ArrayList<>();

    public MarketplaceItemAdapter(List<MarketplaceItem> items) {
        this.items.addAll(items);
    }

    public void replaceItems(List<MarketplaceItem> newItems) {
        int previousSize = items.size();
        items.clear();
        if (previousSize > 0) {
            notifyItemRangeRemoved(0, previousSize);
        }
        items.addAll(newItems);
        if (!newItems.isEmpty()) {
            notifyItemRangeInserted(0, newItems.size());
        }
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

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.ivItemPhoto.getContext())
                    .load(item.getImageUrl())
                    .placeholder(item.getImageResource())
                    .error(item.getImageResource())
                    .into(holder.ivItemPhoto);
        } else {
            holder.ivItemPhoto.setImageResource(item.getImageResource());
        }


        String status = item.getCondition();

        if ("Sold".equalsIgnoreCase(status)) {
            holder.tvCondition.setVisibility(View.VISIBLE);
            holder.tvCondition.setText("ĐÃ BÁN");

            holder.tvCondition.setTextColor(Color.WHITE);
            holder.tvCondition.setBackgroundColor(Color.parseColor("#EF4444")); // Màu đỏ chủ đạo thương mại điện tử

            int paddingPx = (int) (6 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            holder.tvCondition.setPadding(paddingPx, 2, paddingPx, 2);

            holder.ivItemPhoto.setAlpha(0.4f);

        } else if ("Hidden".equalsIgnoreCase(status)) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;

        } else {
            holder.tvCondition.setVisibility(View.GONE);

            holder.ivItemPhoto.setAlpha(1.0f);
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
