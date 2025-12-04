package com.example.smartvotingapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView dashboardTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        dashboardTitle = findViewById(R.id.dashboard_title);

        // Load default fragment
        loadFragment(new AdminHomeFragment());
        dashboardTitle.setText("Admin Dashboard - Home");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_home) {
                selectedFragment = new AdminHomeFragment();
                dashboardTitle.setText("Admin Dashboard - Home");
            } else if (itemId == R.id.nav_admin_users) {
                selectedFragment = new AdminUserListFragment();
                dashboardTitle.setText("Admin Dashboard - Users");
            } else if (itemId == R.id.nav_admin_elections) {
                selectedFragment = new AdminElectionFragment();
                dashboardTitle.setText("Admin Dashboard - Elections");
            } else if (itemId == R.id.nav_admin_parties) {
                selectedFragment = new AdminPartyFragment();
                dashboardTitle.setText("Admin Dashboard - Parties");
            } else if (itemId == R.id.nav_admin_results) {
                selectedFragment = new AdminResultFragment();
                dashboardTitle.setText("Admin Dashboard - Results");
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
