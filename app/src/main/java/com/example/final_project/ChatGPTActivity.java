package com.example.final_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private TextView chatResponse;

    private OpenAIApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatResponse = findViewById(R.id.chatResponse);

        apiService = com.example.final_project.api.RetrofitClient.getInstance().create(OpenAIApiService.class);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString();

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
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                chatResponse.setText("Error: " + t.getMessage());
            }
        });
    }
}
