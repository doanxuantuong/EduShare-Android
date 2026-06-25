package com.example.edushare.common.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.edushare.common.base.AppConstant;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketManager {
    private static final String WS_URL = AppConstant.BASE_URL_CHAT;
    @SuppressLint("StaticFieldLeak")
    private static WebSocketManager instance;

    private StompClient stompClient;

    private WebSocketManager() {
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    @SuppressLint("CheckResult")
    public void connectWebSocket() {
        if (stompClient != null && stompClient.isConnected()) {
            return;
        }

        if (stompClient == null) {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

            // Lắng nghe trạng thái (Chỉ cần 1 thằng Manager nghe là đủ)
            stompClient.lifecycle().subscribe(lifecycleEvent -> {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d("WebSocket", "Connection opened globally");
                        break;
                    case ERROR:
                        Log.d("WebSocket", "Lỗi kết nối globally", lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        Log.d("SOCKET", "Đã ngắt kết nối globally");
                        break;
                }
            }, throwable -> Log.e("WebSocket", "Lỗi lifecycle", throwable));
        }

        stompClient.connect();
    }

    public StompClient getStompClient() {
        return stompClient;
    }

    public void disconnect() {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
            stompClient = null;
        }
    }

}
