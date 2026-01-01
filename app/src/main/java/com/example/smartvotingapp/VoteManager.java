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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteManager {
    private static final String TAG = "VoteManager";
    private Context context;
    private DatabaseReference databaseReference;
    private List<VoteRecord> cachedVotes = new ArrayList<>();
    private List<VoteUpdateListener> listeners = new ArrayList<>();
    private boolean isDataLoaded = false;

    public interface VoteUpdateListener {
        void onVotesUpdated();
    }

    public VoteManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("votes");

        // Start listening for real-time updates
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedVotes.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        VoteRecord vote = child.getValue(VoteRecord.class);
                        if (vote != null) {
                            cachedVotes.add(vote);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing vote: " + e.getMessage());
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

    public void addListener(VoteUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // If we already have data, notify immediately
        if (isDataLoaded) {
            listener.onVotesUpdated();
        }
    }

    public void removeListener(VoteUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (VoteUpdateListener listener : listeners) {
            listener.onVotesUpdated();
        }
    }

    public boolean hasUserVoted(String aadhaarId, int electionId) {
        for (VoteRecord vote : cachedVotes) {
            if (vote.getAadhaarId().equals(aadhaarId) && vote.getElectionId() == electionId) {
                return true;
            }
        }
        return false;
    }

    public void recordVote(VoteRecord vote) {
        // Push to Firebase; this will trigger onDataChange which updates local cache
        String key = databaseReference.push().getKey();
        if (key != null) {
            databaseReference.child(key).setValue(vote)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Vote recorded successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to record vote", e));
        }
    }

    public List<VoteRecord> getVotesByElection(int electionId) {
        List<VoteRecord> result = new ArrayList<>();
        for (VoteRecord vote : cachedVotes) {
            if (vote.getElectionId() == electionId) {
                result.add(vote);
            }
        }
        return result;
    }

    public Map<String, Integer> getVoteCountsByElection(int electionId) {
        Map<String, Integer> counts = new HashMap<>();
        List<VoteRecord> votes = getVotesByElection(electionId);

        for (VoteRecord vote : votes) {
            String optionId = vote.getOptionId();
            counts.put(optionId, counts.getOrDefault(optionId, 0) + 1);
        }
        return counts;
    }

    public List<VoteRecord> getAllVotes() {
        return new ArrayList<>(cachedVotes);
    }
}
