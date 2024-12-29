package com.example.final_project;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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

        // 初始化DrawerLayout和NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 設置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 創建DrawerToggle
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // 設置側邊清單選項點擊事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 處理側邊清單點擊事件
                int id = item.getItemId();
                Fragment selectedFragment = null;

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (id == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                }
                else if (id == R.id.nav_chatgpt) {
                    Intent intent = new Intent(MainActivity.this, ChatGPTActivity.class);
                    startActivity(intent);
                }
                else if (id == R.id.nav_multi_user_chat) {
                    Intent intent = new Intent(MainActivity.this, MultiUserChatActivity.class);
                    startActivity(intent);
                }

                // 切換 Fragment
                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame, selectedFragment)
                            .commit();

                }

                // 關閉側邊清單
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
}