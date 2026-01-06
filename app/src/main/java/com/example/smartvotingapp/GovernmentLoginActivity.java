package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GovernmentLoginActivity extends AppCompatActivity {

    EditText deptCodeInput, passwordInput;
    Button loginButton;

    private static final String TAG = "GovLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Day/Night mode
        android.content.SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_government_login); // ✅ Ensure layout name matches the XML

        deptCodeInput = findViewById(R.id.deptCodeInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.adminLoginBtn);

        loginButton.setOnClickListener(v -> {
            String deptCode = deptCodeInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (validateGovernmentLogin(deptCode, password)) {
                Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();

                try {
                    Intent intent = new Intent(GovernmentLoginActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("dept_code", deptCode);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching AdminDashboardActivity", e);
                    Toast.makeText(this, "Error launching Admin Dashboard:\n" + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }

            } else {
                Toast.makeText(this, "Invalid Department Code or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateGovernmentLogin(String deptCode, String password) {
        try {
            InputStream is = getAssets().open("gov_login_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray departments = new JSONArray(json);
            for (int i = 0; i < departments.length(); i++) {
                JSONObject obj = departments.getJSONObject(i);
                if (deptCode.equals(obj.getString("dept_code")) &&
                        password.equals(obj.getString("password"))) {
                    Log.d(TAG, "Login matched for: " + deptCode);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading gov_login_data.json", e);
            Toast.makeText(this, "Login data file missing or corrupted.", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
