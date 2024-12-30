package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.adapter.ChatAdapter;
import com.example.final_project.api.OpenAIApiService;
import com.example.final_project.api.OpenAIRequest;
import com.example.final_project.api.OpenAIResponse;
import com.example.final_project.api.RetrofitClient;
import com.example.final_project.model.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGPTActivity extends AppCompatActivity {
    private EditText inputMessage;
    private Button sendButton;
    private Button settingsButton; // 新增設定按鈕
    private RecyclerView chatRecyclerView;

    private OpenAIApiService apiService;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        // 初始化元件
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        settingsButton = findViewById(R.id.settingsButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // 初始化 Retrofit 服務
        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        if (apiService == null) {
            Toast.makeText(this, "API Service 初始化失敗", Toast.LENGTH_SHORT).show();
            return;
        }

        // 從 SharedPreferences 獲取使用者暱稱
        SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
        nickname = preferences.getString("nickname", "使用者");

        // 初始化 RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 設定按鈕：導航到設定頁面
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatGPTActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 發送訊息按鈕事件
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString();

        if (message.isEmpty()) {
            Toast.makeText(this, "請輸入訊息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 將使用者訊息添加到 RecyclerView
        chatMessages.add(new ChatMessage(nickname, message, R.drawable.ic_default_profile));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // 清空輸入框
        inputMessage.setText("");

        // 發送訊息到 OpenAI API
        OpenAIRequest request = new OpenAIRequest(
                "gpt-4",
                Collections.singletonList(new OpenAIRequest.Message("user", message))
        );

        apiService.getChatResponse(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();

                    // 將 AI 回覆添加到 RecyclerView
                    chatMessages.add(new ChatMessage("AI", reply, R.drawable.ai_avatar));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        chatMessages.add(new ChatMessage("AI", "API 回應錯誤: " + errorBody, R.drawable.ai_avatar));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    } catch (Exception e) {
                        chatMessages.add(new ChatMessage("AI", "解析錯誤訊息失敗: " + e.getMessage(), R.drawable.ai_avatar));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                chatMessages.add(new ChatMessage("AI", "錯誤: " + t.getMessage(), R.drawable.ai_avatar));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }
}
