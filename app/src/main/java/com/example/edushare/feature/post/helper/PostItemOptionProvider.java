package com.example.edushare.feature.post.helper;

import android.content.Context;
import android.content.res.Resources;

import com.example.edushare.R;

/**
 * Cung cấp dữ liệu lựa chọn từ resources để Activity không hardcode dữ liệu UI.
 */
public class PostItemOptionProvider {
    private final Resources resources;

    public PostItemOptionProvider(Context context) {
        this.resources = context.getResources();
    }

    public String[] getCategoryOptions() {
        return resources.getStringArray(R.array.post_category_options);
    }

    public String[] getLocationOptions() {
        return resources.getStringArray(R.array.post_location_options);
    }
}