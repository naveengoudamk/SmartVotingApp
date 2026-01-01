package com.example.smartvotingapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserUtils {

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
                return null;
            }

            // Get user from Firebase via UserManager
            UserManager userManager = new UserManager(context);

            // If we have DOB, use it for lookup
            if (storedDob != null) {
                return userManager.getUser(storedAadhaar, storedDob);
            }

            // Otherwise, search through all users (less efficient but works)
            for (User user : userManager.getAllUsers()) {
                if (user.getAadhaarId().equals(storedAadhaar)) {
                    return user;
                }
            }

            return null;

        } catch (Exception e) {
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
