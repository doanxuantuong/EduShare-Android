package com.example.edushare.feature.home.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.home.HomeRepository;
import com.example.edushare.feature.home.model.MarketplaceItem;
import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;
import com.example.edushare.feature.notification.NotificationRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends ViewModel {
    private static final int PAGE_SIZE = 10;
    private static final String ALL_CATEGORY = "All";

    private final HomeRepository homeRepository;
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<MarketplaceItem>> marketplaceItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasUnreadNotification = new MutableLiveData<>();

    private final List<MarketplaceItem> loadedItems = new ArrayList<>();
    private int currentPage = 0;
    private int requestVersion = 0;
    private boolean isLastPage = false;
    private boolean isFetching = false;
    private String selectedCategory = ALL_CATEGORY;
    private String searchQuery = "";

    public HomeViewModel(HomeRepository homeRepository, NotificationRepository notificationRepository) {
        this.homeRepository = homeRepository;
        this.notificationRepository = notificationRepository;
    }

    public MutableLiveData<List<MarketplaceItem>> getMarketplaceItems() {
        return marketplaceItems;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getHasUnreadNotification() {
        return hasUnreadNotification;
    }

    public void reloadPosts() {
        requestVersion++;
        currentPage = 0;
        isLastPage = false;
        isFetching = false;
        loadedItems.clear();
        marketplaceItems.setValue(new ArrayList<>());
        loadNextPage();
    }

    public void loadNextPage() {
        if (isLastPage || isFetching) {
            return;
        }

        isFetching = true;
        loading.setValue(loadedItems.isEmpty());

        int loadingPage = currentPage;
        int loadingVersion = requestVersion;
        String categoryFilter = toApiCategory(selectedCategory);
        String keywordFilter = toApiKeyword(searchQuery);

        homeRepository.getPosts(loadingPage, PAGE_SIZE, categoryFilter, keywordFilter,
                new OnActionCallback<PageResponse<PostListItemResponse>>() {
                    @Override
                    public void onSuccess(PageResponse<PostListItemResponse> data) {
                        if (loadingVersion != requestVersion) {
                            return;
                        }

                        isFetching = false;
                        loading.postValue(false);

                        List<PostListItemResponse> posts = data != null ? data.getContent() : null;
                        if (posts == null || posts.isEmpty()) {
                            isLastPage = true;
                            publishLoadedItems();
                            return;
                        }

                        for (PostListItemResponse post : posts) {
                            loadedItems.add(toMarketplaceItem(post));
                        }

                        isLastPage = data.isLast() || posts.size() < PAGE_SIZE;
                        currentPage = loadingPage + 1;
                        publishLoadedItems();
                    }

                    @Override
                    public void onFailure(String message) {
                        if (loadingVersion != requestVersion) {
                            return;
                        }

                        isFetching = false;
                        loading.postValue(false);
                        errorMessage.postValue(message);
                    }
                });
    }

    public void selectCategory(String category) {
        String nextCategory = safeText(category, ALL_CATEGORY);
        if (nextCategory.equals(selectedCategory)) {
            return;
        }
        selectedCategory = nextCategory;
        reloadPosts();
    }

    public void updateSearchQuery(String query) {
        String nextQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (nextQuery.equals(searchQuery)) {
            return;
        }
        searchQuery = nextQuery;
        reloadPosts();
    }

    public void checkUnreadNotification(String userId) {
        if (userId == null || userId.isEmpty()) {
            hasUnreadNotification.setValue(false);
            return;
        }

        notificationRepository.checkUnread(userId, new OnActionCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasUnread) {
                hasUnreadNotification.postValue(Boolean.TRUE.equals(hasUnread));
            }

            @Override
            public void onFailure(String message) {
                hasUnreadNotification.postValue(false);
            }
        });
    }

    private void publishLoadedItems() {
        marketplaceItems.postValue(new ArrayList<>(loadedItems));
    }

    private MarketplaceItem toMarketplaceItem(PostListItemResponse post) {
        String category = safeText(post.getCategoryName(), ALL_CATEGORY);
        String location = safeText(post.getLocationName(), "Unknown location");
        String status = formatStatus(post.getStatus());
        String title = safeText(post.getTitle(), "Untitled item");
        int postId = post.getId() != null ? post.getId() : -1;

        return new MarketplaceItem(
                postId,
                R.drawable.img_organic_chemistry_textbook,
                post.getImageUrl(),
                status,
                title,
                location,
                formatPrice(post.getPrice()),
                category,
                safeText(post.getAuthorName(), "").length() > 0
        );
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String toApiCategory(String category) {
        if (category == null || ALL_CATEGORY.equalsIgnoreCase(category.trim())) {
            return null;
        }
        return category.trim();
    }

    private String toApiKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }

    private String formatStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Available";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("SOLD".equals(normalized)) {
            return "Sold";
        }
        if ("HIDDEN".equals(normalized)) {
            return "Hidden";
        }
        return "Available";
    }

    private String formatPrice(Double price) {
        if (price == null || price <= 0) {
            return "Contact";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }
}
