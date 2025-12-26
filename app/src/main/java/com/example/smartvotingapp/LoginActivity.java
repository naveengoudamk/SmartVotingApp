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

        createNotificationChannel();

        aadhaarInput = findViewById(R.id.aadhaarInput);
        dobInput = findViewById(R.id.dobInput);
        loginButton = findViewById(R.id.loginButton);
        govtLoginLink = findViewById(R.id.govtLoginLink);

        // OTP related views
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        otpInput = findViewById(R.id.otpInput);
        otpArea = findViewById(R.id.otpArea);
        otpTimerText = findViewById(R.id.otpTimerText);

        // Initially hide OTP area and disable the Login button (must verify OTP first)
        showOtpArea(false);
        otpVerified = false;
        setLoginButtonEnabled(false);

        // Admin login click
        govtLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GovernmentLoginActivity.class);
            startActivity(intent);
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
            if (!validateUser(aadhaar, dob)) {
                CustomAlert.showError(this, "Authentication Failed", "Aadhaar/DOB not found. Please check details.");
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
            JSONObject user = getUserDetails(aadhaar, dob);

            if (user != null) {
                UserUtils.saveUserSession(LoginActivity.this, user.optString("aadhaar_id"));

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("aadhaar_id", user.optString("aadhaar_id"));
                intent.putExtra("dob", user.optString("dob"));
                intent.putExtra("name", user.optString("name"));
                intent.putExtra("email", user.optString("email"));
                intent.putExtra("mobile", user.optString("mobile"));
                intent.putExtra("photo", user.optString("photo"));
                intent.putExtra("address", user.optString("address"));
                intent.putExtra("city", user.optString("city"));
                intent.putExtra("pincode", user.optString("pincode"));
                intent.putExtra("eligible", user.optBoolean("eligible"));

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

        String title = "Demo OTP";
        String message = "Your OTP is:\n\n" + otp + "\n\nValid for 2 minutes.";

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

        String title = "Your SmartVoting OTP";
        String text = "Your OTP is: " + otp + " (valid 2 minutes)";

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

    // ---------- Existing user JSON methods ----------

    private boolean validateUser(String aadhaar, String dob) {
        try {
            InputStream is = getAssets().open("aadhaar_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (aadhaar.equals(obj.getString("aadhaar_id"))
                        && dob.equals(obj.getString("dob"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONObject getUserDetails(String aadhaar, String dob) {
        try {
            InputStream is = getAssets().open("aadhaar_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (aadhaar.equals(obj.getString("aadhaar_id"))
                        && dob.equals(obj.getString("dob"))) {
                    return obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null)
            otpTimer.cancel();
    }
}
