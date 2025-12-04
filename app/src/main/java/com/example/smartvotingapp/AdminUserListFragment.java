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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_list, container, false);

        userManager = new UserManager(getContext());
        userContainer = view.findViewById(R.id.userContainer);

        loadUsers();

        return view;
    }

    private void loadUsers() {
        userContainer.removeAllViews();
        List<User> users = userManager.getAllUsers();

        for (User user : users) {
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
}
