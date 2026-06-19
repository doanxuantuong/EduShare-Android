package com.example.edushare.common.helper;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.edushare.R;
import com.example.edushare.feature.chat.conversation.activity.ConversationActivity;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.locations.MapActivity;
import com.example.edushare.feature.users.activity.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class BottomNavigationHelper {
    // Hàm dùng chung cho mọi Activity
    public static void setup(Activity context, View footerRoot, int currentItemId) {

        // 1. Tìm BottomNavigationView và nút Post từ trong layout dùng chung
        BottomNavigationView bottomNavigation = footerRoot.findViewById(R.id.bottomNavigation);
        MaterialCardView btnPost = footerRoot.findViewById(R.id.cvPostButton);

        // 2. Sáng đèn cái nút hiện tại (chỉ làm nếu bottomNavigation tồn tại)
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(currentItemId);

            // Bắt sự kiện chuyển trang cho 4 nút xung quanh
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

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
                }

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    context.overridePendingTransition(0, 0);
                }
                return true;
            });
        }

        // 3. Bắt riêng sự kiện Click cho nút POST tự chế ở giữa
        if (btnPost != null) {
            btnPost.setOnClickListener(v -> {
                // Xử lý khi bấm nút Post ở đây
                Toast.makeText(context, context.getString(R.string.home_post_coming_soon), Toast.LENGTH_SHORT).show();

                // Ví dụ sau này muốn mở màn hình tạo bài viết:
                // Intent intent = new Intent(context, CreatePostActivity.class);
                // context.startActivity(intent);
            });
        }
    }
}
