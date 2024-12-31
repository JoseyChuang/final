package com.example.final_project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class SettingsFragment extends Fragment {

    private EditText nicknameInput;
    private ImageView profileImage;
    private Button saveButton, backButton;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        // 初始化元件
        nicknameInput = view.findViewById(R.id.nicknameInput);
        profileImage = view.findViewById(R.id.profileImage);
        saveButton = view.findViewById(R.id.saveButton);
        backButton = view.findViewById(R.id.backButton);

        // 返回按鈕邏輯 (使用 Fragment 的 popBackStack)
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });

        // 請求權限
        requestStoragePermission();

        // 初始化圖片選擇器
        setupImagePicker();

        // 加載 SharedPreferences 中的設定資料
        SharedPreferences preferences = requireActivity()
                .getSharedPreferences("UserSettings", requireActivity().MODE_PRIVATE);

        // 加載暱稱
        String savedNickname = preferences.getString("nickname", "");
        nicknameInput.setText(savedNickname);

        // 加載圖片 URI
        String savedImageUri = preferences.getString("profileImageUri", null);
        if (savedImageUri != null) {
            selectedImageUri = Uri.parse(savedImageUri);
            profileImage.setImageURI(selectedImageUri);
        }

        // 儲存暱稱
        saveButton.setOnClickListener(v -> saveSettings(preferences));

        // 圖片選擇功能
        profileImage.setOnClickListener(v -> openImagePicker());

        return view;
    }

    /**
     * 儲存設定到 SharedPreferences
     */
    private void saveSettings(SharedPreferences preferences) {
        String nickname = nicknameInput.getText().toString().trim();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("nickname", nickname);

        if (selectedImageUri != null) {
            editor.putString("profileImageUri", selectedImageUri.toString());
        }

        editor.apply();
        Toast.makeText(getActivity(), "設定已儲存", Toast.LENGTH_SHORT).show();

        // 更新側邊欄頭部
        updateNavigationHeader();
    }

    /**
     * 初始化圖片選擇器
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // 持久化 URI 權限
                            final int takeFlags = result.getData().getFlags() &
                                    (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            requireActivity().getContentResolver()
                                    .takePersistableUriPermission(selectedImageUri, takeFlags);

                            profileImage.setImageURI(selectedImageUri);

                            // 保存到 SharedPreferences
                            SharedPreferences preferences = requireActivity()
                                    .getSharedPreferences("UserSettings", requireActivity().MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("profileImageUri", selectedImageUri.toString());
                            editor.apply();

                            // 更新側邊欄
                            updateNavigationHeader();
                        }
                    }
                }
        );
    }

    /**
     * 打開圖片選擇器
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    /**
     * 請求存取權限
     */
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    /**
     * 權限回調
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "權限授予成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "權限被拒絕，無法讀取圖片", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 更新側邊欄的使用者名稱和圖片
     */
    private void updateNavigationHeader() {
        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        ImageView headerImageView = headerView.findViewById(R.id.imageView);
        TextView headerUserName = headerView.findViewById(R.id.tv_user_name);

        SharedPreferences preferences = requireActivity()
                .getSharedPreferences("UserSettings", requireActivity().MODE_PRIVATE);

        String nickname = preferences.getString("nickname", "使用者名稱");
        String imageUriString = preferences.getString("profileImageUri", null);

        headerUserName.setText(nickname);

        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                requireActivity().grantUriPermission(requireActivity().getPackageName(), imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                headerImageView.setImageURI(imageUri);
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "無法加載圖片: 權限被拒絕", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "圖片加載失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
