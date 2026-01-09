package com.example.smartvotingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.json.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String CHANNEL_ID = "otp_channel_01";
    private static final int OTP_NOTIFICATION_ID = 1001;

    EditText aadhaarInput, dobInput;
    Button loginButton;
    TextView govtLoginLink;

    // OTP UI
    Button sendOtpButton, verifyOtpButton, resendOtpButton;
    EditText otpInput;
    LinearLayout otpArea;
    TextView otpTimerText;
    private UserManager userManager;

    // OTP state
    private String currentOtp = null;
    private long otpExpiryMillis = 0;
    private CountDownTimer otpTimer;

    // whether the OTP has been successfully verified
    private boolean otpVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userManager = new UserManager(this);

        createNotificationChannel();

        aadhaarInput = findViewById(R.id.aadhaarInput);
        dobInput = findViewById(R.id.dobInput);
        loginButton = findViewById(R.id.loginButton);
        // govtLoginLink = findViewById(R.id.govtLoginLink); // Replaced by icon

        Button btnSkipLogin = findViewById(R.id.btnSkipLogin);
        ImageButton btnAdminLogin = findViewById(R.id.btnAdminLogin);

        // OTP related views
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        otpInput = findViewById(R.id.otpInput);
        otpArea = findViewById(R.id.otpArea);
        otpTimerText = findViewById(R.id.otpTimerText);

        // Check for app updates BEFORE allowing login
        checkForUpdatesOnLogin();

        // Initially hide OTP area and disable the Login button (must verify OTP first)
        showOtpArea(false);
        otpVerified = false;
        setLoginButtonEnabled(false);

        // Admin login click (Top Icon)
        btnAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GovernmentLoginActivity.class);
            startActivity(intent);
        });

        // Skip Login click
        btnSkipLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // No user data means Guest Mode
            startActivity(intent);
            finish();
        });

        // Disable keyboard for dob input and show calendar picker
        dobInput.setFocusable(false);
        dobInput.setClickable(true);

        dobInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    LoginActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = selectedYear + "-" +
                                String.format("%02d", (selectedMonth + 1)) + "-" +
                                String.format("%02d", selectedDay);
                        dobInput.setText(formattedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // Send OTP flow
        sendOtpButton.setOnClickListener(v -> {
            String aadhaar = aadhaarInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();

            if (aadhaar.isEmpty() || aadhaar.length() != 12) {
                CustomAlert.showError(this, "Invalid Input", "Enter valid 12-digit Aadhaar first");
                return;
            }
            if (dob.isEmpty()) {
                CustomAlert.showError(this, "Missing Info", "Please select DOB");
                return;
            }

            // Validate user exists before sending OTP (keeps flow safe)
            User foundUser = userManager.getUser(aadhaar, dob);
            if (foundUser == null) {
                // Show detailed error for debugging
                String msg = "User not found.\nChecked: " + aadhaar + " / " + dob;
                Log.w("LoginDebug", msg);
                CustomAlert.showError(this, "Authentication Failed", msg + "\nPlease verify credentials.");
                return;
            }

            // Generate OTP and "send"
            currentOtp = generateOtp(6);
            otpExpiryMillis = System.currentTimeMillis() + (2 * 60 * 1000); // 2 minutes validity
            showOtpArea(true);
            startOtpCountdown(2 * 60 * 1000L);

            CustomAlert.showSuccess(this, "OTP Sent", "OTP sent successfully. Check popup.");
            sendOtpNotification(currentOtp);

            // Show demo dialog (copy & autofill)
            showOtpDialog(currentOtp);

            // Reset verification flag because a new OTP was requested
            otpVerified = false;
            setLoginButtonEnabled(false);
        });

        // Verify OTP button
        verifyOtpButton.setOnClickListener(v -> {
            String entered = otpInput.getText().toString().trim();
            if (entered.isEmpty()) {
                CustomAlert.showWarning(this, "Input Required", "Please enter OTP");
                return;
            }
            if (currentOtp == null) {
                CustomAlert.showWarning(this, "Action Required", "No OTP requested yet. Click Send OTP.");
                return;
            }
            if (System.currentTimeMillis() > otpExpiryMillis) {
                CustomAlert.showError(this, "Expired", "OTP expired. Please resend.");
                return;
            }
            if (entered.equals(currentOtp)) {
                CustomAlert.showSuccess(this, "Verified", "OTP Verified. You can now Login.");

                // mark verified and enable login button
                otpVerified = true;
                setLoginButtonEnabled(true);
            } else {
                CustomAlert.showError(this, "Failed", "Invalid OTP. Please try again.");
            }
        });

        // Resend OTP
        resendOtpButton.setOnClickListener(v -> {
            if (otpTimer != null)
                otpTimer.cancel();

            currentOtp = generateOtp(6);
            otpExpiryMillis = System.currentTimeMillis() + (2 * 60 * 1000);
            startOtpCountdown(2 * 60 * 1000L);

            CustomAlert.showInfo(this, "Resent", "OTP has been resent.");
            sendOtpNotification(currentOtp);

            showOtpDialog(currentOtp);

            // verification must be redone
            otpVerified = false;
            setLoginButtonEnabled(false);
        });

        // NEW: Login button now requires OTP verification first
        loginButton.setOnClickListener(v -> {
            if (!otpVerified) {
                CustomAlert.showWarning(this, "Verification Pending",
                        "Please verify OTP first (use Send OTP → Verify).");
                return;
            }

            // proceed to fetch user details and open main activity
            String aadhaar = aadhaarInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();
            User user = userManager.getUser(aadhaar, dob);

            if (user != null) {
                UserUtils.saveUserSession(LoginActivity.this, user);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("aadhaar_id", user.getAadhaarId());
                intent.putExtra("dob", user.getDob());
                intent.putExtra("name", user.getName());
                intent.putExtra("email", user.getEmail());
                intent.putExtra("mobile", user.getMobile());
                intent.putExtra("photo", user.getPhoto());
                intent.putExtra("address", user.getAddress());
                intent.putExtra("city", user.getCity());
                intent.putExtra("pincode", user.getPincode());
                intent.putExtra("eligible", user.isEligible());

                startActivity(intent);
                finish();
            } else {
                // This should be rare because we validated earlier before sending OTP,
                // but handle gracefully.
                CustomAlert.showError(this, "Error", "User details not found. Please check Aadhaar/DOB.");
            }
        });
    }

    // Helper to enable/disable the login button and indicate visually
    private void setLoginButtonEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        loginButton.setAlpha(enabled ? 1f : 0.5f);
    }

    // -------------------- OTP DIALOG --------------------

    private void showOtpDialog(String otp) {
        if (otp == null)
            return;

        String title = "Verification Code";
        String message = otp
                + " is your One Time Password (OTP) for SmartVoting App. Valid for 2 minutes.\n\nDo not share this code with anyone.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton("Copy & Autofill", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OTP", otp);
            if (clipboard != null)
                clipboard.setPrimaryClip(clip);

            otpInput.setText(otp);
            otpInput.setSelection(otp.length());

            Toast.makeText(this, "OTP autofilled", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Resend", (dialog, which) -> {
            resendOtpButton.performClick();
        });

        builder.create().show();
    }

    // -------------------- OTP utilities --------------------

    private String generateOtp(int length) {
        Random rnd = new Random();
        int bound = (int) Math.pow(10, length);
        int number = rnd.nextInt(bound - (bound / 10)) + (bound / 10);
        return String.format(Locale.getDefault(), "%0" + length + "d", number);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "OTP Channel";
            String description = "Channel for OTP notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendOtpNotification(String otp) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "SmartVoting Verification";
        String text = otp
                + " is your verification code for SmartVoting App. Valid for 2 minutes. Do not share this code with anyone.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(OTP_NOTIFICATION_ID, builder.build());
    }

    private void showOtpArea(boolean show) {
        if (show) {
            otpArea.setVisibility(View.VISIBLE);
            otpTimerText.setVisibility(View.VISIBLE);
            otpInput.requestFocus();
        } else {
            otpArea.setVisibility(View.GONE);
            otpTimerText.setVisibility(View.GONE);
            if (otpTimer != null)
                otpTimer.cancel();
            currentOtp = null;
        }
    }

    private void startOtpCountdown(long millisInFuture) {
        otpTimerText.setVisibility(View.VISIBLE);
        otpTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                otpTimerText.setText(
                        String.format(Locale.getDefault(),
                                "Time left: %02d:%02d",
                                sec / 60, sec % 60));
            }

            public void onFinish() {
                otpTimerText.setText("OTP expired");
                Toast.makeText(LoginActivity.this, "OTP expired", Toast.LENGTH_SHORT).show();
                currentOtp = null;
                otpVerified = false;
                setLoginButtonEnabled(false);
            }
        }.start();
    }

    /**
     * Check for app updates on login screen with timeout
     */
    private void checkForUpdatesOnLogin() {
        com.google.firebase.database.DatabaseReference updateRef = com.google.firebase.database.FirebaseDatabase
                .getInstance().getReference("app_version");

        // Set a timeout to prevent blocking login if Firebase is slow
        final boolean[] checkCompleted = { false };

        new android.os.Handler().postDelayed(() -> {
            if (!checkCompleted[0]) {
                Log.w(TAG, "Update check timed out - allowing normal login");
                checkCompleted[0] = true;
            }
        }, 5000); // 5 second timeout

        updateRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (checkCompleted[0])
                    return; // Timeout already occurred
                checkCompleted[0] = true;

                try {
                    if (dataSnapshot.exists()) {
                        Integer latestVersionCode = dataSnapshot.child("version_code").getValue(Integer.class);
                        String latestVersionName = dataSnapshot.child("version_name").getValue(String.class);
                        String updateMessage = dataSnapshot.child("update_message").getValue(String.class);

                        if (latestVersionCode != null) {
                            int currentVersionCode = getCurrentVersionCode();

                            if (latestVersionCode > currentVersionCode) {
                                // Update required - show dialog
                                Log.d(TAG, "Update available: " + latestVersionName);
                                showUpdateRequiredDialog(
                                        latestVersionName != null ? latestVersionName : "New Version",
                                        updateMessage != null ? updateMessage
                                                : "A new version is available. Please update to continue.");
                            } else {
                                Log.d(TAG, "App is up to date");
                            }
                        }
                    } else {
                        Log.w(TAG, "No app_version data in Firebase");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking for update", e);
                    // Don't block login on error
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                if (checkCompleted[0])
                    return;
                checkCompleted[0] = true;
                Log.e(TAG, "Update check cancelled: " + databaseError.getMessage());
                // Don't block login on error
            }
        });
    }

    /**
     * Show update required dialog with Update Now and Go Back buttons
     */
    /**
     * Show update required dialog with modern card-style UI
     */
    private void showUpdateRequiredDialog(String versionName, String message) {
        // Create custom dialog
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_update_available);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Make dialog background transparent to show card properly
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        // Get views
        TextView tvCurrentVersion = dialog.findViewById(R.id.tvCurrentVersion);
        TextView tvNewVersion = dialog.findViewById(R.id.tvNewVersion);
        TextView tvUpdateMessage = dialog.findViewById(R.id.tvUpdateMessage);
        Button btnUpdateNow = dialog.findViewById(R.id.btnUpdateNow);
        Button btnGoBack = dialog.findViewById(R.id.btnGoBack);

        // Set version info
        tvCurrentVersion.setText("v" + getCurrentVersionName());
        tvNewVersion.setText("v" + versionName);
        tvUpdateMessage.setText(message);

        // Update Now button
        btnUpdateNow.setOnClickListener(v -> {
            // Open web link
            android.content.Intent browserIntent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://smart-voting-app-web.vercel.app/#features"));
            startActivity(browserIntent);

            // Close the app
            dialog.dismiss();
            finish();
            System.exit(0);
        });

        // Go Back button
        btnGoBack.setOnClickListener(v -> {
            // Close the app
            dialog.dismiss();
            finish();
            System.exit(0);
        });

        try {
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing update dialog", e);
        }
    }

    /**
     * Get current app version code
     */
    private int getCurrentVersionCode() {
        try {
            android.content.pm.PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version code", e);
            return 0;
        }
    }

    /**
     * Get current app version name
     */
    private String getCurrentVersionName() {
        try {
            android.content.pm.PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version name", e);
            return "Unknown";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null)
            otpTimer.cancel();
    }
}
