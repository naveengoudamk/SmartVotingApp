package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private ImageView searchButton, notificationIcon;
    private TextView dashboardTitle;
    private boolean doubleBackToExitPressedOnce = false;

    // Static user data to be used in AccountFragment
    public static String aadhaarId, dob, name, email, mobile, photo, address, city, pincode;
    public static boolean eligible;

    private android.widget.EditText etSearch;
    private android.widget.TextView tvNotificationBadge;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        // Apply Day/Night mode based on preference
        android.content.SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout inflated successfully");

            // Initialize views
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            notificationIcon = findViewById(R.id.icon_notification);
            tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
            dashboardTitle = findViewById(R.id.dashboard_title);
            etSearch = findViewById(R.id.etSearch);
            Log.d(TAG, "Views initialized");

            // Initialize notification helper with error handling
            try {
                notificationHelper = new NotificationHelper(this);
                Log.d(TAG, "NotificationHelper created");
            } catch (Exception e) {
                Log.e(TAG, "NotificationHelper failed", e);
                e.printStackTrace();
                notificationHelper = null;
            }

            // Get user data from intent
            Intent intent = getIntent();
            aadhaarId = intent.getStringExtra("aadhaar_id");
            dob = intent.getStringExtra("dob");
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
            mobile = intent.getStringExtra("mobile");
            photo = intent.getStringExtra("photo");
            address = intent.getStringExtra("address");
            city = intent.getStringExtra("city");
            pincode = intent.getStringExtra("pincode");
            eligible = intent.getBooleanExtra("eligible", false);
            Log.d(TAG, "User data loaded: " + aadhaarId);

            // Validate minimum required data
            if (aadhaarId == null || aadhaarId.isEmpty()) {
                Log.e(TAG, "No aadhaar ID - redirecting to login");
                Toast.makeText(this, "Login session error. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Save user session
            try {
                UserUtils.saveUserSession(this, aadhaarId);
                Log.d(TAG, "User session saved");
            } catch (Exception e) {
                Log.e(TAG, "Failed to save session", e);
                e.printStackTrace();
            }

            // Load default fragment
            try {
                loadFragment(new HomeFragment());
                Log.d(TAG, "HomeFragment loaded");
            } catch (Exception e) {
                Log.e(TAG, "Failed to load HomeFragment", e);
                e.printStackTrace();
                Toast.makeText(this, "Error loading home", Toast.LENGTH_SHORT).show();
            }

            // Setup bottom navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    try {
                        Fragment selectedFragment = null;
                        int itemId = item.getItemId();

                        if (itemId == R.id.nav_home) {
                            selectedFragment = new HomeFragment();
                        } else if (itemId == R.id.nav_list) {
                            selectedFragment = new ListFragment();
                        } else if (itemId == R.id.nav_vote) {
                            selectedFragment = new VoteFragment();
                        } else if (itemId == R.id.nav_history) {
                            selectedFragment = new HistoryFragment();
                        } else if (itemId == R.id.nav_account) {
                            selectedFragment = new AccountFragment();
                        }

                        if (selectedFragment != null) {
                            loadFragment(selectedFragment);
                            if (etSearch != null) {
                                etSearch.setText("");
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation error", e);
                        e.printStackTrace();
                        Toast.makeText(this, "Nav Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                Log.d(TAG, "Bottom navigation setup complete");
            }

            // Setup search
            if (etSearch != null) {
                etSearch.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            performSearch(s.toString());
                        } catch (Exception e) {
                            Log.e(TAG, "Search error", e);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                    }
                });
                Log.d(TAG, "Search setup complete");
            }

            // Setup notification icon
            if (notificationIcon != null) {
                notificationIcon.setOnClickListener(v -> {
                    try {
                        Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                        startActivity(notificationIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Notification click error", e);
                        e.printStackTrace();
                        Toast.makeText(this, "Cannot open notifications", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, "Notification icon setup complete");
            }

            // Update notification badge
            try {
                updateNotificationBadge();
                Log.d(TAG, "Notification badge updated");
            } catch (Exception e) {
                Log.e(TAG, "Badge update error", e);
                e.printStackTrace();
            }

            // Handle navigation intent
            try {
                handleNavigationIntent(getIntent());
                Log.d(TAG, "Navigation intent handled");
            } catch (Exception e) {
                Log.e(TAG, "Navigation intent error", e);
                e.printStackTrace();
            }

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "FATAL onCreate error", e);
            e.printStackTrace();
            Toast.makeText(this, "Dashboard Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            handleNavigationIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, "onNewIntent error", e);
            e.printStackTrace();
        }
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to")) {
            String destination = intent.getStringExtra("navigate_to");
            if ("vote".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_vote);
            } else if ("account".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_account);
            } else if ("home".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateNotificationBadge();
        } catch (Exception e) {
            Log.e(TAG, "onResume error", e);
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge() {
        try {
            if (notificationHelper == null || tvNotificationBadge == null) {
                return;
            }
            int count = notificationHelper.getUnreadCount();
            if (count > 0) {
                tvNotificationBadge.setVisibility(android.view.View.VISIBLE);
                tvNotificationBadge.setText(String.valueOf(count));
            } else {
                tvNotificationBadge.setVisibility(android.view.View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateNotificationBadge error", e);
            e.printStackTrace();
        }
    }

    private void performSearch(String query) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof SearchableFragment) {
                ((SearchableFragment) currentFragment).onSearch(query);
            }
        } catch (Exception e) {
            Log.e(TAG, "performSearch error", e);
            e.printStackTrace();
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            if (fragment == null) {
                Log.w(TAG, "loadFragment called with null fragment");
                return;
            }
            if (isFinishing() || isDestroyed()) {
                Log.w(TAG, "Activity finishing/destroyed, skipping fragment load");
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
            Log.d(TAG, "Fragment loaded: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "loadFragment error", e);
            e.printStackTrace();
            Toast.makeText(this, "Error loading view: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, "Thank you for using Smart Voting! 🙏✨", Toast.LENGTH_LONG).show();
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}