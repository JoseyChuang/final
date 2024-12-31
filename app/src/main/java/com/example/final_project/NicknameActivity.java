package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NicknameActivity extends AppCompatActivity {
    private EditText nicknameInput;
    private Button startChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        nicknameInput = findViewById(R.id.nicknameInput);
        startChatButton = findViewById(R.id.startChatButton);

        startChatButton.setOnClickListener(v -> {
            String nickname = nicknameInput.getText().toString().trim();
            if (!nickname.isEmpty()) {
                // 將暱稱傳遞到 MultiUserChatActivity
                Intent intent = new Intent(NicknameActivity.this, MultiUserChatFragment.class);
                intent.putExtra("USER_ID", nickname);
                startActivity(intent);
                finish();
            } else {
                nicknameInput.setError("暱稱不能為空");
            }
        });
    }
}
