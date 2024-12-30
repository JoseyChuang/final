package com.example.final_project;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在這裡初始化全局資源，例如資料庫、全局配置等
    }
}
