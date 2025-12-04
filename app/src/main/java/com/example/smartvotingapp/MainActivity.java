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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.icon_search);
        notificationIcon = findViewById(R.id.icon_notification);
        dashboardTitle = findViewById(R.id.dashboard_title);

        // Receive user data from LoginActivity
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

        // Load default fragment
        loadFragment(new HomeFragment());
        dashboardTitle.setText("Dashboard");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                dashboardTitle.setText("Dashboard");
            } else if (itemId == R.id.nav_list) {
                selectedFragment = new ListFragment();
                dashboardTitle.setText("List");
            } else if (itemId == R.id.nav_vote) {
                selectedFragment = new VoteFragment();
                dashboardTitle.setText("Vote Now");
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                dashboardTitle.setText("History");
            } else if (itemId == R.id.nav_account) {
                selectedFragment = new AccountFragment();
                dashboardTitle.setText("My Account");
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });

        searchButton.setOnClickListener(v -> Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show());

        notificationIcon
                .setOnClickListener(v -> Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show());
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
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