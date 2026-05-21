package com.example.edushare.feature.chat.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edushare.R;
import com.example.edushare.feature.chat.adapter.ConversationAdapter;
import com.example.edushare.feature.chat.model.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    private RecyclerView rvChatList;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list);

        initViews();
        setupRecyclerView();
        loadDummyData();
    }

    private void initViews() {
        rvChatList = findViewById(R.id.rvChatList);
    }

    private void setupRecyclerView() {
        conversationList = new ArrayList<>();
        adapter = new ConversationAdapter(conversationList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(adapter);
    }

    private void loadDummyData() {
        conversationList.add(new Conversation(1, "user_001", "Nguyen Van A", null, "Is the textbook still available?", "2m ago"));
        conversationList.add(new Conversation(2, "user_002", "Tran Thi B", null, "Thank you! I will pick it up tomorrow", "15m ago"));
        conversationList.add(new Conversation(3, "user_003", "Le Van C", null, "Can you send more photos?", "1h ago"));
        conversationList.add(new Conversation(4, "user_004", "Pham Thi D", null, "Great! See you at the library", "3h ago"));
        conversationList.add(new Conversation(5, "user_005", "Hoang Van E", null, "How much for the Arduino kit?", "1d ago"));

        adapter.notifyDataSetChanged();
    }
}
