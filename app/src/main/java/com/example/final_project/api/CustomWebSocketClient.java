package com.example.final_project.api;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class CustomWebSocketClient extends WebSocketClient {
    private static final String BASE_URL = "ws://yourserver.com/chat/";

    public CustomWebSocketClient(String roomId, String userId) {
        super(URI.create(BASE_URL + roomId + "/" + userId));
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

    public void sendMessage(String message) {
        if (this.isOpen()) {
            this.send(message);
        } else {
            System.out.println("WebSocket is not connected.");
        }
    }

    public void disconnect() {
        if (this.isOpen()) {
            this.close();
        }
    }
}
