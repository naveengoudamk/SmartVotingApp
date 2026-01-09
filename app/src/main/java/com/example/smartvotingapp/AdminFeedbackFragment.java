package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminFeedbackFragment extends Fragment {

    private LinearLayout feedbackContainer;
    private LinearLayout layoutEmptyState;
    private FirebaseFeedbackManager feedbackManager;
    private Button btnFilterAll, btnFilterPending, btnFilterResolved;
    private String currentFilter = "all";
    private String adminState; // State of the logged-in admin

    public AdminFeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_feedback, container, false);

        feedbackManager = new FirebaseFeedbackManager(getContext());
        feedbackContainer = view.findViewById(R.id.feedbackContainer);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPending = view.findViewById(R.id.btnFilterPending);
        btnFilterResolved = view.findViewById(R.id.btnFilterResolved);

        // Get admin state from login data
        SharedPreferences prefs = getContext().getSharedPreferences("AdminSession", Context.MODE_PRIVATE);
        adminState = prefs.getString("admin_state", "");

        setupFilterButtons();
        loadFeedback();

        return view;
    }

    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtonStyles();
            loadFeedback();
        });

        btnFilterPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateFilterButtonStyles();
            loadFeedback();
        });

        btnFilterResolved.setOnClickListener(v -> {
            currentFilter = "resolved";
            updateFilterButtonStyles();
            loadFeedback();
        });
    }

    private void updateFilterButtonStyles() {
        // Reset all to inactive style
        setButtonInactive(btnFilterAll);
        setButtonInactive(btnFilterPending);
        setButtonInactive(btnFilterResolved);

        // Set active button style
        if (currentFilter.equals("all")) {
            setButtonActive(btnFilterAll);
        } else if (currentFilter.equals("pending")) {
            setButtonActive(btnFilterPending);
        } else if (currentFilter.equals("resolved")) {
            setButtonActive(btnFilterResolved);
        }
    }

    private void setButtonActive(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(android.R.color.white, null)));
        button.setTextColor(getResources().getColor(android.R.color.black, null));
        button.setElevation(2f);
    }

    private void setButtonInactive(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(android.R.color.transparent, null)));
        button.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        button.setElevation(0f);
    }

    private void loadFeedback() {
        feedbackContainer.removeAllViews();

        // Show loading state
        layoutEmptyState.setVisibility(View.VISIBLE);

        // Determine if this is super admin (ECI-INDIA) or state admin
        boolean isSuperAdmin = "ECI-INDIA".equals(adminState) || adminState == null || adminState.isEmpty();

        FirebaseFeedbackManager.OnFeedbackListListener listener = new FirebaseFeedbackManager.OnFeedbackListListener() {
            @Override
            public void onSuccess(List<Feedback> feedbackList) {
                // Filter based on current filter
                feedbackList.removeIf(feedback -> {
                    if (currentFilter.equals("pending")) {
                        return !feedback.getStatus().equals("pending") && !feedback.getStatus().equals("in_progress");
                    } else if (currentFilter.equals("resolved")) {
                        return !feedback.getStatus().equals("resolved");
                    }
                    return false;
                });

                if (feedbackList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                layoutEmptyState.setVisibility(View.GONE);

                // Sort by timestamp (newest first)
                feedbackList.sort((f1, f2) -> Long.compare(f2.getTimestamp(), f1.getTimestamp()));

                for (Feedback feedback : feedbackList) {
                    displayFeedbackCard(feedback);
                }
            }

            @Override
            public void onFailure(String error) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Failed to load feedback: " + error, Toast.LENGTH_SHORT).show();
            }
        };

        // Load feedback based on admin type
        if (isSuperAdmin) {
            feedbackManager.getAllFeedback(listener);
        } else {
            feedbackManager.getFeedbackByState(adminState, listener);
        }
    }

    private void displayFeedbackCard(Feedback feedback) {
        View feedbackView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_admin_feedback_card, feedbackContainer, false);

        TextView tvTitle = feedbackView.findViewById(R.id.tvAdminFeedbackTitle);
        TextView tvStatus = feedbackView.findViewById(R.id.tvAdminFeedbackStatus);
        TextView tvUser = feedbackView.findViewById(R.id.tvAdminFeedbackUser);
        TextView tvDescription = feedbackView.findViewById(R.id.tvAdminFeedbackDescription);
        TextView tvDate = feedbackView.findViewById(R.id.tvAdminFeedbackDate);
        LinearLayout layoutResponse = feedbackView.findViewById(R.id.layoutAdminResponsePreview);
        TextView tvResponse = feedbackView.findViewById(R.id.tvAdminResponsePreview);
        Button btnResolve = feedbackView.findViewById(R.id.btnResolveFeedback);
        Button btnDelete = feedbackView.findViewById(R.id.btnDeleteFeedback);

        tvTitle.setText(feedback.getTitle());
        tvDescription.setText(feedback.getDescription());

        // Format user info
        String maskedAadhaar = maskAadhaar(feedback.getUserAadhaar());
        String userInfo = feedback.getUserName() + " • " + maskedAadhaar;
        if (feedback.getUserState() != null && !feedback.getUserState().isEmpty()) {
            userInfo += " • " + feedback.getUserState();
        }
        tvUser.setText(userInfo);

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        tvDate.setText("Submitted on: " + sdf.format(new Date(feedback.getTimestamp())));

        // Set status
        updateStatusBadge(tvStatus, feedback.getStatus());

        // Show admin response if exists
        if (feedback.getAdminResponse() != null && !feedback.getAdminResponse().isEmpty()) {
            layoutResponse.setVisibility(View.VISIBLE);
            tvResponse.setText(feedback.getAdminResponse());
        }

        // Update button text based on status
        if (feedback.getStatus().equals("resolved")) {
            btnResolve.setText("View Details");
        } else {
            btnResolve.setText("Resolve");
        }

        btnResolve.setOnClickListener(v -> showResolveDialog(feedback));
        btnDelete.setOnClickListener(v -> deleteFeedback(feedback));

        feedbackContainer.addView(feedbackView);
    }

    private void updateStatusBadge(TextView badge, String status) {
        switch (status) {
            case "pending":
                badge.setText("Pending");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_orange_dark, null)));
                break;
            case "in_progress":
                badge.setText("In Progress");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_blue_light, null)));
                break;
            case "resolved":
                badge.setText("Resolved");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_green_dark, null)));
                break;
        }
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "XXXX-XXXX-XXXX";
        }
        return "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4);
    }

    private void showResolveDialog(Feedback feedback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_resolve_feedback, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogFeedbackTitle);
        TextView tvUserInfo = dialogView.findViewById(R.id.tvDialogUserInfo);
        TextView tvDescription = dialogView.findViewById(R.id.tvDialogFeedbackDescription);
        EditText etResponse = dialogView.findViewById(R.id.etAdminResponse);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelResolve);
        Button btnMarkResolved = dialogView.findViewById(R.id.btnMarkResolved);

        tvTitle.setText(feedback.getTitle());
        tvUserInfo.setText("User: " + feedback.getUserName() + " • " + maskAadhaar(feedback.getUserAadhaar()));
        tvDescription.setText(feedback.getDescription());

        // Pre-fill if already has response
        if (feedback.getAdminResponse() != null && !feedback.getAdminResponse().isEmpty()) {
            etResponse.setText(feedback.getAdminResponse());
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnMarkResolved.setOnClickListener(v -> {
            String response = etResponse.getText().toString().trim();

            if (response.isEmpty()) {
                Toast.makeText(getContext(), "Please provide a response", Toast.LENGTH_SHORT).show();
                return;
            }

            feedback.setAdminResponse(response);
            feedback.setStatus("resolved");
            feedback.setResolvedTimestamp(System.currentTimeMillis());

            feedbackManager.updateFeedback(feedback, new FirebaseFeedbackManager.OnFeedbackOperationListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Feedback resolved successfully", Toast.LENGTH_SHORT).show();
                    loadFeedback();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Failed to update feedback: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void deleteFeedback(Feedback feedback) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Feedback")
                .setMessage("Are you sure you want to delete this feedback?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    feedbackManager.deleteFeedback(feedback.getId(),
                            new FirebaseFeedbackManager.OnFeedbackOperationListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Feedback deleted", Toast.LENGTH_SHORT).show();
                                    loadFeedback();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(getContext(), "Failed to delete feedback: " + error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
