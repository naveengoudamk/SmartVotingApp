package com.example.smartvotingapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserUtils {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_AADHAAR = "aadhaar_id";

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
            String storedAadhaar = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_AADHAAR, null);

            if (storedAadhaar == null)
                return null;

            InputStream is = context.getAssets().open("aadhaar_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("aadhaar_id").equals(storedAadhaar)) {
                    return new User(
                            obj.getString("aadhaar_id"),
                            obj.getString("name"),
                            obj.getString("dob"),
                            obj.getString("email"),
                            obj.getString("mobile"),
                            obj.getString("photo"),
                            obj.getString("address"),
                            obj.getString("city"),
                            obj.getString("state"),
                            obj.getString("pincode"),
                            obj.getBoolean("eligible"));
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
}
