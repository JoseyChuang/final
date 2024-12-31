package com.example.final_project.model;

public class ChatMessage {
    private String userName;
    private String message;
    private String userImageUri; // 圖片 URI

    public ChatMessage(String userName, String message, String userImageUri) {
        this.userName = userName;
        this.message = message;
        this.userImageUri = userImageUri;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public String getUserImageUri() {
        return userImageUri;
    }
}

