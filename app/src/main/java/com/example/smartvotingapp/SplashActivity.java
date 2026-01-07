package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds
    private boolean updateCheckComplete = false;
    private boolean shouldProceed = true;

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

        ImageView logo = findViewById(R.id.splash_logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
        logo.startAnimation(fadeIn);

        // Check for updates FIRST before proceeding
        checkForUpdatesBeforeProceed();
    }

    private void checkForUpdatesBeforeProceed() {
        AppUpdateChecker updateChecker = new AppUpdateChecker(this);

        // Check if update is required
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("app_version");

        updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Integer latestVersionCode = dataSnapshot.child("version_code").getValue(Integer.class);

                        if (latestVersionCode != null) {
                            int currentVersionCode = getCurrentVersionCode();

                            if (latestVersionCode > currentVersionCode) {
                                // Update required - show dialog and BLOCK
                                Log.d(TAG, "Update required. Blocking app launch.");
                                shouldProceed = false;
                                updateChecker.checkForUpdate();
                                // DO NOT proceed to login
                                return;
                            }
                        }
                    }

                    // No update required - proceed normally
                    updateCheckComplete = true;
                    proceedToLogin();

                } catch (Exception e) {
                    Log.e(TAG, "Error checking for update", e);
                    // On error, proceed anyway (fail-safe)
                    updateCheckComplete = true;
                    proceedToLogin();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Update check cancelled", databaseError.toException());
                // On error, proceed anyway (fail-safe)
                updateCheckComplete = true;
                proceedToLogin();
            }
        });
    }

    private void proceedToLogin() {
        if (!shouldProceed) {
            // Update is required, don't proceed
            return;
        }

        new Handler().postDelayed(() -> {
            if (shouldProceed && !isFinishing()) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, SPLASH_DURATION);
    }

    private int getCurrentVersionCode() {
        try {
            android.content.pm.PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version code", e);
            return 0;
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from closing splash screen
        // User must wait for update check or update the app
    }
}
