package com.example.final_project.model;

public class ChatMessage {
    private String nickname;       // 暱稱
    private String message;        // 訊息內容
    private int profileImageResId; // 頭像資源ID

    public ChatMessage(String nickname, String message, int profileImageResId) {
        this.nickname = nickname;
        this.message = message;
        this.profileImageResId = profileImageResId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
