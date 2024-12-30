package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.api.OpenAIApiService;
import com.example.final_project.api.OpenAIRequest;
import com.example.final_project.api.OpenAIResponse;
import com.example.final_project.api.RetrofitClient;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGPTActivity extends AppCompatActivity {
    private EditText inputMessage;
    private Button sendButton;
    private Button settingsButton; // 新增設定按鈕
    private TextView chatResponse;

    private OpenAIApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        // 初始化元件
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatResponse = findViewById(R.id.chatResponse);
        settingsButton = findViewById(R.id.settingsButton); // 綁定 XML 中的設定按鈕

        TextView nicknameDisplay = findViewById(R.id.nicknameDisplay);
        ImageView profileImage = findViewById(R.id.profileImage);

        // 初始化 Retrofit 服務
        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        if (apiService == null) {
            Toast.makeText(this, "API Service 初始化失敗", Toast.LENGTH_SHORT).show();
            return;
        }

        // 顯示暱稱
        SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
        String nickname = preferences.getString("nickname", "使用者");
        nicknameDisplay.setText(nickname);

        // 預設圖片（可擴展為使用者自訂圖片）
        profileImage.setImageResource(R.drawable.ic_default_profile);

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

        OpenAIRequest request = new OpenAIRequest(
                "gpt-4",
                Collections.singletonList(new OpenAIRequest.Message("user", message))
        );

        apiService.getChatResponse(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    chatResponse.setText(reply);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        chatResponse.setText("API 回應錯誤: " + errorBody);
                    } catch (Exception e) {
                        chatResponse.setText("解析錯誤訊息失敗: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                chatResponse.setText("錯誤: " + t.getMessage());
            }
        });
    }
}
