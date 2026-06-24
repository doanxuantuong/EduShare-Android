package com.example.edushare.feature.chat.message.view_model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edushare.common.base.retrofit.OnActionCallback;
import com.example.edushare.feature.chat.ChatRepository;
import com.example.edushare.feature.chat.PostSummaryResponse;
import com.example.edushare.feature.chat.conversation.dto.request.ConversationCreateRequest;
import com.example.edushare.feature.chat.message.dto.MessageRequest;
import com.example.edushare.feature.chat.message.dto.MessageResponse;
import com.example.edushare.feature.chat.message.dto.MessageStatusResponse;
import com.example.edushare.feature.chat.message.dto.PageResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatDetailViewModel extends ViewModel {

    private final ChatRepository chatRepository;

    // List thật để xử lý nội bộ
    private final List<MessageResponse> messages = new ArrayList<>();

    // LiveData để Activity observe
    private final MutableLiveData<List<MessageResponse>> messageListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<PostSummaryResponse> postSummaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // Dùng để Activity tự scroll xuống cuối
    private final MutableLiveData<Integer> scrollToPosition = new MutableLiveData<>();

    // Dùng để Activity biết conversationId thật sau khi tạo phòng
    private final MutableLiveData<Integer> conversationIdLiveData = new MutableLiveData<>();

    private int conversationId = -1;
    private String senderId;
    private String recipientId;
    private int postId = -1;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    public ChatDetailViewModel(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public LiveData<List<MessageResponse>> getMessageListLiveData() {
        return messageListLiveData;
    }

    public LiveData<PostSummaryResponse> getPostSummaryLiveData() {
        return postSummaryLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Integer> getScrollToPosition() {
        return scrollToPosition;
    }

    public LiveData<Integer> getConversationIdLiveData() {
        return conversationIdLiveData;
    }

    public boolean canLoadMore() {
        return !isLoading && !isLastPage && conversationId != -1;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void initChat(int initialConversationId, String senderId, String recipientId, int postId) {
        this.conversationId = initialConversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.postId = postId;

        conversationIdLiveData.setValue(conversationId);

        if (conversationId > 0) {
            resetPaging();
            loadMessage(conversationId, currentPage);
        } else if (postId != -1) {
            checkExistingConversation(senderId, recipientId, postId);
        } else {
            conversationId = -1;
            conversationIdLiveData.setValue(conversationId);
        }
    }

    private void resetPaging() {
        currentPage = 0;
        isLoading = false;
        isLastPage = false;
        messages.clear();
        messageListLiveData.setValue(new ArrayList<>(messages));
    }

    public void loadNextPage() {
        if (!canLoadMore()) return;

        currentPage++;
        loadMessage(conversationId, currentPage);
    }

    private void loadMessage(int conversationId, int page) {
        isLoading = true;
        loading.setValue(true);

        chatRepository.getMessages(conversationId, page, PAGE_SIZE, new OnActionCallback<PageResponse<MessageResponse>>() {
            @Override
            public void onSuccess(PageResponse<MessageResponse> data) {
                isLoading = false;
                loading.setValue(false);

                if (data == null) return;

                isLastPage = data.isLast();

                List<MessageResponse> newMessages = data.getContent();

                if (newMessages == null || newMessages.isEmpty()) {
                    return;
                }

                // Server trả DESC: mới nhất trước
                // UI cần ASC: cũ -> mới
                newMessages = new ArrayList<>(newMessages);
                Collections.reverse(newMessages);

                if (page == 0) {
                    messages.clear();
                    messages.addAll(newMessages);

                    messageListLiveData.setValue(new ArrayList<>(messages));

                    markConversationAsRead();

                    if (!messages.isEmpty()) {
                        scrollToPosition.setValue(messages.size() - 1);
                    }
                } else {
                    // Load tin cũ, nhét lên đầu list
                    messages.addAll(0, newMessages);
                    messageListLiveData.setValue(new ArrayList<>(messages));
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading = false;
                loading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) return;

        if (conversationId == -1) {
            sendFirstMessage(content.trim());
        } else {
            sendNormalMessage(content.trim());
        }
    }

    private void sendNormalMessage(String content) {
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setContent(content);
        messageRequest.setConversationId(conversationId);
        messageRequest.setSenderId(senderId);

        chatRepository.sendMessage(conversationId, messageRequest, new OnActionCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                addNewMessage(data);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    private void sendFirstMessage(String content) {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setSenderId(senderId);
        request.setRecipientId(recipientId);
        request.setPostId(postId);
        request.setContent(content);

        chatRepository.sendFirstMessage(request, new OnActionCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                if (data == null) return;

                conversationId = data.getConversationId();
                conversationIdLiveData.setValue(conversationId);

                addNewMessage(data);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Lỗi tạo phòng: " + error);
            }
        });
    }

    public void fetchPostSummary(int postId) {
        if (postId == -1) return;

        chatRepository.getPostSummary(postId, new OnActionCallback<PostSummaryResponse>() {
            @Override
            public void onSuccess(PostSummaryResponse data) {
                postSummaryLiveData.setValue(data);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    private void markConversationAsRead() {
        if (conversationId == -1 || senderId == null || recipientId == null) return;

        chatRepository.markMessageAsRead(conversationId, senderId, recipientId, new OnActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                // Không cần làm gì
            }

            @Override
            public void onFailure(String error) {
                Log.e("ChatDetailViewModel", "Mark read failed: " + error);
            }
        });
    }

    public void checkExistingConversation(String senderId, String recipientId, int postId) {
        chatRepository.checkExistingConversation(senderId, recipientId, postId, new OnActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                if (data != null && data > 0) {
                    conversationId = data;
                    conversationIdLiveData.setValue(conversationId);

                    resetPaging();
                    loadMessage(conversationId, currentPage);
                } else {
                    conversationId = -1;
                    conversationIdLiveData.setValue(conversationId);
                }
            }

            @Override
            public void onFailure(String error) {
                conversationId = -1;
                conversationIdLiveData.setValue(conversationId);
                errorMessage.setValue(error);
            }
        });
    }

    public void addNewMessage(MessageResponse newMessage) {
        if (newMessage == null) return;

        // Nếu đang ở phòng thật thì chỉ nhận message đúng phòng
        // Nếu đang ở phòng ảo -1 thì nhận tin đầu tiên rồi cập nhật phòng thật
        if (conversationId != -1 && newMessage.getConversationId() != conversationId) {
            return;
        }

        if (conversationId == -1) {
            conversationId = newMessage.getConversationId();
            conversationIdLiveData.setValue(conversationId);
        }

        // Chống add trùng message
        for (MessageResponse msg : messages) {
            if (Objects.equals(msg.getId(), newMessage.getId())) {
                return;
            }
        }

        messages.add(newMessage);
        messageListLiveData.setValue(new ArrayList<>(messages));

        scrollToPosition.setValue(messages.size() - 1);
    }

    public void updateLastMessageStatus(MessageStatusResponse statusResponse) {
        if (statusResponse == null) return;

        if (conversationId != statusResponse.getConversationId()) return;

        if (!Boolean.TRUE.equals(statusResponse.getRead())) return;

        boolean isUpdated = false;

        for (int i = messages.size() - 1; i >= 0; i--) {
            MessageResponse message = messages.get(i);

            if (!Boolean.TRUE.equals(message.getRead())) {
                message.setRead(true);
                isUpdated = true;
            } else {
                break;
            }
        }

        if (isUpdated) {
            messageListLiveData.setValue(new ArrayList<>(messages));
        }
    }
}