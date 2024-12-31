package com.example.final_project;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 Toolbar 和 Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // 更新側邊欄
        updateNavigationHeader();

        // 設定 Navigation 選項
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                }  else if (id == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                } else if (id == R.id.nav_chatgpt) {
                    selectedFragment = new ChatGPTFragment();
                } else if (id == R.id.nav_multi_user_chat) {
                    selectedFragment = new MultiUserChatFragment();
                } else {
                    throw new IllegalStateException("Unexpected menu item selected: " + id);
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, selectedFragment)
                            .addToBackStack(null)
                            .commit();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // 預設 Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    /**
     * 更新側邊欄的使用者名稱和圖片
     */
    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.imageView);
        TextView userName = headerView.findViewById(R.id.tv_user_name);
        TextView userEmail = headerView.findViewById(R.id.tv_user_email);

        SharedPreferences preferences = getSharedPreferences("UserSettings", MODE_PRIVATE);

        String nickname = preferences.getString("nickname", "使用者名稱");
        String email = preferences.getString("userEmail", "user@example.com");
        String imageUriString = preferences.getString("profileImageUri", null);

        userName.setText(nickname);
        userEmail.setText(email);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationHeader(); // 確保每次返回主頁時側邊欄更新
    }
}
