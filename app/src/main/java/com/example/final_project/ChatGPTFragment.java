package com.example.final_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class ChatGPTFragment extends Fragment {

    private EditText inputMessage;
    private Button sendButton, settingsButton;
    private RecyclerView chatRecyclerView;
    private TextView nicknameDisplay;
    private ImageView profileImage;

    private OpenAIApiService apiService;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private String nickname;
    private String profileImageUri;

    /**
     * 加載使用者名稱和圖片設定
     */
    private void loadUserSettings() {
        SharedPreferences preferences = requireActivity()
                .getSharedPreferences("UserSettings", requireActivity().MODE_PRIVATE);

        // 加載暱稱和圖片 URI
        nickname = preferences.getString("nickname", "使用者");
        profileImageUri = preferences.getString("profileImageUri", null);

        // 設定使用者名稱到 TextView
        nicknameDisplay.setText(nickname);

        // 設定使用者圖片到 ImageView
        if (profileImageUri != null && !profileImageUri.isEmpty()) {
            Glide.with(requireContext())
                    .load(profileImageUri)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_default_profile);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chatgpt, container, false);

        // 初始化元件
        inputMessage = view.findViewById(R.id.inputMessage);
        sendButton = view.findViewById(R.id.sendButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        nicknameDisplay = view.findViewById(R.id.nicknameDisplay);
        profileImage = view.findViewById(R.id.profileImage);

        // 初始化 Retrofit 服務
        apiService = RetrofitClient.getInstance().create(OpenAIApiService.class);
        if (apiService == null) {
            Toast.makeText(requireContext(), "API Service 初始化失敗", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 讀取使用者暱稱和圖片
        loadUserSettings();

        // 初始化 RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        // 設定按鈕：導航到設定頁面
        settingsButton.setOnClickListener(v -> navigateToSettingsFragment());

        // 發送訊息按鈕事件
        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void navigateToSettingsFragment() {
        // 使用 FragmentManager 進行 Fragment 切換
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "請輸入訊息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 每次發送訊息時，重新讀取使用者設定，確保最新暱稱和圖片
        loadUserSettings();

        // 添加使用者訊息到 RecyclerView
        chatMessages.add(new ChatMessage(nickname, message, profileImageUri));
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

                    chatMessages.add(new ChatMessage("AI", reply, null));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                } else {
                    chatMessages.add(new ChatMessage("AI", "API 回應錯誤", null));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                chatMessages.add(new ChatMessage("AI", "錯誤: " + t.getMessage(), null));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            }
        });
    }
}
