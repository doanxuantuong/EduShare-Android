package com.example.edushare.feature.locations.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edushare.feature.locations.model.ItemLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho AutoCompleteTextView — hiện gợi ý khi user gõ tên đồ vật.
 * Lọc theo name HOẶC areaName, không phân biệt hoa thường.
 */
public class SearchSuggestionAdapter extends ArrayAdapter<ItemLocation> {

    private final List<ItemLocation> allItems;
    private List<ItemLocation> filteredItems;
    private final LayoutInflater inflater;

    public SearchSuggestionAdapter(@NonNull Context context, List<ItemLocation> items) {
        super(context, android.R.layout.simple_dropdown_item_1line, items);
        this.allItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Nullable
    @Override
    public ItemLocation getItem(int position) {
        return filteredItems.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        ItemLocation item = filteredItems.get(position);
        TextView text = view.findViewById(android.R.id.text1);
        // Hiện "Laptop • Dorm A"
        text.setText(item.getName() + "  •  " + item.getAreaName());
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = allItems;
                    results.count = allItems.size();
                } else {
                    String kw = constraint.toString().toLowerCase().trim();
                    List<ItemLocation> matched = new ArrayList<>();
                    for (ItemLocation item : allItems) {
                        if (item.getName().toLowerCase().contains(kw)
                                || item.getAreaName().toLowerCase().contains(kw)) {
                            matched.add(item);
                        }
                    }
                    results.values = matched;
                    results.count = matched.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems = (List<ItemLocation>) results.values;
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((ItemLocation) resultValue).getName();
            }
        };
    }

    /** Cập nhật toàn bộ danh sách (khi filter chip thay đổi) */
    public void updateData(List<ItemLocation> newItems) {
        allItems.clear();
        allItems.addAll(newItems);
        filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }
}