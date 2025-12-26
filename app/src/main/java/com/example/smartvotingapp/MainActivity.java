package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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
        try {
            setContentView(R.layout.activity_main);

            bottomNavigationView = findViewById(R.id.bottom_navigation);
            notificationIcon = findViewById(R.id.icon_notification);
            tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
            dashboardTitle = findViewById(R.id.dashboard_title);
            etSearch = findViewById(R.id.etSearch);

            // Initialize notification helper with error handling
            try {
                notificationHelper = new NotificationHelper(this);
            } catch (Exception e) {
                e.printStackTrace();
                notificationHelper = null;
            }

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

            // Validate that we have minimum required data
            if (aadhaarId == null || aadhaarId.isEmpty()) {
                Toast.makeText(this, "Login session error. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Save user session
            try {
                UserUtils.saveUserSession(this, aadhaarId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loadFragment(new HomeFragment());

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
                    e.printStackTrace();
                    Toast.makeText(this, "Nav Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

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
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                    }
                });
            }

            if (notificationIcon != null) {
                notificationIcon.setOnClickListener(v -> {
                    try {
                        Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                        startActivity(notificationIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Cannot open notifications", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            updateNotificationBadge();
            handleNavigationIntent(getIntent());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Dashboard Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Don't finish - try to keep app running
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateNotificationBadge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge() {
        try {
            int count = notificationHelper.getUnreadCount();
            if (count > 0) {
                tvNotificationBadge.setVisibility(android.view.View.VISIBLE);
                tvNotificationBadge.setText(String.valueOf(count));
            } else {
                tvNotificationBadge.setVisibility(android.view.View.GONE);
            }
        } catch (Exception e) {
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

    private void performSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearch(query);
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            if (fragment == null) {
                return;
            }
            if (isFinishing() || isDestroyed()) {
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
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