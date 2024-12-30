package com.example.final_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;

    // 建構子，傳入訊息列表
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 載入單一訊息項目的佈局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // 綁定訊息資料到 ViewHolder
        ChatMessage message = chatMessages.get(position);
        holder.nicknameDisplay.setText(message.getNickname());
        holder.messageContent.setText(message.getMessage());
        holder.profileImage.setImageResource(message.getProfileImageResId());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder 類別，用於綁定 UI 元件
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameDisplay;
        TextView messageContent;
        ImageView profileImage;

        ChatViewHolder(View itemView) {
            super(itemView);
            nicknameDisplay = itemView.findViewById(R.id.nicknameDisplay);
            messageContent = itemView.findViewById(R.id.messageContent);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }

    // 新增訊息到列表並更新 UI
    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }
}
