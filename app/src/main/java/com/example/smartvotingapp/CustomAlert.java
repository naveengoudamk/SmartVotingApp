package com.example.smartvotingapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class CustomAlert {

    public static void showSuccess(Context context, String title, String message) {
        showAlert(context, title, message, R.color.success, android.R.drawable.checkbox_on_background);
    }

    public static void showError(Context context, String title, String message) {
        showAlert(context, title, message, R.color.error, android.R.drawable.ic_delete);
    }

    public static void showWarning(Context context, String title, String message) {
        showAlert(context, title, message, R.color.warning, android.R.drawable.ic_dialog_alert);
    }

    public static void showInfo(Context context, String title, String message) {
        showAlert(context, title, message, R.color.primary, android.R.drawable.ic_dialog_info);
    }

    private static void showAlert(Context context, String title, String message, int colorResId, int iconResId) {
        // Check if context is an activity and if it's finishing
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return; // Don't show dialog if activity is finishing
            }
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_custom_alert);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            window.getAttributes().windowAnimations = R.style.DialogAnimation;

            // Remove dim background to make it look like a floating notification
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // Allow interaction with the activity behind
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvAlertTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvAlertMessage);
        ImageView imgIcon = dialog.findViewById(R.id.imgAlertIcon);
        View viewColor = dialog.findViewById(R.id.viewAlertColor);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseAlert);

        tvTitle.setText(title);
        tvMessage.setText(message);

        int color = ContextCompat.getColor(context, colorResId);
        imgIcon.setImageResource(iconResId);
        imgIcon.setColorFilter(color);
        viewColor.setBackgroundColor(color);

        btnClose.setOnClickListener(v -> {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Auto dismiss after 4 seconds with safety checks
        new android.os.Handler().postDelayed(() -> {
            try {
                // Check if activity is still valid
                if (context instanceof android.app.Activity) {
                    android.app.Activity activity = (android.app.Activity) context;
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        return; // Don't try to dismiss if activity is gone
                    }
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
                // Silently catch any exceptions during auto-dismiss
                e.printStackTrace();
            }
        }, 4000);
    }
}
