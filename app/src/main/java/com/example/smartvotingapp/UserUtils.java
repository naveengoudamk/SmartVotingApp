package com.example.smartvotingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserUtils {

    private static final String TAG = "UserUtils";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_AADHAAR = "aadhaar_id";
    private static final String KEY_DOB = "dob";

    public static void saveUserSession(Context context, String aadhaarId, String dob) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AADHAAR, aadhaarId)
                .putString(KEY_DOB, dob)
                .apply();
    }

    // Backward compatibility - save just aadhaar
    public static void saveUserSession(Context context, String aadhaarId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AADHAAR, aadhaarId)
                .apply();
    }

    public static void clearUserSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public static User getCurrentUser(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String storedAadhaar = prefs.getString(KEY_AADHAAR, null);
            String storedDob = prefs.getString(KEY_DOB, null);

            if (storedAadhaar == null) {
                Log.w(TAG, "No stored Aadhaar in session");
                return null;
            }

            Log.d(TAG, "Getting user from Firebase: " + storedAadhaar);

            // Get user from Firebase via UserManager
            UserManager userManager = new UserManager(context);

            // Wait for data to load (with timeout)
            int maxWaitTime = 5000; // 5 seconds max
            int waitedTime = 0;
            int sleepInterval = 100; // Check every 100ms

            while (!userManager.isDataLoaded() && waitedTime < maxWaitTime) {
                try {
                    Thread.sleep(sleepInterval);
                    waitedTime += sleepInterval;
                } catch (InterruptedException e) {
                    Log.e(TAG, "Sleep interrupted", e);
                    break;
                }
            }

            if (!userManager.isDataLoaded()) {
                Log.e(TAG, "Firebase data not loaded after " + maxWaitTime + "ms");
                return null;
            }

            Log.d(TAG, "Firebase data loaded, searching for user...");

            // If we have DOB, use it for lookup
            if (storedDob != null) {
                User user = userManager.getUser(storedAadhaar, storedDob);
                if (user != null) {
                    Log.d(TAG, "User found: " + user.getName());
                    return user;
                }
            }

            // Otherwise, search through all users (less efficient but works)
            for (User user : userManager.getAllUsers()) {
                if (user.getAadhaarId().equals(storedAadhaar)) {
                    Log.d(TAG, "User found (without DOB match): " + user.getName());
                    return user;
                }
            }

            Log.w(TAG, "User not found in Firebase: " + storedAadhaar);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            e.printStackTrace();
            return null;
        }
    }

    public static int calculateAge(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date birthDate = sdf.parse(dob);
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isEligibleToVote(String dob) {
        return calculateAge(dob) >= 18;
    }
}
