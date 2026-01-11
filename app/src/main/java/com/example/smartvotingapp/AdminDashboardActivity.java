package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView dashboardTitle;
    private EditText etSearch;
    private TextView tvNotificationBadge;
    private NotificationHelper notificationHelper;
    private BottomNavigationView bottomNavigationView;

    private String currentDeptCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        dashboardTitle = findViewById(R.id.dashboard_title);
        etSearch = findViewById(R.id.etSearch);
        ImageView notificationIcon = findViewById(R.id.icon_notification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        notificationHelper = new NotificationHelper(this);

        // Parse Admin Scope first
        currentDeptCode = getIntent().getStringExtra("dept_code");
        String adminStateScope = null;
        if (currentDeptCode != null) {
            if (currentDeptCode.equals("KAR-GOVT"))
                adminStateScope = "Karnataka";
            else if (currentDeptCode.equals("TN-GOVT"))
                adminStateScope = "Tamil Nadu";
            // ECI-INDIA stays null (Global)
        }
        final String finalScope = adminStateScope;

        // Load default fragment with scope
        if (savedInstanceState == null) {
            try {
                AdminHomeFragment homeFragment = new AdminHomeFragment();
                Bundle args = new Bundle();
                args.putString("admin_scope", finalScope);
                homeFragment.setArguments(args);
                loadFragment(homeFragment);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_home) {
                selectedFragment = new AdminHomeFragment();
            } else if (itemId == R.id.nav_admin_users) {
                selectedFragment = new AdminUserListFragment();
            } else if (itemId == R.id.nav_admin_elections) {
                selectedFragment = new AdminElectionFragment();
            } else if (itemId == R.id.nav_admin_parties) {
                selectedFragment = new AdminPartyFragment();
            } else if (itemId == R.id.nav_admin_results) {
                selectedFragment = new AdminResultFragment();
            }

            if (selectedFragment != null) {
                // Pass scope to fragment
                Bundle args = new Bundle();
                args.putString("admin_scope", finalScope);
                selectedFragment.setArguments(args);

                loadFragment(selectedFragment);
                etSearch.setText(""); // Clear search
                return true;
            }
            return false; // Prevent tab switch if fragment failed
        });

        // Persistent Search Logic
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        try {
            notificationIcon.setOnClickListener(v -> {
                Intent notificationIntent = new Intent(AdminDashboardActivity.this, NotificationActivity.class);
                notificationIntent.putExtra("is_admin", true);
                if (currentDeptCode != null) {
                    notificationIntent.putExtra("dept_code", currentDeptCode);
                }
                startActivity(notificationIntent);
            });

            updateNotificationBadge();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Handle navigation intent (e.g. returning from notification)
        handleNavigationIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (notificationHelper != null) {
                updateNotificationBadge();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationBadge() {
        try {
            if (notificationHelper == null)
                return;

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

    private void performSearch(String query) {
        try {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof SearchableFragment) {
                ((SearchableFragment) currentFragment).onSearch(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            handleNavigationIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String pendingTargetId = null;

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to")) {
            String destination = intent.getStringExtra("navigate_to");
            if (intent.hasExtra("target_id")) {
                pendingTargetId = intent.getStringExtra("target_id");
            }

            if ("home".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_admin_home);
            } else if ("vote".equals(destination) || "election".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_admin_elections);
            } else if ("history".equals(destination) || "result".equals(destination)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_admin_results);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            if (pendingTargetId != null) {
                Bundle args = fragment.getArguments();
                if (args == null)
                    args = new Bundle();
                args.putString("target_id", pendingTargetId);
                fragment.setArguments(args);
                pendingTargetId = null; // Consume it
                // Note: We might be overwriting args set by bottomNav listener logic (the
                // scope).
                // We should ensure we don't lose the scope.
                // The bottomNav listener sets args NEWLY.
                // This loadFragment is called by the listener.
                // BUT the listener creates the fragment then calls loadFragment.
                // So here we are just adding to existing args.
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Nav Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
