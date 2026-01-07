package com.example.smartvotingapp;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackManager {
    private static final String FEEDBACK_FILE = "feedback_data.json";
    private Context context;

    public FeedbackManager(Context context) {
        this.context = context;
    }

    // Add new feedback
    public boolean addFeedback(Feedback feedback) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            feedbackList.add(feedback);
            boolean saved = saveFeedbackList(feedbackList);

            // Also sync to Firebase
            if (saved) {
                FirebaseFeedbackManager firebaseManager = new FirebaseFeedbackManager(context);
                firebaseManager.addFeedback(feedback, null);
            }

            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all feedback
    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FEEDBACK_FILE);
            if (!file.exists()) {
                return feedbackList;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Feedback feedback = new Feedback(
                        obj.getString("id"),
                        obj.getString("userId"),
                        obj.getString("userName"),
                        obj.getString("userAadhaar"),
                        obj.optString("userState", ""),
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("status"),
                        obj.optString("adminResponse", ""),
                        obj.getLong("timestamp"),
                        obj.optLong("resolvedTimestamp", 0));
                feedbackList.add(feedback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    // Get feedback by user ID
    public List<Feedback> getFeedbackByUserId(String userId) {
        List<Feedback> userFeedback = new ArrayList<>();
        List<Feedback> allFeedback = getAllFeedback();

        for (Feedback feedback : allFeedback) {
            if (feedback.getUserId().equals(userId)) {
                userFeedback.add(feedback);
            }
        }
        return userFeedback;
    }

    // Get feedback by ID
    public Feedback getFeedbackById(String id) {
        List<Feedback> feedbackList = getAllFeedback();
        for (Feedback feedback : feedbackList) {
            if (feedback.getId().equals(id)) {
                return feedback;
            }
        }
        return null;
    }

    // Update feedback (for admin response)
    public boolean updateFeedback(Feedback updatedFeedback) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            for (int i = 0; i < feedbackList.size(); i++) {
                if (feedbackList.get(i).getId().equals(updatedFeedback.getId())) {
                    feedbackList.set(i, updatedFeedback);
                    return saveFeedbackList(feedbackList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete feedback
    public boolean deleteFeedback(String id) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            feedbackList.removeIf(feedback -> feedback.getId().equals(id));
            return saveFeedbackList(feedbackList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get count of unresolved feedback for user
    public int getUnresolvedCount(String userId) {
        int count = 0;
        List<Feedback> userFeedback = getFeedbackByUserId(userId);
        for (Feedback feedback : userFeedback) {
            if (!feedback.getStatus().equals("resolved")) {
                count++;
            }
        }
        return count;
    }

    // Get count of newly resolved feedback (for notifications)
    public int getNewlyResolvedCount(String userId, long lastCheckTimestamp) {
        int count = 0;
        List<Feedback> userFeedback = getFeedbackByUserId(userId);
        for (Feedback feedback : userFeedback) {
            if (feedback.getStatus().equals("resolved") &&
                    feedback.getResolvedTimestamp() > lastCheckTimestamp) {
                count++;
            }
        }
        return count;
    }

    // Save feedback list to file
    private boolean saveFeedbackList(List<Feedback> feedbackList) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Feedback feedback : feedbackList) {
                JSONObject obj = new JSONObject();
                obj.put("id", feedback.getId());
                obj.put("userId", feedback.getUserId());
                obj.put("userName", feedback.getUserName());
                obj.put("userAadhaar", feedback.getUserAadhaar());
                obj.put("userState", feedback.getUserState());
                obj.put("title", feedback.getTitle());
                obj.put("description", feedback.getDescription());
                obj.put("status", feedback.getStatus());
                obj.put("adminResponse", feedback.getAdminResponse());
                obj.put("timestamp", feedback.getTimestamp());
                obj.put("resolvedTimestamp", feedback.getResolvedTimestamp());
                jsonArray.put(obj);
            }

            File file = new File(context.getFilesDir(), FEEDBACK_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
