package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Day/Night mode based on preference immediately
        android.content.SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        android.view.View logoCard = findViewById(R.id.logo_card);
        Animation splashAnim = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        logoCard.startAnimation(splashAnim);

        // Initialize Handler and Runnable
        handler = new Handler();
        runnable = () -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        };

        // Proceed to login after splash duration
        handler.postDelayed(runnable, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent double navigation if activity is destroyed (e.g. theme change
        // restart)
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
