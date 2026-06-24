package com.example.edushare.feature.my_items.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.edushare.feature.my_items.model.MyItem;

import java.util.ArrayList;
import java.util.List;

public class MyItemAdapter extends RecyclerView.Adapter<MyItemAdapter.ViewHolder> {
    private final List<MyItem> myItems = new ArrayList<>();
    private final OnMyItemActionListener actionListener;

    public MyItemAdapter(OnMyItemActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MyItem> items) {
        myItems.clear();
        if (items != null) {
            myItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyItem item = myItems.get(position);
        Context context = holder.itemView.getContext();

        // Ảnh có thể đến từ API hoặc resource local; adapter chỉ chọn cách hiển thị phù hợp.
        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(holder.ivItemImage.getContext())
                    .load(item.getImageUrl())
                    .placeholder(item.getImageResource())
                    .error(item.getImageResource())
                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setImageResource(item.getImageResource());
        }

        holder.tvItemTitle.setText(item.getTitle());
        holder.tvItemPrice.setText(item.getPriceLabel());
        holder.tvViewCount.setText(context.getString(R.string.my_items_views_format, item.getViewCount()));
        holder.tvLikeCount.setText(context.getString(R.string.my_items_likes_format, item.getLikeCount()));

        holder.btnViewItem.setOnClickListener(view -> actionListener.onViewItem(item));
        holder.btnEditItem.setOnClickListener(view -> actionListener.onEditItem(item));
        holder.btnDeleteItem.setOnClickListener(view -> actionListener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return myItems.size();
    }

    public interface OnMyItemActionListener {
        void onViewItem(MyItem item);

        void onEditItem(MyItem item);

        void onDeleteItem(MyItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivItemImage;
        private final TextView tvItemTitle;
        private final TextView tvItemPrice;
        private final TextView tvViewCount;
        private final TextView tvLikeCount;
        private final TextView btnViewItem;
        private final TextView btnEditItem;
        private final View btnDeleteItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivMyItemImage);
            tvItemTitle = itemView.findViewById(R.id.tvMyItemTitle);
            tvItemPrice = itemView.findViewById(R.id.tvMyItemPrice);
            tvViewCount = itemView.findViewById(R.id.tvMyItemViewCount);
            tvLikeCount = itemView.findViewById(R.id.tvMyItemLikeCount);
            btnViewItem = itemView.findViewById(R.id.btnViewMyItem);
            btnEditItem = itemView.findViewById(R.id.btnEditMyItem);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteMyItem);
        }
    }
}
