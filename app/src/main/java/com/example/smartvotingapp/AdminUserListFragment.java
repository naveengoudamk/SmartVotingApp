package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.List;

public class AdminUserListFragment extends Fragment {

    private LinearLayout userContainer;
    private UserManager userManager;

    public AdminUserListFragment() {
    }

    private String adminScope;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (getArguments() != null) {
                adminScope = getArguments().getString("admin_scope");
            }

            View view = inflater.inflate(R.layout.fragment_admin_user_list, container, false);

            userManager = new UserManager(getContext());
            userContainer = view.findViewById(R.id.userContainer);
            Button btnAddUser = view.findViewById(R.id.btnAddUser);

            btnAddUser.setOnClickListener(v -> showAddUserDialog());

            loadUsers();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
    }

    private void loadUsers() {
        userContainer.removeAllViews();
        List<User> users = userManager.getAllUsers();

        if (users.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No users found.");
            empty.setPadding(32, 32, 32, 32);
            userContainer.addView(empty);
            return;
        }

        for (User user : users) {
            // FILTER: If admin has a scope, only show users from that state
            if (adminScope != null && !adminScope.isEmpty()) {
                if (user.getState() == null || !user.getState().equalsIgnoreCase(adminScope)) {
                    continue; // Skip users from other states
                }
            }

            View userView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_admin, userContainer, false);

            TextView name = userView.findViewById(R.id.tvName);
            TextView aadhaar = userView.findViewById(R.id.tvAadhaar);
            Button btnEdit = userView.findViewById(R.id.btnEdit);

            name.setText(user.getName());
            aadhaar.setText("Aadhaar: " + user.getAadhaarId());

            btnEdit.setOnClickListener(v -> showEditUserDialog(user));

            userContainer.addView(userView);
        }
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etMobile = view.findViewById(R.id.etMobile);
        EditText etAddress = view.findViewById(R.id.etAddress);
        CheckBox cbEligible = view.findViewById(R.id.cbEligible);

        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etMobile.setText(user.getMobile());
        etAddress.setText(user.getAddress());
        cbEligible.setChecked(user.isEligible());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newMobile = etMobile.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();
            boolean isEligible = cbEligible.isChecked();

            User updatedUser = new User(
                    user.getAadhaarId(),
                    newName,
                    user.getDob(),
                    newEmail,
                    newMobile,
                    user.getPhoto(),
                    newAddress,
                    user.getCity(),
                    user.getState(),
                    user.getPincode(),
                    isEligible);

            userManager.updateUser(updatedUser);
            loadUsers();
            Toast.makeText(getContext(), "User updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_user, null);
        builder.setView(view);
        builder.setTitle("Add New User");

        EditText etAadhaar = view.findViewById(R.id.etAadhaar);
        EditText etName = view.findViewById(R.id.etName);
        EditText etDob = view.findViewById(R.id.etDob);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etMobile = view.findViewById(R.id.etMobile);
        EditText etAddress = view.findViewById(R.id.etAddress);
        EditText etCity = view.findViewById(R.id.etCity);
        EditText etState = view.findViewById(R.id.etState);
        EditText etPincode = view.findViewById(R.id.etPincode);
        CheckBox cbEligible = view.findViewById(R.id.cbEligible);

        // Date picker for DOB
        etDob.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(getContext(), (dp, year, month, day) -> {
                etDob.setText(year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day));
            }, c.get(java.util.Calendar.YEAR) - 20, c.get(java.util.Calendar.MONTH),
                    c.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            String aadhaar = etAadhaar.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String state = etState.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();
            boolean isEligible = cbEligible.isChecked();

            // Validation
            if (aadhaar.isEmpty() || name.isEmpty() || dob.isEmpty()) {
                Toast.makeText(getContext(), "Aadhaar, Name, and DOB are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (aadhaar.length() != 12) {
                Toast.makeText(getContext(), "Aadhaar must be 12 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User(
                    aadhaar,
                    name,
                    dob,
                    email,
                    mobile,
                    "", // photo - empty for now
                    address,
                    city,
                    state,
                    pincode,
                    isEligible);

            userManager.addUser(newUser);
            loadUsers();
            Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
