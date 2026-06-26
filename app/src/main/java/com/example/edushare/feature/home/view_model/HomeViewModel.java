package com.example.edushare.feature.home.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.R;
import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.home.repository.HomeRepository;
import com.example.edushare.feature.home.model.MarketplaceItem;
import com.example.edushare.feature.home.model.PageResponse;
import com.example.edushare.feature.home.model.PostListItemResponse;
import com.example.edushare.feature.notification.repository.NotificationRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends ViewModel {
    public static final String CATEGORY_TEXTBOOKS = "Textbooks";
    public static final String CATEGORY_LAB_EQUIPMENT = "Lab Equipment";
    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String STAT_FILTER_ACTIVE = "ACTIVE";
    public static final String STAT_FILTER_FREE = "FREE";
    public static final String STAT_FILTER_SALE = "SALE";

    private static final int PAGE_SIZE = 10;
    private static final String ALL_CATEGORY = "All";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String TYPE_FREE = "FREE";
    private static final String TYPE_SALE = "SALE";

    private final HomeRepository homeRepository;
    private final NotificationRepository notificationRepository;
    private final MutableLiveData<List<MarketplaceItem>> marketplaceItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasUnreadNotification = new MutableLiveData<>();
    private final MutableLiveData<Long> activeItemCount = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> freeItemCount = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> saleItemCount = new MutableLiveData<>(0L);
    private final MutableLiveData<String> selectedStatFilter = new MutableLiveData<>();

    private final List<MarketplaceItem> loadedItems = new ArrayList<>();
    private int currentPage = 0;
    private int requestVersion = 0;
    private boolean isLastPage = false;
    private boolean isFetching = false;
    private String selectedCategory = ALL_CATEGORY;
    private String searchQuery = "";
    private String selectedStatus = null;
    private String selectedTransactionType = null;

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

    public MutableLiveData<Long> getActiveItemCount() {
        return activeItemCount;
    }

    public MutableLiveData<Long> getFreeItemCount() {
        return freeItemCount;
    }

    public MutableLiveData<Long> getSaleItemCount() {
        return saleItemCount;
    }

    public MutableLiveData<String> getSelectedStatFilter() {
        return selectedStatFilter;
    }

    public void reloadPosts() {
        requestVersion++;
        currentPage = 0;
        isLastPage = false;
        isFetching = false;
        loadedItems.clear();
        marketplaceItems.setValue(new ArrayList<>());
        loadStats(requestVersion);
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
                selectedStatus, selectedTransactionType,
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

    public void selectActiveItems() {
        applyCardFilter(STAT_FILTER_ACTIVE, STATUS_AVAILABLE, null);
    }

    public void selectFreeItems() {
        applyCardFilter(STAT_FILTER_FREE, STATUS_AVAILABLE, TYPE_FREE);
    }

    public void selectSaleItems() {
        applyCardFilter(STAT_FILTER_SALE, STATUS_AVAILABLE, TYPE_SALE);
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

    private void applyCardFilter(String statFilter, String status, String transactionType) {
        if (safeEquals(selectedStatus, status) && safeEquals(selectedTransactionType, transactionType)) {
            return;
        }
        selectedStatus = status;
        selectedTransactionType = transactionType;
        selectedStatFilter.setValue(statFilter);
        reloadPosts();
    }

    private void loadStats(int statsVersion) {
        String categoryFilter = toApiCategory(selectedCategory);
        String keywordFilter = toApiKeyword(searchQuery);
        loadCount(statsVersion, activeItemCount, categoryFilter, keywordFilter, STATUS_AVAILABLE, null);
        loadCount(statsVersion, freeItemCount, categoryFilter, keywordFilter, STATUS_AVAILABLE, TYPE_FREE);
        loadCount(statsVersion, saleItemCount, categoryFilter, keywordFilter, STATUS_AVAILABLE, TYPE_SALE);
    }

    private void loadCount(int statsVersion, MutableLiveData<Long> target, String category, String keyword,
                           String status, String transactionType) {
        homeRepository.getPosts(0, 1, category, keyword, status, transactionType,
                new OnActionCallback<PageResponse<PostListItemResponse>>() {
                    @Override
                    public void onSuccess(PageResponse<PostListItemResponse> data) {
                        if (statsVersion == requestVersion) {
                            target.postValue(data != null ? data.getTotalElements() : 0L);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        if (statsVersion == requestVersion) {
                            target.postValue(0L);
                        }
                    }
                });
    }

    private void publishLoadedItems() {
        marketplaceItems.postValue(new ArrayList<>(loadedItems));
    }

    private MarketplaceItem toMarketplaceItem(PostListItemResponse post) {
        String category = safeText(post.getCategoryName(), ALL_CATEGORY);
        String location = safeText(post.getLocationName(), "Kh\u00f4ng r\u00f5 v\u1ecb tr\u00ed");
        String status = formatStatus(post.getStatus());
        String title = safeText(post.getTitle(), "Ch\u01b0a c\u00f3 ti\u00eau \u0111\u1ec1");
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

    private boolean safeEquals(String first, String second) {
        if (first == null) {
            return second == null;
        }
        return first.equals(second);
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
            return "C\u00f2n h\u00e0ng";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("SOLD".equals(normalized)) {
            return "\u0110\u00e3 b\u00e1n";
        }
        if ("HIDDEN".equals(normalized)) {
            return "\u0110\u00e3 \u1ea9n";
        }
        return "C\u00f2n h\u00e0ng";
    }

    private String formatPrice(Double price) {
        if (price == null || price <= 0) {
            return "Mi\u1ec5n ph\u00ed";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "\u0111";
    }
}
