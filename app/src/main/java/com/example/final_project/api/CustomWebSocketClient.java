package com.example.final_project.api;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class CustomWebSocketClient extends WebSocketClient {

    private static final String WS_URL = "ws://192.168.196.242:8080";

    public CustomWebSocketClient(URI serverUri) {
        super(serverUri); // 使用父類建構函數初始化
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket Connected");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket Closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket Error: " + ex.getMessage());
    }

    // ✅ 修正 sendMessage 方法
    public void sendMessage(String message) {
        if (this.isOpen()) { // 檢查連線是否打開
            this.send(message); // 使用 WebSocketClient 的 send 方法
        } else {
            System.out.println("WebSocket is not connected.");
        }
    }

    // ✅ 添加一個方法來手動關閉 WebSocket 連接
    public void disconnect() {
        if (this.isOpen()) {
            this.close();
        }
    }
}
