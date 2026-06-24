package com.example.edushare.feature.my_items;

import com.example.edushare.R;
import com.example.edushare.feature.my_items.model.MyItem;

import java.util.ArrayList;
import java.util.List;

public class MyItemsRepository {
    public List<MyItem> getMyItems() {
        List<MyItem> myItems = new ArrayList<>();

        // Dữ liệu mẫu được đặt trong Repository để Activity/ViewModel không phụ thuộc vào nguồn dữ liệu cụ thể.
        myItems.add(new MyItem(
                1,
                R.drawable.img_organic_chemistry_textbook,
                "",
                "Organic Chemistry Textbook - 5th Edition",
                "150,000đ",
                24,
                5,
                false
        ));
        myItems.add(new MyItem(
                2,
                R.drawable.ic_my_items_microscope,
                "",
                "Laboratory Microscope - Olympus CX23",
                "Free",
                24,
                5,
                false
        ));
        myItems.add(new MyItem(
                3,
                R.drawable.ic_my_items_caliper,
                "",
                "Digital Caliper - Precision Measuring Tool",
                "80,000đ",
                24,
                5,
                false
        ));

        return myItems;
    }
}
