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

    public static User getCurrentUser(Context context) {
        try {
            InputStream is = context.getAssets().open("aadhaar_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray arr = new JSONArray(json);
            JSONObject obj = arr.getJSONObject(0); // first user (Ravi Kumar)

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
                    obj.getBoolean("eligible")
            );

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
