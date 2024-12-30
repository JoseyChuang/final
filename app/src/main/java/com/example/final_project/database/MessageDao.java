package com.example.final_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    void insertMessage(Message message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<Message> getAllMessages();

    @Query("DELETE FROM chat_messages")
    void clearAllMessages();

    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    List<Message> getMessagesByRoomId(String roomId);
}
