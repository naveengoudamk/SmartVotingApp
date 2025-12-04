package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserManager {
    private static final String FILE_NAME = "aadhaar_data.json";
    private Context context;

    public UserManager(Context context) {
        this.context = context;
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try {
                InputStream is = context.getAssets().open(FILE_NAME);
                Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
                String json = scanner.useDelimiter("\\A").next();
                scanner.close();
                saveJsonToFile(json);
            } catch (IOException e) {
                Log.e("UserManager", "Error copying asset to internal storage", e);
            }
        }
    }

    private void saveJsonToFile(String json) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            Log.e("UserManager", "Error saving JSON to file", e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                users.add(new User(
                        obj.getString("aadhaar_id"),
                        obj.getString("name"),
                        obj.getString("dob"),
                        obj.optString("email", ""),
                        obj.optString("mobile", ""),
                        obj.optString("photo", ""),
                        obj.optString("address", ""),
                        obj.optString("city", ""),
                        obj.optString("state", ""),
                        obj.optString("pincode", ""),
                        obj.optBoolean("eligible", true)
                ));
            }
        } catch (Exception e) {
            Log.e("UserManager", "Error reading users", e);
        }
        return users;
    }

    public void updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        JSONArray array = new JSONArray();
        for (User u : users) {
            JSONObject obj = new JSONObject();
            try {
                if (u.getAadhaarId().equals(updatedUser.getAadhaarId())) {
                    // Update this user
                    obj.put("aadhaar_id", updatedUser.getAadhaarId());
                    obj.put("name", updatedUser.getName());
                    obj.put("dob", updatedUser.getDob());
                    obj.put("email", updatedUser.getEmail());
                    obj.put("mobile", updatedUser.getMobile());
                    obj.put("photo", updatedUser.getPhoto());
                    obj.put("address", updatedUser.getAddress());
                    obj.put("city", updatedUser.getCity());
                    obj.put("state", updatedUser.getState());
                    obj.put("pincode", updatedUser.getPincode());
                    obj.put("eligible", updatedUser.isEligible());
                } else {
                    // Keep existing
                    obj.put("aadhaar_id", u.getAadhaarId());
                    obj.put("name", u.getName());
                    obj.put("dob", u.getDob());
                    obj.put("email", u.getEmail());
                    obj.put("mobile", u.getMobile());
                    obj.put("photo", u.getPhoto());
                    obj.put("address", u.getAddress());
                    obj.put("city", u.getCity());
                    obj.put("state", u.getState());
                    obj.put("pincode", u.getPincode());
                    obj.put("eligible", u.isEligible());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        saveJsonToFile(array.toString());
    }
}
