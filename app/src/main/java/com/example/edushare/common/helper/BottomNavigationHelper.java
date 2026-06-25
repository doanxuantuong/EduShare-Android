package com.example.edushare.common.helper;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.example.edushare.R;
import com.example.edushare.feature.chat.conversation.activity.ConversationActivity;
import com.example.edushare.feature.home.activity.HomeActivity;
import com.example.edushare.feature.locations.activity.MapActivity;
import com.example.edushare.feature.post.activity.PostItemActivity;
import com.example.edushare.feature.users.activity.ProfileActivity;

public class BottomNavigationHelper {

    // Hàm dùng chung để gắn điều hướng dưới cho các Activity
    public static void setup(Activity context, View footerRoot, int currentItemId) {

        // 1. Ánh xạ các ImageView từ layout Custom của bạn
        ImageView navHome = footerRoot.findViewById(R.id.nav_home);
        ImageView navMap = footerRoot.findViewById(R.id.nav_map);
        ImageView navPost = footerRoot.findViewById(R.id.nav_post);
        ImageView navChat = footerRoot.findViewById(R.id.nav_chat);
        ImageView navProfile = footerRoot.findViewById(R.id.nav_profile);

        // 2. Kích hoạt hiệu ứng (Selected) cho tab hiện tại
        // Lệnh setSelected(true) sẽ tự động gọi file selector_nav_xxx để đổi ảnh
        if (navHome != null && currentItemId == R.id.nav_home) navHome.setSelected(true);
        if (navMap != null && currentItemId == R.id.nav_map) navMap.setSelected(true);
        if (navPost != null && currentItemId == R.id.nav_post) navPost.setSelected(true);
        if (navChat != null && currentItemId == R.id.nav_chat) navChat.setSelected(true);
        if (navProfile != null && currentItemId == R.id.nav_profile) navProfile.setSelected(true);

        // 3. Tạo một bộ lắng nghe Click chung cho tất cả các nút để code gọn gàng
        View.OnClickListener navigationListener = v -> {
            int itemId = v.getId();

            // Nếu người dùng bấm lại đúng cái tab đang mở -> Bỏ qua (không load lại trang)
            if (itemId == currentItemId) {
                return;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(context, HomeActivity.class);
            } else if (itemId == R.id.nav_map) {
                intent = new Intent(context, MapActivity.class);
            } else if (itemId == R.id.nav_post) {
                intent = new Intent(context, PostItemActivity.class);
            } else if (itemId == R.id.nav_chat) {
                intent = new Intent(context, ConversationActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(context, ProfileActivity.class);
            }

            // Thực hiện chuyển trang mượt mà không bị chớp màn hình
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
                context.overridePendingTransition(0, 0);
            }
        };

        // 4. Gắn sự kiện Click vào các ImageView
        if (navHome != null) navHome.setOnClickListener(navigationListener);
        if (navMap != null) navMap.setOnClickListener(navigationListener);
        if (navPost != null) navPost.setOnClickListener(navigationListener);
        if (navChat != null) navChat.setOnClickListener(navigationListener);
        if (navProfile != null) navProfile.setOnClickListener(navigationListener);
    }
}