package com.example.edushare.common.helper;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.edushare.R;
import com.example.edushare.feature.chat.conversation.activity.ConversationActivity;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.locations.MapActivity;
import com.example.edushare.feature.users.activity.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {
    // Hàm dùng chung cho mọi Activity
    public static void setup(Activity context, BottomNavigationView bottomNavigation, int currentItemId) {

        // 1. Sáng đèn cái nút hiện tại
        bottomNavigation.setSelectedItemId(currentItemId);

        // 2. Bắt sự kiện chuyển trang
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Nếu user bấm vào đúng cái tab đang mở -> Không làm gì cả (tránh load lại trang)
            if (itemId == currentItemId) {
                return true;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(context, HomeActivity.class);
            } else if (itemId == R.id.nav_map) {
                intent = new Intent(context, MapActivity.class);
            } else if (itemId == R.id.nav_chat) {
                intent = new Intent(context, ConversationActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(context, ProfileActivity.class);
            } else if (itemId == R.id.nav_post) {
                Toast.makeText(context, R.string.home_post_coming_soon, Toast.LENGTH_SHORT).show();
                return true;
            }

            // Thực hiện chuyển trang
            if (intent != null) {
                // Thêm cờ này để tránh việc tạo ra hàng chục Activity đè lên nhau gây nặng máy
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);

                // (Tùy chọn) Tắt hiệu ứng trượt của Activity để trông giống chuyển tab hơn
                context.overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}
