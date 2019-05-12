package com.example.nminhanh.spacesharing.Utils;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class ThreeTimeAnroidIntit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
