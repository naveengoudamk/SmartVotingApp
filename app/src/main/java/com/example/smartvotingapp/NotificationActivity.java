package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;
    private LinearLayout layoutEmptyState;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationContainer = findViewById(R.id.notificationContainer);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        ImageView btnBack = findViewById(R.id.btnBack);

        notificationHelper = new NotificationHelper(this);

        btnBack.setOnClickListener(v -> finish());

        loadNotifications();

        // Mark as read after loading
        notificationHelper.markAllAsRead();
    }

    private void loadNotifications() {
        notificationContainer.removeAllViews();
        List<NotificationItem> notifications = notificationHelper.getAllNotifications();

        if (notifications.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            for (NotificationItem item : notifications) {
                addNotificationCard(item);
            }
        }
    }

    private void addNotificationCard(NotificationItem item) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_notification_card, notificationContainer,
                false);

        TextView tvTitle = cardView.findViewById(R.id.tvNotificationTitle);
        TextView tvMessage = cardView.findViewById(R.id.tvNotificationMessage);
        TextView tvDate = cardView.findViewById(R.id.tvNotificationDate);
        TextView tvSender = cardView.findViewById(R.id.tvNotificationSender);

        tvTitle.setText(item.getTitle());
        tvMessage.setText(item.getMessage());

        String sender = "System";
        if (item.getType() == NotificationItem.TYPE_NEWS)
            sender = "Admin";
        else if (item.getType() == NotificationItem.TYPE_ELECTION)
            sender = "Election Commission";
        else if (item.getType() == NotificationItem.TYPE_FEEDBACK)
            sender = "Support Team";
        else if (item.getType() == NotificationItem.TYPE_RESULT)
            sender = "Election Commission";

        tvSender.setText("From: " + sender);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(item.getTimestamp())));

        cardView.setOnClickListener(v -> handleNotificationClick(item));

        notificationContainer.addView(cardView);
    }

    private void handleNotificationClick(NotificationItem item) {
        boolean isAdmin = getIntent().getBooleanExtra("is_admin", false);
        String deptCode = getIntent().getStringExtra("dept_code");

        Intent intent;
        if (isAdmin) {
            intent = new Intent(this, AdminDashboardActivity.class);
            intent.putExtra("dept_code", deptCode);
            // Verify if deptCode is null, maybe handle gracefully, but we pass what we got
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("target_id", item.getReferenceId());

        if (item.getType() == NotificationItem.TYPE_NEWS) {
            intent.putExtra("navigate_to", "home");
        } else if (item.getType() == NotificationItem.TYPE_ELECTION) {
            intent.putExtra("navigate_to", isAdmin ? "election" : "vote"); // Admin maps to election, User to vote
        } else if (item.getType() == NotificationItem.TYPE_FEEDBACK) {
            // Feedback for Admin? Maybe AdminFeedbackFragment
            intent.putExtra("navigate_to", isAdmin ? "home" : "account"); // Admin home has recent feedback? or default
            // Admin users don't get Feedback notifications usually (users get them).
            // The item created in NotificationHelper: "Admin responded: ..." -> User
            // notification.
            // But if Admin gets one, default to home.
        } else if (item.getType() == NotificationItem.TYPE_RESULT) {
            intent.putExtra("navigate_to", isAdmin ? "result" : "history");
        }

        startActivity(intent);
        finish();
    }
}
