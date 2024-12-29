package com.example.final_project.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String content;
    public String sender; // "User" or "AI"
    public long timestamp; // 時間戳
}
