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
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CITY = "city";
    private static final String KEY_STATE = "state";
    private static final String KEY_PINCODE = "pincode";
    private static final String KEY_ELIGIBLE = "eligible";

    public static void saveUserSession(Context context, User user) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_AADHAAR, user.getAadhaarId());
        editor.putString(KEY_DOB, user.getDob());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_MOBILE, user.getMobile());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_CITY, user.getCity());
        editor.putString(KEY_STATE, user.getState());
        editor.putString(KEY_PINCODE, user.getPincode());
        editor.putBoolean(KEY_ELIGIBLE, user.isEligible());
        editor.apply();
        Log.d(TAG, "User session saved: " + user.getName());
    }

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

            if (storedAadhaar == null) {
                Log.w(TAG, "No stored Aadhaar in session");
                return null;
            }

            // Try to get from cached session first (fast)
            String name = prefs.getString(KEY_NAME, null);
            if (name != null) {
                // We have full user data cached in SharedPreferences
                User user = new User(
                        storedAadhaar,
                        name,
                        prefs.getString(KEY_DOB, ""),
                        prefs.getString(KEY_EMAIL, ""),
                        prefs.getString(KEY_MOBILE, ""),
                        "",
                        prefs.getString(KEY_ADDRESS, ""),
                        prefs.getString(KEY_CITY, ""),
                        prefs.getString(KEY_STATE, ""),
                        prefs.getString(KEY_PINCODE, ""),
                        prefs.getBoolean(KEY_ELIGIBLE, false));
                Log.d(TAG, "User loaded from cache: " + user.getName());
                return user;
            }

            Log.w(TAG, "User not found in cache: " + storedAadhaar);
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
