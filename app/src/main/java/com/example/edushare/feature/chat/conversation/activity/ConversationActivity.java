package com.example.edushare.feature.chat.conversation.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.common.base.OnActionCallback;
import com.example.edushare.common.helper.BottomNavigationHelper;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.conversation.adapter.ConversationAdapter;
import com.example.edushare.feature.chat.conversation.model.Conversation;

import java.util.ArrayList;
import java.util.List;

/*
* Tưởng tượng bạn (file Activity) là một vị Giám đốc.
* - RecyclerView là anh Quản đốc xưởng trực tiếp điều hành công việc.
* - File Adapter chính là một "Cuốn sổ tay quy trình" mà bạn viết ra để hướng dẫn Quản đốc làm việc.
* Trong cuốn sổ này có 2 trang quan trọng:
*   + Trang 1: Cách tạo ra cái hộp (onCreateViewHolder).
*   + Trang 2: Cách nhét dữ liệu vào hộp (onBindViewHolder).
* - Khi bạn viết dòng code này trong Activity: rvChatList.setAdapter(adapter);
*   + Ném cuốn sổ tay (adapter) cho anh Quản đốc (rvChatList)
*   + Và bảo: "Làm việc đi, quy trình tôi ghi hết trong sổ rồi đấy!"
* */
public class ConversationActivity extends AppCompatActivity {
    private RecyclerView rvChatList;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;
    private ChatRepository chatRepository;
    private View footerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Chỉ định giao diện hiển thị cho Activity
        setContentView(R.layout.chat_list);

        //Khởi tạo sự kiện cho các View
        initViews();
        setupRecyclerView();

        // Khởi tạo Repository
        chatRepository = new ChatRepository();

        // Gọi API lấy dữ liệu thực
        fetchConversationsFromApi("fire-base-uid-test-12");
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);

        // 2. Ánh xạ thẻ <include> trong file xml (ID là footer_layout)
        footerLayout = findViewById(R.id.footer_layout);

        // 3. Kích hoạt bộ Helper: Truyền id của tab Chat (R.id.nav_chat) để nó biết đường làm sáng đèn icon Chat
        BottomNavigationHelper.setup(this, footerLayout, R.id.nav_chat);
    }

    private void setupRecyclerView() {
        // Tạo danh sách các đoạn hội thoại (conversationList)
        conversationList = new ArrayList<>();
        // Thêm dữ liệu qua bên Adapter cho Adapter load lên
        adapter = new ConversationAdapter(conversationList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        //Gắn Adapter vào RecyclerView để hiển thị danh sách
        rvChatList.setAdapter(adapter);
    }

    private void fetchConversationsFromApi(String currentUserId) {
        chatRepository.getConversations(currentUserId, new OnActionCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                // Xóa danh sách cũ và thêm dữ liệu mới từ API
                conversationList.clear();
                conversationList.addAll(data);

                // Thông báo cho adapter cập nhật giao diện
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                // Hiển thị thông báo lỗi bằng Toast
                Toast.makeText(ConversationActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
