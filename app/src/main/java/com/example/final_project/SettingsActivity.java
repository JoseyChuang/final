package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private EditText nicknameInput;
    private ImageView profileImage;
    private Button saveButton;

    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化元件
        nicknameInput = findViewById(R.id.nicknameInput);
        profileImage = findViewById(R.id.profileImage);
        saveButton = findViewById(R.id.saveButton);
        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        // 讀取暱稱設定
        SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
        String savedNickname = preferences.getString("nickname", "");
        nicknameInput.setText(savedNickname);

        // 儲存暱稱
        saveButton.setOnClickListener(v -> {
            String nickname = nicknameInput.getText().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("nickname", nickname);
            editor.apply();
            Toast.makeText(this, "設定已儲存", Toast.LENGTH_SHORT).show();
        });


        // 圖片選擇功能
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            // 儲存圖片 URI 到 SharedPreferences
            SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("profileImageUri", imageUri.toString());
            editor.apply();
        }
    }

}
