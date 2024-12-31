package com.example.final_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化按鈕
        Button btnSingleChat = view.findViewById(R.id.btn_single_chat);
        Button btnMultiUserChat = view.findViewById(R.id.btn_multi_user_chat);

        // 單人聊天室按鈕點擊事件
        btnSingleChat.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new ChatGPTFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 多人聊天室按鈕點擊事件
        btnMultiUserChat.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new MultiUserChatFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
