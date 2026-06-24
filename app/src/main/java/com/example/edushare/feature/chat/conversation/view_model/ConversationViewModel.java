package com.example.edushare.feature.chat.conversation.view_model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.conversation.model.Conversation;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;

public class ConversationViewModel extends ViewModel {
    private final ChatRepository chatRepository;

    // Các LiveData để Activity "nhìn" (Observe)
    private final MutableLiveData<List<Conversation>> conversationList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Lưu trữ danh sách gốc để phục vụ tính năng Tìm kiếm
    private final List<Conversation> originalList = new ArrayList<>();
    private String currentKeyword = "";

    // Biến phân trang
    private final int PAGE_SIZE = 20;
    private int currentPage = 0;
    private boolean isFetching = false;
    private boolean isLastPage = false;

    public ConversationViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // --- GETTERS ---
    public MutableLiveData<List<Conversation>> getConversationList() { return conversationList; }
    public MutableLiveData<Boolean> getLoading() { return loading; }
    public MutableLiveData<String> getErrorMessage() { return errorMessage; }

    // --- HÀM 1: LẤY DANH SÁCH (Hỗ trợ phân trang và Refresh) ---
    public void loadConversations(String userId, boolean isRefresh) {
        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
        }

        if (isFetching || isLastPage) return;

        isFetching = true;
        loading.postValue(true);

        chatRepository.getConversations(userId, currentPage, PAGE_SIZE, new OnActionCallback<PageResponse<Conversation>>() {
            @Override
            public void onSuccess(PageResponse<Conversation> data) {
                isFetching = false;
                loading.postValue(false);
                isLastPage = data.isLast();

                if (data.getContent() != null) {
                    if (currentPage == 0) {
                        originalList.clear();
                    }
                    originalList.addAll(data.getContent());

                    // Áp dụng bộ lọc tìm kiếm hiện tại (nếu có) trước khi đẩy ra UI
                    applySearchFilter(currentKeyword);

                    currentPage++;
                }
            }

            @Override
            public void onFailure(String error) {
                isFetching = false;
                loading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    // --- HÀM 2: CẬP NHẬT KHI CÓ TIN NHẮN TỪ WEBSOCKET ---
    public void handleNewMessage(MessageResponse newMessage, String currentUserId) {
        int existingIndex = -1;

        // Kiếm xem tin nhắn mới của conversation đó có tồn tại chưa?
        for (int i = 0; i < originalList.size(); i++) {
            if (originalList.get(i).getId() == newMessage.getConversationId()) {
                existingIndex = i;
                break;
            }
        }

        // Nếu có thì cập nhật lại tin nhắn
        if (existingIndex != -1) {
            // Lấy ra đối tượng conversation cũ
            Conversation conversation = originalList.get(existingIndex);
            // Đặt lại nội dung tin nhắn mới nhất
            conversation.setLastMessage(newMessage.getContent());
            // Đặt lại thời gian tin nhắn mới nhất
            conversation.setUpdatedAt(newMessage.getCreatedAt());
            // Cộng thêm 1 vào con số màu đỏ báo tin nhắn chưa đọc
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);

            // Rút nó ra và nhét lên đầu danh sách gốc
            originalList.remove(existingIndex);
            originalList.add(0, conversation);

            // Cập nhật lại UI
            applySearchFilter(currentKeyword);

        } else {
            // Có người mới nhắn tin -> Lấy lại danh sách từ đầu
            loadConversations(currentUserId, true);
        }
    }

    // --- HÀM 3: TÌM KIẾM TÊN NGƯỜI NHẬN ---
    public void search(String keyword) {
        this.currentKeyword = keyword != null ? keyword.trim().toLowerCase() : "";
        applySearchFilter(this.currentKeyword);
    }

    private void applySearchFilter(String keyword) {
        // Hiển thị danh sách gốc nếu không tìm kiếm
        if (keyword.isEmpty()) {
            conversationList.postValue(new ArrayList<>(originalList));
            return;
        }

        // Hiển thị danh sách tạm nếu tìm kiếm (Xóa chữ thì về gốc)
        // Khi lọc chữ tìm kiếm nó không hiện ra danh sách gốc
        // Mà hiển thị danh sách tạm đề phòng trường hợp có người mơới nhawns tin
        List<Conversation> filteredList = new ArrayList<>();
        for (Conversation conversation : originalList) {
            String name = conversation.getRecipientName();
            if (name != null && name.toLowerCase().contains(keyword)) {
                filteredList.add(conversation);
            }
        }
        conversationList.postValue(filteredList);
    }
}
