package com.example.edushare.common.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketManager {
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
        if (stompClient != null || stompClient.isConnected()) {
            return;
        }

        String wsUrl = "ws://10.0.2.2:8080/ws-edushare/websocket";
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

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
