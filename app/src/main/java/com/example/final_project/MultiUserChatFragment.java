package com.example.final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class MultiUserChatFragment extends Fragment {

    private EditText inputMessage;
    private Button sendButton;
    private TextView chatHistory;

    private String roomId;
    private String userId;

    private CustomWebSocketClient webSocketClient;
    private OpenAIApiService apiService;
    private ChatDatabase chatDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_multi_user_chat, container, false);

        // 初始化 UI 元件
        inputMessage = view.findViewById(R.id.inputMessage);
        sendButton = view.findViewById(R.id.sendButton);
        chatHistory = view.findViewById(R.id.chatHistory);

        // 初始化 API 和資料庫
        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        chatDatabase = ChatDatabase.getInstance(requireContext());

        // 獲取使用者資訊
        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID", "Guest");
        } else {
            userId = "Guest";
        }

        roomId = "default_room"; // 預設 Room ID

        // 註冊 WebSocket
        registerUser();

        // 發送訊息按鈕事件
        sendButton.setOnClickListener(v -> sendMessage());

        chatHistory.append("\n[Fragment Initialized Successfully]");

        return view;
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
                    requireActivity().runOnUiThread(() -> chatHistory.append("\n[WebSocket Connected]"));
                    webSocketClient.send("{\"type\": \"register\", \"userId\": \"" + userId + "\"}");
                    requireActivity().runOnUiThread(() -> chatHistory.append("\n[Registered with User ID: " + userId + "]"));
                }

                @Override
                public void onMessage(String message) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            JSONObject data = new JSONObject(message);
                            String type = data.getString("type");
                            String sender = data.getString("userId");
                            String content = data.getString("message");

                            if (type.equals("message")) {
                                if ("AI".equals(sender)) {
                                    chatHistory.append("\n🤖 AI: " + content);
                                } else {
                                    chatHistory.append("\n" + sender + ": " + content);
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
                    requireActivity().runOnUiThread(() -> chatHistory.append("\n[WebSocket Closed: " + reason + "]"));
                }

                @Override
                public void onError(Exception ex) {
                    requireActivity().runOnUiThread(() -> chatHistory.append("\n[Error: " + ex.getMessage() + "]"));
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            requireActivity().runOnUiThread(() -> chatHistory.append("\n[Error: WebSocket Initialization Failed - " + e.getMessage() + "]"));
        }
    }

    /**
     * 發送消息
     */
    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send("{\"type\": \"message\", \"userId\": \"" + userId + "\", \"message\": \"" + message + "\"}");
            requireActivity().runOnUiThread(() -> {
                chatHistory.append("\nYou: " + message);
                inputMessage.setText("");
            });
        } else {
            requireActivity().runOnUiThread(() -> chatHistory.append("\n❌ 無法發送消息，WebSocket 未連接"));
        }
    }

    /**
     * 保存消息到資料庫
     */
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
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
