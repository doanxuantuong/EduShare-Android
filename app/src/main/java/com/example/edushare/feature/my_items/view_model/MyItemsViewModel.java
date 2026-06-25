package com.example.edushare.feature.my_items.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.my_items.MyItemsRepository;
import com.example.edushare.feature.my_items.model.MyItem;

import java.util.ArrayList;
import java.util.List;

public class MyItemsViewModel extends ViewModel {
    private final MyItemsRepository myItemsRepository;
    private final MutableLiveData<List<MyItem>> displayedItems = new MutableLiveData<>();
    private final MutableLiveData<Integer> activeItemCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> soldItemCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showingSoldItems = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final List<MyItem> allItems = new ArrayList<>();

    private boolean isShowingSoldItems = false;

    public MyItemsViewModel(MyItemsRepository myItemsRepository) {
        this.myItemsRepository = myItemsRepository;
    }

    public MutableLiveData<List<MyItem>> getDisplayedItems() {
        return displayedItems;
    }

    public MutableLiveData<Integer> getActiveItemCount() {
        return activeItemCount;
    }

    public MutableLiveData<Integer> getSoldItemCount() {
        return soldItemCount;
    }

    public MutableLiveData<Boolean> getShowingSoldItems() {
        return showingSoldItems;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadItems() {
        loading.setValue(true);
        myItemsRepository.getMyItems(new OnActionCallback<List<MyItem>>() {
            @Override
            public void onSuccess(List<MyItem> items) {
                allItems.clear();
                if (items != null) {
                    allItems.addAll(items);
                }
                publishCurrentTab();
                loading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                allItems.clear();
                publishCurrentTab();
                loading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void showActiveItems() {
        isShowingSoldItems = false;
        publishCurrentTab();
    }

    public void showSoldItems() {
        isShowingSoldItems = true;
        publishCurrentTab();
    }

    private void publishCurrentTab() {
        int activeCount = 0;
        int soldCount = 0;
        List<MyItem> filteredItems = new ArrayList<>();

        for (MyItem item : allItems) {
            if (item.isSold()) {
                soldCount++;
            } else {
                activeCount++;
            }

            if (item.isSold() == isShowingSoldItems) {
                filteredItems.add(item);
            }
        }

        activeItemCount.postValue(activeCount);
        soldItemCount.postValue(soldCount);
        showingSoldItems.postValue(isShowingSoldItems);
        displayedItems.postValue(filteredItems);
    }
}
