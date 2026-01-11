package com.example.smartvotingapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationHelper {
    private static final String PREF_NAME = "SmartVotingNotifications";
    private static final String KEY_LAST_CHECK = "last_check_timestamp";

    private Context context;
    private SharedPreferences prefs;
    private NewsManager newsManager;
    private ElectionManager electionManager;
    private FeedbackManager feedbackManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.newsManager = new NewsManager(context);
        this.electionManager = new ElectionManager(context);
        this.feedbackManager = new FeedbackManager(context);
    }

    public long getLastCheckTimestamp() {
        return prefs.getLong(KEY_LAST_CHECK, 0);
    }

    public void markAllAsRead() {
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
    }

    public List<NotificationItem> getAllNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        long lastCheck = getLastCheckTimestamp();

        // 1. News Notifications
        List<News> allNews = newsManager.getAllNews();
        for (News news : allNews) {
            // Assuming News has a timestamp field. If not, we might need to parse date
            // string.
            // Based on previous file view, News has getTimestamp()
            notifications.add(new NotificationItem(
                    NotificationItem.TYPE_NEWS,
                    "New Announcement: " + news.getTitle(),
                    news.getDescription(),
                    news.getTimestamp(),
                    news.getId()));
        }

        // 2. Election Notifications (Active Elections)
        List<Election> elections = electionManager.getAllElections();
        for (Election election : elections) {
            if ("Active".equalsIgnoreCase(election.getStatus())) {
                // Use current time or a fixed time if we don't have creation time
                // For sorting, we'll put them at the top or based on ID
                notifications.add(new NotificationItem(
                        NotificationItem.TYPE_ELECTION,
                        "Election Live: " + election.getTitle(),
                        "Voting is now open for " + election.getState(),
                        System.currentTimeMillis(), // Placeholder as we don't have creation time
                        String.valueOf(election.getId())));
            }

            // 2.1 Election Result Notifications
            if (election.getResultDate() != null && !election.getResultDate().isEmpty()) {
                long timestamp = System.currentTimeMillis();
                try {
                    // Try standard format first, fallback to current time
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd",
                            java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(election.getResultDate());
                    if (date != null)
                        timestamp = date.getTime();
                } catch (Exception e) {
                    // fall back to current
                }

                notifications.add(new NotificationItem(
                        NotificationItem.TYPE_RESULT,
                        "Results Announced",
                        "Results for " + election.getTitle() + " have been declared!",
                        timestamp,
                        String.valueOf(election.getId())));
            }
        }

        // 3. Feedback Notifications (Resolved)
        User user = UserUtils.getCurrentUser(context);
        if (user != null) {
            List<Feedback> feedbackList = feedbackManager.getFeedbackByUserId(user.getAadhaarId());
            for (Feedback feedback : feedbackList) {
                if ("resolved".equalsIgnoreCase(feedback.getStatus())) {
                    notifications.add(new NotificationItem(
                            NotificationItem.TYPE_FEEDBACK,
                            "Feedback Resolved",
                            "Admin responded: " + feedback.getAdminResponse(),
                            feedback.getResolvedTimestamp(),
                            feedback.getId()));
                }
            }
        }

        // Sort by timestamp descending
        Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

        return notifications;
    }

    public int getUnreadCount() {
        long lastCheck = getLastCheckTimestamp();
        List<NotificationItem> all = getAllNotifications();
        int count = 0;
        for (NotificationItem item : all) {
            if (item.getTimestamp() > lastCheck) {
                count++;
            }
        }
        return count;
    }
}
