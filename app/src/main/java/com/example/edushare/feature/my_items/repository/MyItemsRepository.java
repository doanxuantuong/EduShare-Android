package com.example.edushare.feature.my_items.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.common.base.retrofit.RetrofitClient;
import com.example.edushare.common.helper.UserSessionHelper;
import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;
import com.example.edushare.feature.my_items.api.MyItemsApi;
import com.example.edushare.feature.my_items.model.MyItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyItemsRepository {
    private static final int FIRST_PAGE = 0;
    private static final int PAGE_SIZE = 100;

    private final Context context;
    private final MyItemsApi myItemsApi;

    public MyItemsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.myItemsApi = RetrofitClient.createService(MyItemsApi.class, context);
    }

    public void getMyItems(OnActionCallback<List<MyItem>> callback) {
        String userId = UserSessionHelper.getUserId(context);
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("Please login again to view your posts");
            return;
        }

        myItemsApi.getMyPosts(userId, FIRST_PAGE, PAGE_SIZE).enqueue(new Callback<PageResponse<PostListItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<PostListItemResponse>> call,
                                   @NonNull Response<PageResponse<PostListItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(toMyItems(response.body().getContent()));
                    return;
                }
                callback.onFailure("Cannot load your posts. Server code: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<PostListItemResponse>> call,
                                  @NonNull Throwable t) {
                callback.onFailure("Cannot connect to post service");
            }
        });
    }

    private List<MyItem> toMyItems(List<PostListItemResponse> posts) {
        List<MyItem> items = new ArrayList<>();
        if (posts == null) {
            return items;
        }

        for (PostListItemResponse post : posts) {
            items.add(new MyItem(
                    post.getId() != null ? post.getId() : -1,
                    R.drawable.img_organic_chemistry_textbook,
                    post.getImageUrl(),
                    safeText(post.getTitle(), "Untitled item"),
                    formatPrice(post.getPrice()),
                    0,
                    0,
                    isSold(post.getStatus())
            ));
        }
        return items;
    }

    private boolean isSold(String status) {
        return status != null && "SOLD".equalsIgnoreCase(status.trim());
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String formatPrice(Double price) {
        if (price == null || price <= 0) {
            return "Contact";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }
}