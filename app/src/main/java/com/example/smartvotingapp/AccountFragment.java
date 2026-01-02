package com.example.smartvotingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;

public class AccountFragment extends Fragment {

    private TextView txtName, txtAadhaar, txtMobile, txtAddress, txtCity, txtPincode, txtEligible;
    private EditText editEmail;
    private View updateEmailButton, changePhotoButton;
    private Button savePhotoButton;
    private ImageView profileImage;
    private Button btnSubmitFeedback;
    private LinearLayout feedbackListContainer;
    private TextView tvNoFeedback, tvFeedbackBadge;
    private FeedbackManager feedbackManager;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private Uri selectedImageUri;
    private Bitmap capturedImageBitmap;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String JSON_FILE_NAME = "user_data.json";

    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_account, container, false);

            prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            feedbackManager = new FeedbackManager(getContext());

            txtName = view.findViewById(R.id.txt_name);
            txtAadhaar = view.findViewById(R.id.txt_aadhaar);
            txtMobile = view.findViewById(R.id.txt_mobile);
            txtAddress = view.findViewById(R.id.txt_address);
            txtCity = view.findViewById(R.id.txt_city);
            txtPincode = view.findViewById(R.id.txt_pincode);
            txtEligible = view.findViewById(R.id.txt_eligible);
            editEmail = view.findViewById(R.id.edit_email);
            updateEmailButton = view.findViewById(R.id.btn_update_email);
            changePhotoButton = view.findViewById(R.id.btn_change_photo);
            savePhotoButton = view.findViewById(R.id.btn_save_photo);
            profileImage = view.findViewById(R.id.img_profile);
            btnSubmitFeedback = view.findViewById(R.id.btnSubmitFeedback);
            feedbackListContainer = view.findViewById(R.id.feedbackListContainer);
            tvNoFeedback = view.findViewById(R.id.tvNoFeedback);
            tvFeedbackBadge = view.findViewById(R.id.tvFeedbackBadge);
            com.google.android.material.switchmaterial.SwitchMaterial switchDarkMode = view
                    .findViewById(R.id.switchDarkMode);

            User user = UserUtils.getCurrentUser(getContext());
            if (user != null) {
                txtName.setText(user.getName());
                txtAadhaar.setText(user.getAadhaarId());
                txtMobile.setText(user.getMobile());
                txtAddress.setText(user.getAddress());
                txtCity.setText(user.getCity());
                txtPincode.setText(user.getPincode());
                txtEligible.setText(user.isEligible() ? "Eligible to vote" : "Not eligible");
                if (user.isEligible()) {
                    txtEligible.setTextColor(0xFF059669);
                } else {
                    txtEligible.setTextColor(0xFFDC2626);
                }
                editEmail.setText(user.getEmail());
            } else {
                CustomAlert.showError(getContext(), "Error", "User data not found");
            }

            // Dark Mode Logic
            boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
            switchDarkMode.setChecked(isDarkMode);

            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("isDarkMode", isChecked).apply();
                if (isChecked) {
                    androidx.appcompat.app.AppCompatDelegate
                            .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    androidx.appcompat.app.AppCompatDelegate
                            .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                }
            });

            loadSavedProfileImage();

            updateEmailButton.setOnClickListener(v -> {
                String updatedEmail = editEmail.getText().toString().trim();
                MainActivity.email = updatedEmail;
                updateEmailInJson(updatedEmail);
                CustomAlert.showSuccess(getContext(), "Success", "Email updated successfully!");
            });

            changePhotoButton.setOnClickListener(v -> showImagePickerOptions());
            profileImage.setOnClickListener(v -> showImagePickerOptions());

            savePhotoButton.setVisibility(View.GONE);
            savePhotoButton.setOnClickListener(v -> {
                String filename = "profile_" + MainActivity.aadhaarId + ".jpg";

                if (capturedImageBitmap != null) {
                    saveBitmapToInternalStorage(capturedImageBitmap, filename);
                } else if (selectedImageUri != null) {
                    try {
                        InputStream is = getContext().getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        saveBitmapToInternalStorage(bitmap, filename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                updatePhotoInJson(filename);
                CustomAlert.showSuccess(getContext(), "Success", "Profile photo saved!");
                savePhotoButton.setVisibility(View.GONE);
            });

            btnSubmitFeedback.setOnClickListener(v -> showSubmitFeedbackDialog());
            loadUserFeedback();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
    }

    private void showImagePickerOptions() {
        String[] options = { "Choose from Gallery", "Take Photo" };
        new AlertDialog.Builder(getContext())
                .setTitle("Update Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImageFromGallery();
                    } else {
                        takePhotoFromCamera();
                    }
                })
                .show();
    }

    private void pickImageFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.READ_MEDIA_IMAGES }, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
            @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY) {
                selectedImageUri = data.getData();
                profileImage.setImageURI(selectedImageUri);
                savePhotoButton.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_CAMERA) {
                capturedImageBitmap = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(capturedImageBitmap);
                savePhotoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveBitmapToInternalStorage(Bitmap bitmap, String filename) {
        try {
            File file = new File(getContext().getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            User user = UserUtils.getCurrentUser(getContext());
            if (user != null) {
                prefs.edit().putBoolean("hasSavedImage_" + user.getAadhaarId(), true).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedProfileImage() {
        User user = UserUtils.getCurrentUser(getContext());
        if (user == null)
            return;

        String filename = "profile_" + user.getAadhaarId() + ".jpg";
        if (prefs.getBoolean("hasSavedImage_" + user.getAadhaarId(), false)) {
            File file = new File(getContext().getFilesDir(), filename);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        }
    }

    private void updatePhotoInJson(String filename) {
        try {
            User currentUser = UserUtils.getCurrentUser(getContext());
            if (currentUser == null)
                return;

            File jsonFile = new File(getContext().getFilesDir(), JSON_FILE_NAME);
            JSONArray jsonArray;

            if (jsonFile.exists()) {
                String jsonStr = new String(Files.readAllBytes(jsonFile.toPath()));
                jsonArray = new JSONArray(jsonStr);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject user = jsonArray.getJSONObject(i);
                    if (user.getString("aadhaar_id").equals(currentUser.getAadhaarId())) {
                        user.put("photo", filename);
                        break;
                    }
                }

                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(jsonArray.toString(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEmailInJson(String newEmail) {
        try {
            User currentUser = UserUtils.getCurrentUser(getContext());
            if (currentUser == null)
                return;

            File jsonFile = new File(getContext().getFilesDir(), JSON_FILE_NAME);
            JSONArray jsonArray;

            if (jsonFile.exists()) {
                String jsonStr = new String(Files.readAllBytes(jsonFile.toPath()));
                jsonArray = new JSONArray(jsonStr);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject user = jsonArray.getJSONObject(i);
                    if (user.getString("aadhaar_id").equals(currentUser.getAadhaarId())) {
                        user.put("email", newEmail);
                        break;
                    }
                }

                try (FileWriter writer = new FileWriter(jsonFile)) {
                    writer.write(jsonArray.toString(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSubmitFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_submit_feedback, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etFeedbackTitle);
        EditText etDescription = dialogView.findViewById(R.id.etFeedbackDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelFeedback);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitFeedback);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty()) {
                CustomAlert.showWarning(getContext(), "Missing Info", "Please fill all fields");
                return;
            }

            User user = UserUtils.getCurrentUser(getContext());
            if (user == null) {
                CustomAlert.showError(getContext(), "Error", "User not found");
                return;
            }

            Feedback feedback = new Feedback(
                    java.util.UUID.randomUUID().toString(),
                    user.getAadhaarId(),
                    user.getName(),
                    user.getAadhaarId(),
                    title,
                    description,
                    "pending",
                    "",
                    System.currentTimeMillis(),
                    0);

            if (feedbackManager.addFeedback(feedback)) {
                CustomAlert.showSuccess(getContext(), "Submitted", "Feedback submitted successfully!");
                loadUserFeedback();
                dialog.dismiss();
            } else {
                CustomAlert.showError(getContext(), "Failed", "Failed to submit feedback");
            }
        });

        dialog.show();
    }

    private void loadUserFeedback() {
        feedbackListContainer.removeAllViews();

        User user = UserUtils.getCurrentUser(getContext());
        if (user == null)
            return;

        java.util.List<Feedback> userFeedback = feedbackManager.getFeedbackByUserId(user.getAadhaarId());

        if (userFeedback.isEmpty()) {
            tvNoFeedback.setVisibility(View.VISIBLE);
            tvFeedbackBadge.setVisibility(View.GONE);
            return;
        }

        tvNoFeedback.setVisibility(View.GONE);

        // Sort by timestamp (newest first)
        userFeedback.sort((f1, f2) -> Long.compare(f2.getTimestamp(), f1.getTimestamp()));

        // Check for newly resolved feedback
        long lastCheckTime = prefs.getLong("lastFeedbackCheck_" + user.getAadhaarId(), 0);
        int newlyResolvedCount = feedbackManager.getNewlyResolvedCount(user.getAadhaarId(), lastCheckTime);

        if (newlyResolvedCount > 0) {
            tvFeedbackBadge.setText(String.valueOf(newlyResolvedCount));
            tvFeedbackBadge.setVisibility(View.VISIBLE);
        } else {
            tvFeedbackBadge.setVisibility(View.GONE);
        }

        // Update last check time
        prefs.edit().putLong("lastFeedbackCheck_" + user.getAadhaarId(), System.currentTimeMillis()).apply();

        for (Feedback feedback : userFeedback) {
            View feedbackView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_feedback_card, feedbackListContainer, false);

            TextView tvTitle = feedbackView.findViewById(R.id.tvFeedbackTitle);
            TextView tvStatus = feedbackView.findViewById(R.id.tvFeedbackStatus);
            TextView tvDescription = feedbackView.findViewById(R.id.tvFeedbackDescription);
            TextView tvDate = feedbackView.findViewById(R.id.tvFeedbackDate);
            LinearLayout layoutAdminResponse = feedbackView.findViewById(R.id.layoutAdminResponse);
            TextView tvAdminResponse = feedbackView.findViewById(R.id.tvAdminResponse);
            TextView tvResolvedDate = feedbackView.findViewById(R.id.tvResolvedDate);

            tvTitle.setText(feedback.getTitle());
            tvDescription.setText(feedback.getDescription());

            // Format date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy",
                    java.util.Locale.getDefault());
            tvDate.setText("Submitted on: " + sdf.format(new java.util.Date(feedback.getTimestamp())));

            // Set status badge
            updateStatusBadge(tvStatus, feedback.getStatus());

            // Show admin response if exists
            if (feedback.getStatus().equals("resolved") &&
                    feedback.getAdminResponse() != null &&
                    !feedback.getAdminResponse().isEmpty()) {
                layoutAdminResponse.setVisibility(View.VISIBLE);
                tvAdminResponse.setText(feedback.getAdminResponse());
                tvResolvedDate
                        .setText("Resolved on: " + sdf.format(new java.util.Date(feedback.getResolvedTimestamp())));

                // Highlight if newly resolved
                if (feedback.getResolvedTimestamp() > lastCheckTime) {
                    feedbackView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF0FDF4));
                }
            } else {
                layoutAdminResponse.setVisibility(View.GONE);
            }

            feedbackListContainer.addView(feedbackView);
        }
    }

    private void updateStatusBadge(TextView badge, String status) {
        switch (status) {
            case "pending":
                badge.setText("Pending");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
                break;
            case "in_progress":
                badge.setText("In Progress");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3B82F6));
                break;
            case "resolved":
                badge.setText("Resolved");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF059669));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CustomAlert.showSuccess(getContext(), "Permission", "Permission granted");
        } else {
            CustomAlert.showError(getContext(), "Permission", "Permission denied");
        }
    }
}
