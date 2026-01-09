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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class UserManager {
    private static final String TAG = "UserManager";
    private static final String FILE_NAME = "aadhaar_data.json";
    private Context context;
    private DatabaseReference databaseReference;

    // Separate maps for robust data handling
    // 1. Immutable map loaded from assets (Backup/Test data)
    private Map<String, User> assetUserMap = new HashMap<>();
    // 2. Dynamic map loaded from Firebase (Live data)
    private Map<String, User> firebaseUserMap = new HashMap<>();

    private List<UserUpdateListener> listeners = new ArrayList<>();
    private boolean isDataLoaded = false;

    public interface UserUpdateListener {
        void onUsersUpdated();
    }

    public UserManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // 1. Load local assets immediately (synchronously so it's ready for login)
        loadFromAssets();

        // 2. Listen for Firebase updates (asynchronous overlay)
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            User user = child.getValue(User.class);
                            if (user != null) {
                                firebaseUserMap.put(user.getAadhaarId(), user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Firebase data loaded. Count: " + firebaseUserMap.size());
                }

                isDataLoaded = true;
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                // Even on error, we have local data, so keep isDataLoaded = true
                isDataLoaded = true;
            }
        });
    }

    public void addListener(UserUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        if (isDataLoaded) {
            listener.onUsersUpdated();
        }
    }

    public void removeListener(UserUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (UserUpdateListener listener : listeners) {
            listener.onUsersUpdated();
        }
    }

    private void loadFromAssets() {
        try {
            InputStream is = context.getAssets().open(FILE_NAME);
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            int count = 0;

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

                // Add to ASSET map (Immutable)
                assetUserMap.put(user.getAadhaarId(), user);
                count++;
            }
            Log.d(TAG, "Loaded " + count + " users from assets (Immutable).");

        } catch (Exception e) {
            Log.e(TAG, "Error loading assets", e);
        }

        // FAIL-SAFE: Explicitly add Test Users (Guarantees these credentials ALWAYS
        // work)
        // 1. Ramesh Gowda (Standard Test User)
        User ramesh = new User("112233445566", "Ramesh Gowda", "1990-01-01",
                "ramesh@example.com", "9900112233", "ramesh.jpg",
                "123 MG Road", "Bengaluru", "Karnataka", "560038", true);
        assetUserMap.put(ramesh.getAadhaarId(), ramesh);

        // 2. Naveen Gouda (Admin/Dev)
        User naveen = new User("123456789102", "Naveen Gouda", "2004-01-01",
                "naveen@example.com", "7766554433", "sneha.jpg",
                "Hubli Main Rd", "Hubli", "Karnataka", "580020", true);
        assetUserMap.put(naveen.getAadhaarId(), naveen);

        Log.d(TAG, "Hardcoded fail-safe users added: Ramesh & Naveen");
        isDataLoaded = true; // Local data is ready
    }

    public List<User> getAllUsers() {
        // Merge for display: Firebase overrides Assets
        Map<String, User> merged = new HashMap<>(assetUserMap);
        merged.putAll(firebaseUserMap);
        return new ArrayList<>(merged.values());
    }

    public void updateUser(User updatedUser) {
        // Update local cache
        firebaseUserMap.put(updatedUser.getAadhaarId(), updatedUser);
        notifyListeners();

        // Update Firebase (Source of Truth for changes)
        Log.d(TAG, "Updating user: " + updatedUser.getAadhaarId());
        databaseReference.child(updatedUser.getAadhaarId()).setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User updated successfully: " + updatedUser.getAadhaarId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to update user: " + e.getMessage(), e);
                });
    }

    public void addUser(User newUser) {
        if (getUser(newUser.getAadhaarId(), newUser.getDob()) != null) {
            Log.w(TAG, "User already exists: " + newUser.getAadhaarId());
            return;
        }

        // Update local cache
        firebaseUserMap.put(newUser.getAadhaarId(), newUser);
        notifyListeners();

        Log.d(TAG, "Adding new user: " + newUser.getAadhaarId());
        databaseReference.child(newUser.getAadhaarId()).setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ User added successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to add user: " + e.getMessage(), e);
                });
    }

    public void deleteUser(String aadhaarId) {
        // Soft delete: Update Firebase with deleted=true
        // We first need to get the current user object to preserve other fields
        User userToDelete = firebaseUserMap.get(aadhaarId);
        if (userToDelete == null) {
            userToDelete = assetUserMap.get(aadhaarId);
        }

        if (userToDelete != null) {
            userToDelete.setDeleted(true);
            updateUser(userToDelete); // This saves to Firebase
            Log.d(TAG, "User marked as deleted: " + aadhaarId);
        } else {
            // Create a skeleton user just to mark deleted if not found (rare)
            User skeleton = new User();
            skeleton.setAadhaarId(aadhaarId);
            skeleton.setDeleted(true);
            updateUser(skeleton);
        }
    }

    /**
     * Robust User Retrieval Strategy:
     * 1. Check local assets & Firebase.
     * 2. CRITICAL: If *EITHER* source says "deleted=true", BLOCK LOGIN.
     * (Firebase takes precedence for status updates).
     */
    public User getUser(String aadhaar, String dob) {
        if (aadhaar == null || dob == null)
            return null;

        String a = aadhaar.trim();
        String d = dob.trim();

        // Check if user is BANNED/DELETED in Firebase (Source of Truth for status)
        User firebaseUser = firebaseUserMap.get(a);
        if (firebaseUser != null && firebaseUser.isDeleted()) {
            Log.w(TAG, "Login blocked: User is deleted. " + a);
            return null;
        }

        // 1. Check ASSETS first
        User assetUser = assetUserMap.get(a);
        if (assetUser != null && assetUser.getDob().trim().equals(d)) {
            // Even if found in assets, double check we didn't miss a ban
            // (Handled above by checking firebaseUserMap.get(a) first)
            Log.d(TAG, "User found in assets: " + a);
            return assetUser;
        }

        // 2. Check FIREBASE second
        if (firebaseUser != null && firebaseUser.getDob().trim().equals(d)) {
            Log.d(TAG, "User found in Firebase: " + a);
            return firebaseUser;
        }

        Log.w(TAG, "User not found locally or in Firebase: " + a);
        return null;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }
}
