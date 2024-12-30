package com.example.final_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.api.CustomWebSocketClient;
import com.example.final_project.api.OpenAIApiService;
import com.example.final_project.api.RetrofitClient;
import com.example.final_project.database.ChatDatabase;
import com.example.final_project.database.Message;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.Executors;

public class MultiUserChatActivity extends AppCompatActivity {
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatHistory;

    private String roomId;
    private String userId;

    private CustomWebSocketClient webSocketClient;
    private OpenAIApiService apiService;
    private ChatDatabase chatDatabase;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_chat);

        // 獲取暱稱
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            throw new IllegalStateException("USER_ID cannot be null or empty");
        }

        // 綁定 UI 元件
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatHistory = findViewById(R.id.chatHistory);

        // 初始化 API 和資料庫
        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        chatDatabase = ChatDatabase.getInstance(getApplicationContext());

        roomId = "default_room"; // 預設 Room ID

        // 註冊 WebSocket
        registerUser();

        sendButton.setOnClickListener(v -> sendMessage());

        chatHistory.append("\n[Activity Initialized Successfully]");
    }

    /**
     * 註冊用戶到 WebSocket
     */
    private void registerUser() {
        try {
            URI webSocketUri = new URI("ws://192.168.1.105:8080/chat?roomId=" + roomId + "&userId=" + userId);

            webSocketClient = new CustomWebSocketClient(webSocketUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    runOnUiThread(() -> chatHistory.append("\n[WebSocket Connected]"));
                    webSocketClient.send("{\"type\": \"register\", \"userId\": \"" + userId + "\"}");
                    runOnUiThread(() -> chatHistory.append("\n[Registered with User ID: " + userId + "]"));
                }

                @Override
                public void onMessage(String message) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject data = new JSONObject(message);
                            String type = data.getString("type");
                            String sender = data.getString("userId");
                            String content = data.getString("message");

                            if (type.equals("message")) {
                                if ("AI".equals(sender)) {
                                    chatHistory.append("\n🤖 AI: " + content); // AI 訊息標示為 AI
                                } else {
                                    chatHistory.append("\n" + sender + ": " + content); // 普通用戶訊息
                                }

                                saveMessageToDatabase(roomId, sender, content);
                            }
                        } catch (JSONException e) {
                            chatHistory.append("\n[Error parsing message: " + e.getMessage() + "]");
                        }
                    });
                }


                @Override
                public void onClose(int code, String reason, boolean remote) {
                    runOnUiThread(() -> chatHistory.append("\n[WebSocket Closed: " + reason + "]"));
                }

                @Override
                public void onError(Exception ex) {
                    runOnUiThread(() -> chatHistory.append("\n[Error: " + ex.getMessage() + "]"));
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            runOnUiThread(() -> chatHistory.append("\n[Error: WebSocket Initialization Failed - " + e.getMessage() + "]"));
        }
    }

    /**
     * 發送消息
     */
    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send("{\"type\": \"message\", \"userId\": \"" + userId + "\", \"message\": \"" + message + "\"}");
            runOnUiThread(() -> {
                chatHistory.append("\nYou: " + message);
                inputMessage.setText("");
            });
        } else {
            runOnUiThread(() -> chatHistory.append("\n❌ 無法發送消息，WebSocket 未連接"));
        }
    }

    private void saveMessageToDatabase(String roomId, String sender, String content) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Message message = new Message();
            message.roomId = roomId;
            message.sender = sender;
            message.content = content;
            message.timestamp = System.currentTimeMillis();
            chatDatabase.messageDao().insertMessage(message);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
