package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VotingOptionManager {
    private static final String TAG = "VotingOptionManager";
    private Context context;
    private DatabaseReference databaseReference;
    private List<VotingOption> cachedOptions = new ArrayList<>();
    private List<VotingOptionUpdateListener> listeners = new ArrayList<>();
    private boolean isDataLoaded = false;

    public interface VotingOptionUpdateListener {
        void onVotingOptionsUpdated();
    }

    public VotingOptionManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("voting_options");

        // Start listening for real-time updates
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedOptions.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        VotingOption option = child.getValue(VotingOption.class);
                        if (option != null) {
                            cachedOptions.add(option);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing voting option: " + e.getMessage());
                    }
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

    public void addListener(VotingOptionUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // If we already have data, notify immediately
        if (isDataLoaded) {
            listener.onVotingOptionsUpdated();
        }
    }

    public void removeListener(VotingOptionUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (VotingOptionUpdateListener listener : listeners) {
            listener.onVotingOptionsUpdated();
        }
    }

    public List<VotingOption> getOptionsByElection(int electionId) {
        List<VotingOption> options = new ArrayList<>();
        for (VotingOption option : cachedOptions) {
            if (option.getElectionId() == electionId) {
                options.add(option);
            }
        }
        return options;
    }

    public List<VotingOption> getAllOptions() {
        return new ArrayList<>(cachedOptions);
    }

    public void addOption(VotingOption option) {
        // Use option ID as the key
        databaseReference.child(option.getId()).setValue(option)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Voting option added successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add voting option", e));
    }

    public void updateOption(VotingOption option) {
        databaseReference.child(option.getId()).setValue(option)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Voting option updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update voting option", e));
    }

    public void deleteOption(String optionId) {
        databaseReference.child(optionId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Voting option deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete voting option", e));
    }
}
