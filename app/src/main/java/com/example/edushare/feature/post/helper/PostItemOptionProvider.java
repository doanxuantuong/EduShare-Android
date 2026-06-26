package com.example.edushare.feature.post.helper;

import android.content.Context;
import android.content.res.Resources;

import com.example.edushare.R;

/**
 * Cung cấp dữ liệu lựa chọn từ resources để Activity không hardcode dữ liệu UI.
 */
public class PostItemOptionProvider {
    private final Resources resources;
    private final String[] categoryOptions;
    private final String[] categoryApiValues;

    public PostItemOptionProvider(Context context) {
        this.resources = context.getResources();
        this.categoryOptions = resources.getStringArray(R.array.post_category_options);
        this.categoryApiValues = resources.getStringArray(R.array.post_category_api_values);
    }

    public String[] getCategoryOptions() {
        return categoryOptions;
    }

    public String getCategoryApiValue(String displayValue) {
        for (int index = 0; index < categoryOptions.length && index < categoryApiValues.length; index++) {
            if (categoryOptions[index].equals(displayValue)) {
                return categoryApiValues[index];
            }
        }
        return displayValue;
    }

    public String getCategoryDisplayValue(String apiValue) {
        for (int index = 0; index < categoryOptions.length && index < categoryApiValues.length; index++) {
            if (categoryApiValues[index].equalsIgnoreCase(apiValue)
                    || categoryOptions[index].equalsIgnoreCase(apiValue)) {
                return categoryOptions[index];
            }
        }
        return apiValue;
    }

    public String[] getLocationOptions() {
        return resources.getStringArray(R.array.post_location_options);
    }
}