package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppUpdateChecker {
    private static final String TAG = "AppUpdateChecker";
    private static final String DOWNLOAD_URL = "https://smart-voting-web.vercel.app/";

    private Context context;
    private DatabaseReference updateRef;

    public AppUpdateChecker(Context context) {
        this.context = context;
        this.updateRef = FirebaseDatabase.getInstance().getReference("app_version");
    }

    /**
     * Check for app updates and show dialog if update is available
     */
    public void checkForUpdate() {
        updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Integer latestVersionCode = dataSnapshot.child("version_code").getValue(Integer.class);
                        String latestVersionName = dataSnapshot.child("version_name").getValue(String.class);
                        String updateMessage = dataSnapshot.child("update_message").getValue(String.class);
                        Boolean forceUpdate = dataSnapshot.child("force_update").getValue(Boolean.class);

                        if (latestVersionCode != null) {
                            int currentVersionCode = getCurrentVersionCode();

                            if (latestVersionCode > currentVersionCode) {
                                showUpdateDialog(
                                        latestVersionName != null ? latestVersionName : "New Version",
                                        updateMessage != null ? updateMessage
                                                : "A new version is available. Please update to continue using the app.",
                                        forceUpdate != null ? forceUpdate : false);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking for update", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Update check cancelled", databaseError.toException());
            }
        });
    }

    /**
     * Get current app version code
     */
    private int getCurrentVersionCode() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version code", e);
            return 0;
        }
    }

    /**
     * Get current app version name
     */
    private String getCurrentVersionName() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version name", e);
            return "Unknown";
        }
    }

    /**
     * Show update dialog to user - BLOCKING until user updates
     */
    private void showUpdateDialog(String versionName, String message, boolean forceUpdate) {
        // Always force update for any new version
        forceUpdate = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("🚀 Update Required");
        builder.setMessage(
                message + "\n\n" +
                        "New Version: " + versionName + "\n" +
                        "Current Version: " + getCurrentVersionName() + "\n\n" +
                        "⚠️ You must update to continue using the app.");

        // Make dialog non-cancelable
        builder.setCancelable(false);

        builder.setPositiveButton("Update Now", (dialog, which) -> {
            // Redirect to web download page
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_URL));
            context.startActivity(browserIntent);

            // Close the app - user must manually install update
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finishAffinity();
                System.exit(0);
            }
        });

        // No "Later" button - user MUST update

        AlertDialog dialog = builder.create();

        // Prevent dialog from being dismissed by back button or outside touch
        dialog.setCanceledOnTouchOutside(false);

        try {
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing update dialog", e);
        }
    }

    /**
     * Manually set the latest version in Firebase (for admin use)
     */
    public static void setLatestVersion(int versionCode, String versionName, String updateMessage,
            boolean forceUpdate) {
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("app_version");

        updateRef.child("version_code").setValue(versionCode);
        updateRef.child("version_name").setValue(versionName);
        updateRef.child("update_message").setValue(updateMessage);
        updateRef.child("force_update").setValue(forceUpdate);
        updateRef.child("last_updated").setValue(System.currentTimeMillis());
    }
}
