package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseFeedbackManager {
    private static final String TAG = "FirebaseFeedbackManager";
    private DatabaseReference feedbackRef;
    private Context context;

    public FirebaseFeedbackManager(Context context) {
        this.context = context;
        this.feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");
    }

    /**
     * Add new feedback to Firebase
     */
    public void addFeedback(Feedback feedback, OnFeedbackOperationListener listener) {
        String feedbackId = feedback.getId();

        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("id", feedback.getId());
        feedbackMap.put("userId", feedback.getUserId());
        feedbackMap.put("userName", feedback.getUserName());
        feedbackMap.put("userAadhaar", feedback.getUserAadhaar());
        feedbackMap.put("userState", feedback.getUserState());
        feedbackMap.put("title", feedback.getTitle());
        feedbackMap.put("description", feedback.getDescription());
        feedbackMap.put("status", feedback.getStatus());
        feedbackMap.put("adminResponse", feedback.getAdminResponse());
        feedbackMap.put("timestamp", feedback.getTimestamp());
        feedbackMap.put("resolvedTimestamp", feedback.getResolvedTimestamp());

        feedbackRef.child(feedbackId).setValue(feedbackMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Feedback added successfully");
                    if (listener != null)
                        listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add feedback", e);
                    if (listener != null)
                        listener.onFailure(e.getMessage());
                });
    }

    /**
     * Get all feedback (for super admin - ECI-INDIA)
     */
    public void getAllFeedback(OnFeedbackListListener listener) {
        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Feedback> feedbackList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Feedback feedback = parseFeedback(snapshot);
                    if (feedback != null) {
                        feedbackList.add(feedback);
                    }
                }
                if (listener != null)
                    listener.onSuccess(feedbackList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to get feedback", databaseError.toException());
                if (listener != null)
                    listener.onFailure(databaseError.getMessage());
            }
        });
    }

    /**
     * Get feedback by state (for state admins)
     */
    public void getFeedbackByState(String state, OnFeedbackListListener listener) {
        feedbackRef.orderByChild("userState").equalTo(state)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Feedback> feedbackList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Feedback feedback = parseFeedback(snapshot);
                            if (feedback != null) {
                                feedbackList.add(feedback);
                            }
                        }
                        if (listener != null)
                            listener.onSuccess(feedbackList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get feedback by state", databaseError.toException());
                        if (listener != null)
                            listener.onFailure(databaseError.getMessage());
                    }
                });
    }

    /**
     * Get feedback by user ID
     */
    public void getFeedbackByUserId(String userId, OnFeedbackListListener listener) {
        feedbackRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Feedback> feedbackList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Feedback feedback = parseFeedback(snapshot);
                            if (feedback != null) {
                                feedbackList.add(feedback);
                            }
                        }
                        if (listener != null)
                            listener.onSuccess(feedbackList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get user feedback", databaseError.toException());
                        if (listener != null)
                            listener.onFailure(databaseError.getMessage());
                    }
                });
    }

    /**
     * Update feedback (for admin responses)
     */
    public void updateFeedback(Feedback feedback, OnFeedbackOperationListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", feedback.getStatus());
        updates.put("adminResponse", feedback.getAdminResponse());
        updates.put("resolvedTimestamp", feedback.getResolvedTimestamp());

        feedbackRef.child(feedback.getId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Feedback updated successfully");
                    if (listener != null)
                        listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update feedback", e);
                    if (listener != null)
                        listener.onFailure(e.getMessage());
                });
    }

    /**
     * Delete feedback
     */
    public void deleteFeedback(String feedbackId, OnFeedbackOperationListener listener) {
        feedbackRef.child(feedbackId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Feedback deleted successfully");
                    if (listener != null)
                        listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete feedback", e);
                    if (listener != null)
                        listener.onFailure(e.getMessage());
                });
    }

    /**
     * Parse feedback from Firebase snapshot
     */
    private Feedback parseFeedback(DataSnapshot snapshot) {
        try {
            String id = snapshot.child("id").getValue(String.class);
            String userId = snapshot.child("userId").getValue(String.class);
            String userName = snapshot.child("userName").getValue(String.class);
            String userAadhaar = snapshot.child("userAadhaar").getValue(String.class);
            String userState = snapshot.child("userState").getValue(String.class);
            String title = snapshot.child("title").getValue(String.class);
            String description = snapshot.child("description").getValue(String.class);
            String status = snapshot.child("status").getValue(String.class);
            String adminResponse = snapshot.child("adminResponse").getValue(String.class);
            Long timestamp = snapshot.child("timestamp").getValue(Long.class);
            Long resolvedTimestamp = snapshot.child("resolvedTimestamp").getValue(Long.class);

            return new Feedback(
                    id != null ? id : "",
                    userId != null ? userId : "",
                    userName != null ? userName : "",
                    userAadhaar != null ? userAadhaar : "",
                    userState != null ? userState : "",
                    title != null ? title : "",
                    description != null ? description : "",
                    status != null ? status : "pending",
                    adminResponse != null ? adminResponse : "",
                    timestamp != null ? timestamp : 0,
                    resolvedTimestamp != null ? resolvedTimestamp : 0);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing feedback", e);
            return null;
        }
    }

    // Callback interfaces
    public interface OnFeedbackOperationListener {
        void onSuccess();

        void onFailure(String error);
    }

    public interface OnFeedbackListListener {
        void onSuccess(List<Feedback> feedbackList);

        void onFailure(String error);
    }
}
