package com.example.edushare.feature.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.feature.home.model.MarketplaceItem;

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
        holder.ivItemPhoto.setImageResource(item.getImageResource());
        holder.tvCondition.setText(item.getCondition());
        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemLocation.setText(item.getLocation());
        holder.tvItemPrice.setText(item.getPrice());
        holder.ivVerified.setVisibility(item.isVerified() ? View.VISIBLE : View.GONE);
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
