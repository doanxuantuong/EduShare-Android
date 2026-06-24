package com.example.edushare.feature.my_items.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public void loadItems() {
        // Repository là nơi duy nhất biết dữ liệu lấy từ đâu; sau này đổi sang API thì ViewModel không cần đổi luồng xử lý.
        allItems.clear();
        allItems.addAll(myItemsRepository.getMyItems());
        publishCurrentTab();
    }

    public void showActiveItems() {
        isShowingSoldItems = false;
        publishCurrentTab();
    }

    public void showSoldItems() {
        isShowingSoldItems = true;
        publishCurrentTab();
    }

    public void deleteItem(MyItem deletedItem) {
        // Xoá theo id để adapter không phụ thuộc vào đúng object reference của item.
        for (int index = allItems.size() - 1; index >= 0; index--) {
            if (allItems.get(index).getId() == deletedItem.getId()) {
                allItems.remove(index);
                break;
            }
        }
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

        activeItemCount.setValue(activeCount);
        soldItemCount.setValue(soldCount);
        showingSoldItems.setValue(isShowingSoldItems);
        displayedItems.setValue(filteredItems);
    }
}
