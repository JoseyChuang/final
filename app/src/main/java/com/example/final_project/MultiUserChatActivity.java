package com.example.final_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.api.CustomWebSocketClient;
import com.example.final_project.api.OpenAIApiService;
import com.example.final_project.api.OpenAIRequest;
import com.example.final_project.api.OpenAIResponse;
import com.example.final_project.api.RetrofitClient;
import com.example.final_project.database.ChatDatabase;
import com.example.final_project.database.Message;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.concurrent.Executors;

public class MultiUserChatActivity extends AppCompatActivity {
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatHistory;

    private CustomWebSocketClient webSocketClient;
    private OpenAIApiService apiService;
    private ChatDatabase chatDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_chat);

        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatHistory = findViewById(R.id.chatHistory);

        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        chatDatabase = ChatDatabase.getInstance(this);

        // 載入歷史訊息
        loadChatHistory();

        // 連接到多人聊天室 WebSocket
        webSocketClient = new CustomWebSocketClient("room123", "user123") {
            @Override
            public void onMessage(String message) {
                runOnUiThread(() -> {
                    chatHistory.append("\n" + message);
                    saveMessageToDatabase("User123", message);
                });
            }
        };
        webSocketClient.connect();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString();

        // 廣播給所有聊天室用戶
        webSocketClient.sendMessage("User123: " + message);
        saveMessageToDatabase("User123", message);

        // 發送給 AI 並接收回覆
        OpenAIRequest request = new OpenAIRequest(
                "gpt-4",
                Collections.singletonList(new OpenAIRequest.Message("user", message))
        );

        apiService.getChatResponse(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    webSocketClient.sendMessage("AI: " + reply);
                    runOnUiThread(() -> {
                        chatHistory.append("\nAI: " + reply);
                        saveMessageToDatabase("AI", reply);
                    });
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                runOnUiThread(() -> chatHistory.append("\nError: " + t.getMessage()));
            }
        });

        inputMessage.setText(""); // 清空輸入欄
    }

    private void saveMessageToDatabase(String sender, String content) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Message message = new Message();
            message.sender = sender;
            message.content = content;
            message.timestamp = System.currentTimeMillis();
            chatDatabase.messageDao().insertMessage(message);
        });
    }

    private void loadChatHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Message> messages = chatDatabase.messageDao().getAllMessages();
            runOnUiThread(() -> {
                for (Message msg : messages) {
                    chatHistory.append("\n" + msg.sender + ": " + msg.content);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }
}
