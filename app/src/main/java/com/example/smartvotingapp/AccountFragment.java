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
    private Button updateEmailButton, changePhotoButton, savePhotoButton;
    private ImageView profileImage;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private Uri selectedImageUri;
    private Bitmap capturedImageBitmap;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String JSON_FILE_NAME = "user_data.json";

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

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

        txtName.setText(MainActivity.name);
        txtAadhaar.setText(MainActivity.aadhaarId);
        txtMobile.setText(MainActivity.mobile);
        txtAddress.setText(MainActivity.address);
        txtCity.setText(MainActivity.city);
        txtPincode.setText(MainActivity.pincode);
        txtEligible.setText(MainActivity.eligible ? "Eligible to vote" : "Not eligible");
        editEmail.setText(MainActivity.email);

        loadSavedProfileImage();

        updateEmailButton.setOnClickListener(v -> {
            String updatedEmail = editEmail.getText().toString().trim();
            MainActivity.email = updatedEmail;
            updateEmailInJson(updatedEmail);
            Toast.makeText(getContext(), "Email updated!", Toast.LENGTH_SHORT).show();
        });

        changePhotoButton.setOnClickListener(v -> showImagePickerOptions());

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
            Toast.makeText(getContext(), "Photo saved permanently!", Toast.LENGTH_SHORT).show();
            savePhotoButton.setVisibility(View.GONE);
        });

        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        view.startAnimation(fadeIn);

        return view;
    }

    private void showImagePickerOptions() {
        String[] options = {"Choose from Gallery", "Take Photo"};
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
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
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
            prefs.edit().putBoolean("hasSavedImage_" + MainActivity.aadhaarId, true).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSavedProfileImage() {
        String filename = "profile_" + MainActivity.aadhaarId + ".jpg";
        if (prefs.getBoolean("hasSavedImage_" + MainActivity.aadhaarId, false)) {
            File file = new File(getContext().getFilesDir(), filename);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        }
    }

    private void updatePhotoInJson(String filename) {
        try {
            File jsonFile = new File(getContext().getFilesDir(), JSON_FILE_NAME);
            JSONArray jsonArray;

            if (jsonFile.exists()) {
                String jsonStr = new String(Files.readAllBytes(jsonFile.toPath()));
                jsonArray = new JSONArray(jsonStr);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject user = jsonArray.getJSONObject(i);
                    if (user.getString("aadhaar_id").equals(MainActivity.aadhaarId)) {
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
            File jsonFile = new File(getContext().getFilesDir(), JSON_FILE_NAME);
            JSONArray jsonArray;

            if (jsonFile.exists()) {
                String jsonStr = new String(Files.readAllBytes(jsonFile.toPath()));
                jsonArray = new JSONArray(jsonStr);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject user = jsonArray.getJSONObject(i);
                    if (user.getString("aadhaar_id").equals(MainActivity.aadhaarId)) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
