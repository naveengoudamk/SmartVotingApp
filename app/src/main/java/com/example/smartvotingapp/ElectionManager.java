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

public class ElectionManager {
    private static final String TAG = "ElectionManager";
    private Context context;
    private DatabaseReference databaseReference;
    private List<Election> cachedElections = new ArrayList<>();
    private List<ElectionUpdateListener> listeners = new ArrayList<>();
    private boolean isDataLoaded = false;
    private boolean defaultsLoaded = false;

    public interface ElectionUpdateListener {
        void onElectionsUpdated();
    }

    public ElectionManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("elections");

        // Start listening for real-time updates
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedElections.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Election election = child.getValue(Election.class);
                        if (election != null) {
                            cachedElections.add(election);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing election: " + e.getMessage());
                    }
                }

                // If no elections found and defaults not loaded, load from assets
                if (cachedElections.isEmpty() && !defaultsLoaded) {
                    loadDefaultElections();
                    defaultsLoaded = true;
                }

                isDataLoaded = true;
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    public void addListener(ElectionUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // If we already have data, notify immediately
        if (isDataLoaded) {
            listener.onElectionsUpdated();
        }
    }

    public void removeListener(ElectionUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ElectionUpdateListener listener : listeners) {
            listener.onElectionsUpdated();
        }
    }

    public List<Election> getAllElections() {
        return new ArrayList<>(cachedElections);
    }

    public void addElection(Election election) {
        String key = String.valueOf(election.getId());
        databaseReference.child(key).setValue(election)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Election added successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add election", e));
    }

    public void updateElection(Election election) {
        String key = String.valueOf(election.getId());
        databaseReference.child(key).setValue(election)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Election updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update election", e));
    }

    public void deleteElection(int id) {
        String key = String.valueOf(id);
        databaseReference.child(key).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Election deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete election", e));
    }

    private void loadDefaultElections() {
        try {
            InputStream is = context.getAssets().open("elections_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Election election = new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("min_age"),
                        obj.getString("status"),
                        obj.optString("stopDate", ""),
                        obj.optString("resultDate", null));

                // Add to Firebase
                String key = String.valueOf(election.getId());
                databaseReference.child(key).setValue(election);
            }
            Log.d(TAG, "Default elections loaded to Firebase");
        } catch (Exception e) {
            Log.e(TAG, "Error reading default elections", e);
        }
    }
}
