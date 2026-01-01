package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserManager {
    private static final String TAG = "UserManager";
    private static final String FILE_NAME = "aadhaar_data.json";
    private Context context;
    private DatabaseReference databaseReference;
    private List<User> cachedUsers = new ArrayList<>();
    private boolean isDataLoaded = false;

    public UserManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Listen for data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedUsers.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            User user = child.getValue(User.class);
                            if (user != null) {
                                cachedUsers.add(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + e.getMessage());
                        }
                    }
                    isDataLoaded = true;
                    Log.d(TAG, "Users loaded from Firebase: " + cachedUsers.size());
                } else {
                    // Firebase is empty, seed from assets
                    Log.d(TAG, "Firebase empty. Seeding from assets...");
                    seedFromAssets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void seedFromAssets() {
        try {
            InputStream is = context.getAssets().open(FILE_NAME);
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            List<User> initialUsers = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                User user = new User(
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
                        obj.optBoolean("eligible", true));

                initialUsers.add(user);
                // Upload individually (efficient enough for seeding)
                databaseReference.child(user.getAadhaarId()).setValue(user);
            }
            Log.d(TAG, "Seeded " + initialUsers.size() + " users to Firebase.");

        } catch (Exception e) {
            Log.e(TAG, "Error seeding assets", e);
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(cachedUsers);
    }

    public void updateUser(User updatedUser) {
        // Optimistic update
        for (int i = 0; i < cachedUsers.size(); i++) {
            if (cachedUsers.get(i).getAadhaarId().equals(updatedUser.getAadhaarId())) {
                cachedUsers.set(i, updatedUser);
                break;
            }
        }
        databaseReference.child(updatedUser.getAadhaarId()).setValue(updatedUser);
    }

    public void addUser(User newUser) {
        // Check if exists
        for (User u : cachedUsers) {
            if (u.getAadhaarId().equals(newUser.getAadhaarId())) {
                Log.w(TAG, "User already exists");
                return;
            }
        }
        databaseReference.child(newUser.getAadhaarId()).setValue(newUser);
    }

    public User getUser(String aadhaar, String dob) {
        for (User u : cachedUsers) {
            if (u.getAadhaarId().equals(aadhaar) && u.getDob().equals(dob)) {
                return u;
            }
        }
        return null;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }
}
