package com.example.edushare.feature.post.helper;

import android.content.Context;

import com.example.edushare.R;
import com.example.edushare.feature.post.model.PostItemForm;

/**
 * Kiểm tra các trường bắt buộc của form Post trước khi tích hợp API.
 */
public class PostItemFormValidator {
    private final Context context;

    public PostItemFormValidator(Context context) {
        this.context = context;
    }

    /**
     * Trả về thông báo lỗi cho người dùng, hoặc null khi form hợp lệ.
     */
    public String validate(PostItemForm form) {
        if (form.getTitle().isEmpty()) {
            return context.getString(R.string.post_validation_title_required);
        }
        if (form.getCategory().isEmpty()) {
            return context.getString(R.string.post_validation_category_required);
        }
        if (form.getCondition().isEmpty()) {
            return context.getString(R.string.post_validation_condition_required);
        }
        if (context.getString(R.string.post_type_sale).equals(form.getItemType())
                && form.getPrice().isEmpty()) {
            return context.getString(R.string.post_validation_price_required);
        }
        if (form.getLocation().isEmpty()) {
            return context.getString(R.string.post_validation_location_required);
        }
        return null;
    }
}