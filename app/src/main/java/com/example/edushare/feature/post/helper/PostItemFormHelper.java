package com.example.edushare.feature.post.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.post.activity.PickLocationActivity;
import com.example.edushare.feature.post.model.CreatePostRequest;
import com.example.edushare.feature.post.model.PostItemForm;
import com.example.edushare.feature.post.model.PostResponse;
import com.example.edushare.feature.post.repository.PostRepository;

/**
 * Gắn các view của màn Post với hành vi chỉ liên quan đến UI.
 * Gọi API, xử lý upload và lưu dữ liệu nên được tách sang class riêng.
 */
public class PostItemFormHelper {
    private final Activity activity;
    private final PostItemFormValidator validator;
    private final PostItemOptionProvider optionProvider;
    private final PostRepository postRepository;

    private TextView tvTypeSale;
    private TextView tvTypeFree;
    private TextView tvCategory;
    private TextView tvLocation;
    private TextView tvConditionNew;
    private TextView tvConditionLikeNew;
    private TextView tvConditionGood;
    private TextView tvConditionFair;
    private TextView tvConditionPoor;
    private TextView btnSubmitPost;
    private EditText edtTitle;
    private EditText edtPrice;
    private EditText edtDescription;

    // Lưu giá trị đã chọn riêng với text trên view để buildForm() có nguồn dữ liệu rõ ràng.
    private String selectedItemType;
    private String selectedCategory = "";
    private String selectedCondition = "";
    private String selectedLocation = "";

    private int selectedLocationId = -1;
    private boolean isSubmitting = false;

    public PostItemFormHelper(Activity activity) {
        this.activity = activity;
        this.validator = new PostItemFormValidator(activity);
        this.optionProvider = new PostItemOptionProvider(activity);
        this.postRepository = new PostRepository(activity);
        this.selectedItemType = activity.getString(R.string.post_type_sale);
    }

    public void setup() {
        bindViews();
        setupBackButton();
        setupPhotoAction();
        setupItemTypeActions();
        setupConditionActions();
        setupSelectActions();
        setupSubmitAction();
    }

    private void bindViews() {
        tvTypeSale = activity.findViewById(R.id.tvTypeSale);
        tvTypeFree = activity.findViewById(R.id.tvTypeFree);
        tvCategory = activity.findViewById(R.id.tvPostCategory);
        tvLocation = activity.findViewById(R.id.tvPostLocation);
        tvConditionNew = activity.findViewById(R.id.tvConditionNew);
        tvConditionLikeNew = activity.findViewById(R.id.tvConditionLikeNew);
        tvConditionGood = activity.findViewById(R.id.tvConditionGood);
        tvConditionFair = activity.findViewById(R.id.tvConditionFair);
        tvConditionPoor = activity.findViewById(R.id.tvConditionPoor);
        btnSubmitPost = activity.findViewById(R.id.btnSubmitPost);
        edtTitle = activity.findViewById(R.id.edtPostTitle);
        edtPrice = activity.findViewById(R.id.edtPostPrice);
        edtDescription = activity.findViewById(R.id.edtPostDescription);
    }

    private void setupBackButton() {
        activity.findViewById(R.id.btnPostBack).setOnClickListener(view -> activity.finish());
    }

    private void setupPhotoAction() {
        activity.findViewById(R.id.layoutAddPhoto).setOnClickListener(view ->
                Toast.makeText(activity, R.string.post_photo_picker_message, Toast.LENGTH_SHORT).show());
    }

    private void setupItemTypeActions() {
        tvTypeSale.setOnClickListener(view -> selectItemType(activity.getString(R.string.post_type_sale)));
        tvTypeFree.setOnClickListener(view -> selectItemType(activity.getString(R.string.post_type_free)));
    }

    private void selectItemType(String itemType) {
        selectedItemType = itemType;
        boolean saleSelected = activity.getString(R.string.post_type_sale).equals(itemType);
        updateTypeStyle(tvTypeSale, saleSelected);
        updateTypeStyle(tvTypeFree, !saleSelected);
    }

    private void updateTypeStyle(TextView view, boolean isSelected) {
        view.setBackgroundResource(isSelected
                ? R.drawable.bg_post_option_active
                : R.drawable.bg_post_option_inactive);
        view.setTextColor(isSelected ? Color.WHITE : activity.getColor(R.color.post_text_secondary));
    }

    private void setupConditionActions() {
        tvConditionNew.setOnClickListener(view -> selectCondition(tvConditionNew));
        tvConditionLikeNew.setOnClickListener(view -> selectCondition(tvConditionLikeNew));
        tvConditionGood.setOnClickListener(view -> selectCondition(tvConditionGood));
        tvConditionFair.setOnClickListener(view -> selectCondition(tvConditionFair));
        tvConditionPoor.setOnClickListener(view -> selectCondition(tvConditionPoor));
    }

    private void selectCondition(TextView selectedView) {
        selectedCondition = selectedView.getText().toString();
        resetConditionStyle(tvConditionNew);
        resetConditionStyle(tvConditionLikeNew);
        resetConditionStyle(tvConditionGood);
        resetConditionStyle(tvConditionFair);
        resetConditionStyle(tvConditionPoor);
        selectedView.setBackgroundResource(R.drawable.bg_post_chip_active);
        selectedView.setTextColor(Color.WHITE);
    }

    private void resetConditionStyle(TextView view) {
        view.setBackgroundResource(R.drawable.bg_post_chip_inactive);
        view.setTextColor(activity.getColor(R.color.post_text_secondary));
    }

    private void setupSelectActions() {
        tvCategory.setOnClickListener(view -> showCategoryMenu());
        tvLocation.setOnClickListener(view -> showLocationMenu());
    }

    private void showCategoryMenu() {
        showMenu(tvCategory, optionProvider.getCategoryOptions(), value -> {
            selectedCategory = value;
            tvCategory.setText(value);
        });
    }

    private void showLocationMenu() {
        Intent intent = new Intent(activity, PickLocationActivity.class);
        activity.startActivityForResult(intent, 200);
    }
    public void onLocationPicked(int locationId, String locationName) {
        selectedLocationId = locationId;
        selectedLocation = locationName;
        tvLocation.setText(locationName);
    }

    private void showMenu(TextView anchor, String[] values, OnMenuValueSelected listener) {
        // Dùng chung cho các trường chọn một giá trị để tránh lặp code tạo PopupMenu.
        PopupMenu popupMenu = new PopupMenu(activity, anchor);
        Menu menu = popupMenu.getMenu();
        for (String value : values) {
            menu.add(value);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            listener.onSelected(item.getTitle().toString());
            return true;
        });
        popupMenu.show();
    }

    private void setupSubmitAction() {
        btnSubmitPost.setOnClickListener(view -> submitForm());
    }

    private void submitForm() {
        if (isSubmitting) {
            return;
        }

        PostItemForm form = buildForm();
        String error = validator.validate(form);
        if (error != null) {
            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true);
        postRepository.createPost(toCreatePostRequest(form), new OnActionCallback<PostResponse>() {
            @Override
            public void onSuccess(PostResponse data) {
                setSubmitting(false);
                Toast.makeText(activity, R.string.post_submit_success, Toast.LENGTH_SHORT).show();
                activity.finish();
            }

            @Override
            public void onFailure(String error) {
                setSubmitting(false);
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        btnSubmitPost.setEnabled(!submitting);
        btnSubmitPost.setText(submitting ? R.string.post_submit_loading : R.string.post_submit);
    }

    private CreatePostRequest toCreatePostRequest(PostItemForm form) {
        String transactionType = activity.getString(R.string.post_type_sale).equals(form.getItemType())
                ? "SALE"
                : "FREE";
        return new CreatePostRequest(
                form.getTitle(),
                form.getDescription(),
                form.getCondition(),
                parsePrice(form, transactionType),
                null,
                form.getCategory(),
                form.getLocationId(),
                form.getItemType(),
                transactionType
        );
    }

    private Double parsePrice(PostItemForm form, String transactionType) {
        if (!"SALE".equals(transactionType)) {
            return 0D;
        }
        String rawPrice = form.getPrice().trim();
        if (rawPrice.isEmpty()) {
            return null;
        }
        return Double.parseDouble(rawPrice);
    }

    private PostItemForm buildForm() {
        return new PostItemForm(
                selectedItemType,
                edtTitle.getText().toString().trim(),
                selectedCategory,
                selectedCondition,
                edtPrice.getText().toString().trim(),
                selectedLocationId,
                selectedLocation,
                edtDescription.getText().toString().trim()
        );
    }

    private interface OnMenuValueSelected {
        void onSelected(String value);
    }
}