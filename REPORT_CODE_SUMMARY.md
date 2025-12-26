# Smart Voting App - Final Project Report

# Manifest

## AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.smartvotingapp">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_logo"
        android:label="SmartVotingApp"
        android:roundIcon="@drawable/ic_logo"
        android:supportsRtl="true"
        android:theme="@style/Theme.SmartVotingApp">
        <activity
            android:name=".VotingActivity"
            android:exported="false" />
        <activity
            android:name=".SplashActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".LoginActivity"
            android:exported="false" />
        <activity
            android:name=".MainActivity"
            android:exported="false" />
        <activity
            android:name=".GovernmentLoginActivity"
            android:exported="true" />
        <activity
            android:name=".AdminDashboardActivity"
            android:exported="true" />
        <activity
            android:name=".NotificationActivity"
            android:exported="false" />
        <activity
            android:name=".PartyDetailsActivity"
            android:exported="true"
            android:label="Party Details"
            android:theme="@style/Theme.SmartVotingApp" />
    </application>
</manifest>
```
---

# Java Source Code

## AccountFragment.java
```java
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
                txtEligible.setTextColor(0xFF059669); // Green
            } else {
                txtEligible.setTextColor(0xFFDC2626); // Red
            }
            editEmail.setText(user.getEmail());
        } else {
            CustomAlert.showError(getContext(), "Error", "User data not found");
        }
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
        userFeedback.sort((f1, f2) -> Long.compare(f2.getTimestamp(), f1.getTimestamp()));
        long lastCheckTime = prefs.getLong("lastFeedbackCheck_" + user.getAadhaarId(), 0);
        int newlyResolvedCount = feedbackManager.getNewlyResolvedCount(user.getAadhaarId(), lastCheckTime);
        if (newlyResolvedCount > 0) {
            tvFeedbackBadge.setText(String.valueOf(newlyResolvedCount));
            tvFeedbackBadge.setVisibility(View.VISIBLE);
        } else {
            tvFeedbackBadge.setVisibility(View.GONE);
        }
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
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy",
                    java.util.Locale.getDefault());
            tvDate.setText("Submitted on: " + sdf.format(new java.util.Date(feedback.getTimestamp())));
            updateStatusBadge(tvStatus, feedback.getStatus());
            if (feedback.getStatus().equals("resolved") &&
                    feedback.getAdminResponse() != null &&
                    !feedback.getAdminResponse().isEmpty()) {
                layoutAdminResponse.setVisibility(View.VISIBLE);
                tvAdminResponse.setText(feedback.getAdminResponse());
                tvResolvedDate
                        .setText("Resolved on: " + sdf.format(new java.util.Date(feedback.getResolvedTimestamp())));
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
```
---

## AdminDashboardActivity.java
```java
public class AdminDashboardActivity extends AppCompatActivity {
    private TextView dashboardTitle;
    private EditText etSearch;
    private TextView tvNotificationBadge;
    private NotificationHelper notificationHelper;
    private BottomNavigationView bottomNavigationView;
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
        try {
            loadFragment(new AdminHomeFragment());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            } else if (itemId == R.id.nav_admin_results) {
                selectedFragment = new AdminResultFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                etSearch.setText(""); // Clear search
            }
            return true;
        });
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
        notificationIcon.setOnClickListener(v -> {
            Intent notificationIntent = new Intent(AdminDashboardActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        });
        updateNotificationBadge();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }
    private void updateNotificationBadge() {
        int count = notificationHelper.getUnreadCount();
        if (count > 0) {
            tvNotificationBadge.setVisibility(android.view.View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(count));
        } else {
            tvNotificationBadge.setVisibility(android.view.View.GONE);
        }
    }
    private void performSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearch(query);
        }
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
```
---

## AdminElectionFragment.java
```java
public class AdminElectionFragment extends Fragment {
    private LinearLayout electionContainer;
    private ElectionManager electionManager;
    public AdminElectionFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_election, container, false);
        electionManager = new ElectionManager(getContext());
        electionContainer = view.findViewById(R.id.electionContainer);
        Button btnAddElection = view.findViewById(R.id.btnAddElection);
        btnAddElection.setOnClickListener(v -> showAddEditDialog(null));
        loadElections();
        return view;
    }
    private void loadElections() {
        electionContainer.removeAllViews();
        List<Election> elections = electionManager.getAllElections();
        for (Election election : elections) {
            View electionView = LayoutInflater.from(getContext()).inflate(R.layout.item_election_admin,
                    electionContainer, false);
            TextView title = electionView.findViewById(R.id.tvTitle);
            TextView state = electionView.findViewById(R.id.tvState);
            TextView status = electionView.findViewById(R.id.tvStatus);
            TextView stopDate = electionView.findViewById(R.id.tvStopDate);
            Button btnEdit = electionView.findViewById(R.id.btnEdit);
            Button btnDelete = electionView.findViewById(R.id.btnDelete);
            Button btnManageOptions = electionView.findViewById(R.id.btnManageOptions);
            title.setText(election.getTitle());
            state.setText("State: " + election.getState());
            status.setText("Status: " + election.getStatus());
            stopDate.setText("Stop Date: " + election.getStopDate());
            btnEdit.setOnClickListener(v -> showAddEditDialog(election));
            btnDelete.setOnClickListener(v -> {
                electionManager.deleteElection(election.getId());
                loadElections();
            });
            btnManageOptions.setOnClickListener(v -> showManageVotingOptionsDialog(election));
            electionContainer.addView(electionView);
        }
    }
    private void showAddEditDialog(Election election) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_election, null);
        builder.setView(view);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etState = view.findViewById(R.id.etState);
        EditText etMinAge = view.findViewById(R.id.etMinAge);
        EditText etStatus = view.findViewById(R.id.etStatus);
        EditText etStopDate = view.findViewById(R.id.etStopDate);
        etStopDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (dp, year, month, day) -> {
                etStopDate.setText(year + "-" + (month + 1) + "-" + day);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        if (election != null) {
            etTitle.setText(election.getTitle());
            etState.setText(election.getState());
            etMinAge.setText(String.valueOf(election.getMinAge()));
            etStatus.setText(election.getStatus());
            etStopDate.setText(election.getStopDate());
        }
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String state = etState.getText().toString().trim();
            String minAgeStr = etMinAge.getText().toString().trim();
            String status = etStatus.getText().toString().trim();
            String stopDate = etStopDate.getText().toString().trim();
            if (title.isEmpty() || state.isEmpty() || minAgeStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            int minAge = Integer.parseInt(minAgeStr);
            if (election == null) {
                int id = new Random().nextInt(10000);
                Election newElection = new Election(id, title, state, minAge, status, stopDate);
                electionManager.addElection(newElection);
            } else {
                Election updatedElection = new Election(election.getId(), title, state, minAge, status, stopDate);
                electionManager.updateElection(updatedElection);
            }
            loadElections();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void showManageVotingOptionsDialog(Election election) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_voting_options, null);
        builder.setView(view);
        TextView tvElectionTitle = view.findViewById(R.id.tvElectionTitle);
        Button btnAddOption = view.findViewById(R.id.btnAddOption);
        LinearLayout optionsContainer = view.findViewById(R.id.optionsContainer);
        tvElectionTitle.setText("Manage Options: " + election.getTitle());
        VotingOptionManager optionManager = new VotingOptionManager(getContext());
        final Runnable[] loadOptionsWrapper = new Runnable[1];
        loadOptionsWrapper[0] = () -> {
            optionsContainer.removeAllViews();
            List<VotingOption> options = optionManager.getOptionsByElection(election.getId());
            if (options.isEmpty()) {
                TextView emptyView = new TextView(getContext());
                emptyView.setText("No voting options yet. Add some!");
                emptyView.setPadding(20, 20, 20, 20);
                optionsContainer.addView(emptyView);
            } else {
                for (VotingOption option : options) {
                    View optionView = LayoutInflater.from(getContext()).inflate(
                            R.layout.item_voting_option_admin, optionsContainer, false);
                    TextView tvOptionName = optionView.findViewById(R.id.tvOptionName);
                    TextView tvOptionDescription = optionView.findViewById(R.id.tvOptionDescription);
                    Button btnDelete = optionView.findViewById(R.id.btnDelete);
                    tvOptionName.setText(option.getOptionName());
                    tvOptionDescription.setText(option.getDescription());
                    btnDelete.setOnClickListener(v -> {
                        optionManager.deleteOption(option.getId());
                        loadOptionsWrapper[0].run();
                    });
                    optionsContainer.addView(optionView);
                }
            }
        };
        loadOptionsWrapper[0].run();
        btnAddOption.setOnClickListener(v -> showAddVotingOptionDialog(election, optionManager, loadOptionsWrapper[0]));
        builder.setPositiveButton("Done", null);
        builder.show();
    }
    private void showAddVotingOptionDialog(Election election, VotingOptionManager optionManager, Runnable onComplete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_voting_option, null);
        builder.setView(view);
        android.widget.Spinner spinnerParties = view.findViewById(R.id.spinnerParties);
        EditText etOptionName = view.findViewById(R.id.etOptionName);
        EditText etOptionDescription = view.findViewById(R.id.etOptionDescription);
        PartyManager partyManager = new PartyManager(getContext());
        List<Party> parties = partyManager.getAllParties();
        List<String> partyNames = new java.util.ArrayList<>();
        partyNames.add("Select a Party (Optional)");
        for (Party p : parties) {
            partyNames.add(p.getName());
        }
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, partyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParties.setAdapter(adapter);
        final String[] selectedLogoPath = { null };
        spinnerParties.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Party selectedParty = parties.get(position - 1);
                    etOptionDescription.setText(selectedParty.getName());
                    selectedLogoPath[0] = selectedParty.getLogoPath();
                } else {
                    etOptionDescription.setText("");
                    selectedLogoPath[0] = null;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        builder.setPositiveButton("Add", (dialog, which) -> {
            String candidateName = etOptionName.getText().toString().trim();
            String partyName = etOptionDescription.getText().toString().trim();
            if (candidateName.isEmpty()) {
                Toast.makeText(getContext(), "Candidate name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (partyName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a party", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = java.util.UUID.randomUUID().toString();
            VotingOption option = new VotingOption(id, election.getId(), candidateName, partyName, selectedLogoPath[0]);
            optionManager.addOption(option);
            onComplete.run();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
```
---

## AdminFeedbackFragment.java
```java
public class AdminFeedbackFragment extends Fragment {
    private LinearLayout feedbackContainer;
    private LinearLayout layoutEmptyState;
    private FeedbackManager feedbackManager;
    private Button btnFilterAll, btnFilterPending, btnFilterResolved;
    private String currentFilter = "all";
    public AdminFeedbackFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_feedback, container, false);
        feedbackManager = new FeedbackManager(getContext());
        feedbackContainer = view.findViewById(R.id.feedbackContainer);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPending = view.findViewById(R.id.btnFilterPending);
        btnFilterResolved = view.findViewById(R.id.btnFilterResolved);
        setupFilterButtons();
        loadFeedback();
        return view;
    }
    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtonStyles();
            loadFeedback();
        });
        btnFilterPending.setOnClickListener(v -> {
            currentFilter = "pending";
            updateFilterButtonStyles();
            loadFeedback();
        });
        btnFilterResolved.setOnClickListener(v -> {
            currentFilter = "resolved";
            updateFilterButtonStyles();
            loadFeedback();
        });
    }
    private void updateFilterButtonStyles() {
        setButtonOutlined(btnFilterAll);
        setButtonOutlined(btnFilterPending);
        setButtonOutlined(btnFilterResolved);
        if (currentFilter.equals("all")) {
            setButtonFilled(btnFilterAll);
        } else if (currentFilter.equals("pending")) {
            setButtonFilled(btnFilterPending);
        } else if (currentFilter.equals("resolved")) {
            setButtonFilled(btnFilterResolved);
        }
    }
    private void setButtonFilled(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(android.R.color.holo_blue_dark, null)));
        button.setTextColor(getResources().getColor(android.R.color.white, null));
    }
    private void setButtonOutlined(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(android.R.color.transparent, null)));
        button.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
    }
    private void loadFeedback() {
        feedbackContainer.removeAllViews();
        List<Feedback> feedbackList = feedbackManager.getAllFeedback();
        feedbackList.removeIf(feedback -> {
            if (currentFilter.equals("pending")) {
                return !feedback.getStatus().equals("pending") && !feedback.getStatus().equals("in_progress");
            } else if (currentFilter.equals("resolved")) {
                return !feedback.getStatus().equals("resolved");
            }
            return false;
        });
        if (feedbackList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        layoutEmptyState.setVisibility(View.GONE);
        feedbackList.sort((f1, f2) -> Long.compare(f2.getTimestamp(), f1.getTimestamp()));
        for (Feedback feedback : feedbackList) {
            View feedbackView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_admin_feedback_card, feedbackContainer, false);
            TextView tvTitle = feedbackView.findViewById(R.id.tvAdminFeedbackTitle);
            TextView tvStatus = feedbackView.findViewById(R.id.tvAdminFeedbackStatus);
            TextView tvUser = feedbackView.findViewById(R.id.tvAdminFeedbackUser);
            TextView tvDescription = feedbackView.findViewById(R.id.tvAdminFeedbackDescription);
            TextView tvDate = feedbackView.findViewById(R.id.tvAdminFeedbackDate);
            LinearLayout layoutResponse = feedbackView.findViewById(R.id.layoutAdminResponsePreview);
            TextView tvResponse = feedbackView.findViewById(R.id.tvAdminResponsePreview);
            Button btnResolve = feedbackView.findViewById(R.id.btnResolveFeedback);
            Button btnDelete = feedbackView.findViewById(R.id.btnDeleteFeedback);
            tvTitle.setText(feedback.getTitle());
            tvDescription.setText(feedback.getDescription());
            String maskedAadhaar = maskAadhaar(feedback.getUserAadhaar());
            tvUser.setText(feedback.getUserName() + " • " + maskedAadhaar);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            tvDate.setText("Submitted on: " + sdf.format(new Date(feedback.getTimestamp())));
            updateStatusBadge(tvStatus, feedback.getStatus());
            if (feedback.getAdminResponse() != null && !feedback.getAdminResponse().isEmpty()) {
                layoutResponse.setVisibility(View.VISIBLE);
                tvResponse.setText(feedback.getAdminResponse());
            }
            if (feedback.getStatus().equals("resolved")) {
                btnResolve.setText("View Details");
            } else {
                btnResolve.setText("Resolve");
            }
            btnResolve.setOnClickListener(v -> showResolveDialog(feedback));
            btnDelete.setOnClickListener(v -> deleteFeedback(feedback));
            feedbackContainer.addView(feedbackView);
        }
    }
    private void updateStatusBadge(TextView badge, String status) {
        switch (status) {
            case "pending":
                badge.setText("Pending");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_orange_dark, null)));
                break;
            case "in_progress":
                badge.setText("In Progress");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_blue_light, null)));
                break;
            case "resolved":
                badge.setText("Resolved");
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        getResources().getColor(android.R.color.holo_green_dark, null)));
                break;
        }
    }
    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "XXXX-XXXX-XXXX";
        }
        return "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4);
    }
    private void showResolveDialog(Feedback feedback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_resolve_feedback, null);
        builder.setView(dialogView);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogFeedbackTitle);
        TextView tvUserInfo = dialogView.findViewById(R.id.tvDialogUserInfo);
        TextView tvDescription = dialogView.findViewById(R.id.tvDialogFeedbackDescription);
        EditText etResponse = dialogView.findViewById(R.id.etAdminResponse);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelResolve);
        Button btnMarkResolved = dialogView.findViewById(R.id.btnMarkResolved);
        tvTitle.setText(feedback.getTitle());
        tvUserInfo.setText("User: " + feedback.getUserName() + " • " + maskAadhaar(feedback.getUserAadhaar()));
        tvDescription.setText(feedback.getDescription());
        if (feedback.getAdminResponse() != null && !feedback.getAdminResponse().isEmpty()) {
            etResponse.setText(feedback.getAdminResponse());
        }
        AlertDialog dialog = builder.create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnMarkResolved.setOnClickListener(v -> {
            String response = etResponse.getText().toString().trim();
            if (response.isEmpty()) {
                Toast.makeText(getContext(), "Please provide a response", Toast.LENGTH_SHORT).show();
                return;
            }
            feedback.setAdminResponse(response);
            feedback.setStatus("resolved");
            feedback.setResolvedTimestamp(System.currentTimeMillis());
            if (feedbackManager.updateFeedback(feedback)) {
                Toast.makeText(getContext(), "Feedback resolved successfully", Toast.LENGTH_SHORT).show();
                loadFeedback();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to update feedback", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    private void deleteFeedback(Feedback feedback) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Feedback")
                .setMessage("Are you sure you want to delete this feedback?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (feedbackManager.deleteFeedback(feedback.getId())) {
                        Toast.makeText(getContext(), "Feedback deleted", Toast.LENGTH_SHORT).show();
                        loadFeedback();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete feedback", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
```
---

## AdminHomeFragment.java
```java
public class AdminHomeFragment extends Fragment {
    private LinearLayout newsContainer;
    private NewsManager newsManager;
    public AdminHomeFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);
        newsManager = new NewsManager(getContext());
        newsContainer = view.findViewById(R.id.newsContainer);
        Button btnAddNews = view.findViewById(R.id.btnAddNews);
        Button btnViewFeedback = view.findViewById(R.id.btnViewFeedback);
        btnAddNews.setOnClickListener(v -> showAddEditDialog(null));
        btnViewFeedback.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminFeedbackFragment())
                    .addToBackStack(null)
                    .commit();
        });
        loadNews();
        return view;
    }
    private void loadNews() {
        newsContainer.removeAllViews();
        List<News> newsList = newsManager.getAllNews();
        for (News news : newsList) {
            View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_admin, newsContainer, false);
            TextView title = newsView.findViewById(R.id.tvTitle);
            TextView date = newsView.findViewById(R.id.tvDate);
            TextView desc = newsView.findViewById(R.id.tvDescription);
            android.widget.ImageView imgNews = newsView.findViewById(R.id.imgNews);
            Button btnEdit = newsView.findViewById(R.id.btnEdit);
            Button btnDelete = newsView.findViewById(R.id.btnDelete);
            title.setText(news.getTitle());
            date.setText(news.getDate());
            desc.setText(news.getDescription());
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                imgNews.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        java.io.InputStream in = new java.net.URL(news.getImageUrl()).openStream();
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                        newsView.post(() -> imgNews.setImageBitmap(bmp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                imgNews.setVisibility(View.GONE);
            }
            btnEdit.setOnClickListener(v -> showAddEditDialog(news));
            btnDelete.setOnClickListener(v -> {
                newsManager.deleteNews(news.getId());
                loadNews();
            });
            newsContainer.addView(newsView);
        }
    }
    private androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher;
    private EditText currentImageUrlInput;
    private android.widget.ImageView currentImagePreview;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContext().getContentResolver().takePersistableUriPermission(uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (currentImageUrlInput != null) {
                            currentImageUrlInput.setText(uri.toString());
                        }
                        if (currentImagePreview != null) {
                            currentImagePreview.setVisibility(View.VISIBLE);
                            currentImagePreview.setImageURI(uri);
                        }
                    }
                });
    }
    private void showAddEditDialog(News news) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_news, null);
        builder.setView(view);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etImageUrl = view.findViewById(R.id.etImageUrl);
        Button btnPickImage = view.findViewById(R.id.btnPickImage);
        android.widget.ImageView imgPreview = view.findViewById(R.id.imgPreview);
        currentImageUrlInput = etImageUrl;
        currentImagePreview = imgPreview;
        if (news != null) {
            etTitle.setText(news.getTitle());
            etDescription.setText(news.getDescription());
            etImageUrl.setText(news.getImageUrl());
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(news.getImageUrl());
                    if (uri.getScheme() != null
                            && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                        imgPreview.setVisibility(View.VISIBLE);
                        imgPreview.setImageURI(uri);
                    }
                } catch (Exception e) {
                }
            }
        }
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();
            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            long timestamp = System.currentTimeMillis();
            if (news == null) {
                News newNews = new News(UUID.randomUUID().toString(), title, desc, date, timestamp, imageUrl);
                newsManager.addNews(newNews);
            } else {
                News updatedNews = new News(news.getId(), title, desc, date, timestamp, imageUrl);
                newsManager.updateNews(updatedNews);
            }
            loadNews();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
```
---

## AdminPartyFragment.java
```java
public class AdminPartyFragment extends Fragment {
    private LinearLayout partyContainer;
    private PartyManager partyManager;
    private static final int REQUEST_IMAGE_PICK = 200;
    private Uri selectedImageUri;
    private ImageView currentLogoPreview;
    private Party editingParty;
    public AdminPartyFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_party, container, false);
        partyManager = new PartyManager(getContext());
        partyContainer = view.findViewById(R.id.partyContainer);
        Button btnAddParty = view.findViewById(R.id.btnAddParty);
        btnAddParty.setOnClickListener(v -> showAddEditDialog(null));
        loadParties();
        return view;
    }
    private void loadParties() {
        partyContainer.removeAllViews();
        List<Party> parties = partyManager.getAllParties();
        for (Party party : parties) {
            View partyView = LayoutInflater.from(getContext()).inflate(R.layout.item_party_admin, partyContainer,
                    false);
            TextView name = partyView.findViewById(R.id.tvPartyName);
            TextView symbol = partyView.findViewById(R.id.tvPartySymbol);
            Button btnEdit = partyView.findViewById(R.id.btnEdit);
            Button btnDelete = partyView.findViewById(R.id.btnDelete);
            name.setText(party.getName());
            symbol.setText("Symbol: " + party.getSymbol());
            btnEdit.setOnClickListener(v -> showAddEditDialog(party));
            btnDelete.setOnClickListener(v -> {
                partyManager.deleteParty(party.getId());
                loadParties();
            });
            partyContainer.addView(partyView);
        }
    }
    private void showAddEditDialog(Party party) {
        editingParty = party;
        selectedImageUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_party, null);
        builder.setView(view);
        EditText etName = view.findViewById(R.id.etPartyName);
        EditText etSymbol = view.findViewById(R.id.etPartySymbol);
        EditText etDescription = view.findViewById(R.id.etPartyDescription);
        currentLogoPreview = view.findViewById(R.id.ivPartyLogo);
        Button btnSelectLogo = view.findViewById(R.id.btnSelectLogo);
        if (party != null) {
            etName.setText(party.getName());
            etSymbol.setText(party.getSymbol());
            etDescription.setText(party.getDescription());
            if (party.getLogoPath() != null) {
                loadImageToView(party.getLogoPath(), currentLogoPreview);
            }
        }
        btnSelectLogo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String symbol = etSymbol.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Party name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            String logoPath = null;
            if (selectedImageUri != null) {
                logoPath = saveImageToInternalStorage(selectedImageUri, name);
            } else if (party != null && party.getLogoPath() != null) {
                logoPath = party.getLogoPath();
            }
            if (party == null) {
                Party newParty = new Party(UUID.randomUUID().toString(), name, symbol, description, logoPath);
                partyManager.addParty(newParty);
            } else {
                Party updatedParty = new Party(party.getId(), name, symbol, description, logoPath);
                partyManager.updateParty(updatedParty);
            }
            loadParties();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (currentLogoPreview != null && selectedImageUri != null) {
                currentLogoPreview.setImageURI(selectedImageUri);
            }
        }
    }
    private String saveImageToInternalStorage(Uri imageUri, String partyName) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            String filename = "party_logo_" + partyName.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
            File file = new File(getContext().getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    private void loadImageToView(String filename, ImageView imageView) {
        if (filename == null)
            return;
        if (filename.startsWith("res:")) {
            String resName = filename.substring(4);
            int resId = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
            }
            return;
        }
        try {
            File file = new File(getContext().getFilesDir(), filename);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
---

## AdminResultFragment.java
```java
public class AdminResultFragment extends Fragment {
    private LinearLayout resultsContainer;
    private ElectionManager electionManager;
    private VoteManager voteManager;
    private VotingOptionManager optionManager;
    public AdminResultFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_result, container, false);
        resultsContainer = view.findViewById(R.id.resultsContainer);
        electionManager = new ElectionManager(getContext());
        voteManager = new VoteManager(getContext());
        optionManager = new VotingOptionManager(getContext());
        loadElections();
        return view;
    }
    private void loadElections() {
        resultsContainer.removeAllViews();
        List<Election> elections = electionManager.getAllElections();
        if (elections.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No elections found.");
            emptyView.setPadding(32, 32, 32, 32);
            resultsContainer.addView(emptyView);
            return;
        }
        for (Election election : elections) {
            View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_result_card, resultsContainer,
                    false);
            TextView tvTitle = cardView.findViewById(R.id.tvTitle);
            TextView tvStatus = cardView.findViewById(R.id.tvStatus);
            TextView tvResultDate = cardView.findViewById(R.id.tvResultDate);
            Button btnSetDate = cardView.findViewById(R.id.btnSetDate);
            LinearLayout layoutCounts = cardView.findViewById(R.id.layoutCounts);
            tvTitle.setText(election.getTitle());
            tvStatus.setText("Status: " + election.getStatus());
            String date = election.getResultDate();
            tvResultDate.setText(date != null ? "Results Date: " + date : "Results Date: Not Set");
            btnSetDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (dp, year, month, day) -> {
                    String newDate = year + "-" + (month + 1) + "-" + day;
                    election.setResultDate(newDate);
                    electionManager.updateElection(election);
                    loadElections(); // Refresh
                    Toast.makeText(getContext(), "Result date updated", Toast.LENGTH_SHORT).show();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
            Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
            List<VotingOption> options = optionManager.getOptionsByElection(election.getId());
            layoutCounts.removeAllViews();
            if (options.isEmpty()) {
                TextView tvNoOptions = new TextView(getContext());
                tvNoOptions.setText("No candidates configured.");
                layoutCounts.addView(tvNoOptions);
            } else {
                int totalVotes = 0;
                for (VotingOption option : options) {
                    totalVotes += voteCounts.getOrDefault(option.getId(), 0);
                }
                for (VotingOption option : options) {
                    View countView = LayoutInflater.from(getContext()).inflate(R.layout.item_result_count_row,
                            layoutCounts, false);
                    TextView tvName = countView.findViewById(R.id.tvOptionName);
                    TextView tvCount = countView.findViewById(R.id.tvVoteCount);
                    android.widget.ProgressBar progressBar = countView.findViewById(R.id.progressBar);
                    int count = voteCounts.getOrDefault(option.getId(), 0);
                    tvName.setText(option.getOptionName());
                    tvCount.setText(count + " votes");
                    progressBar.setMax(totalVotes > 0 ? totalVotes : 1);
                    progressBar.setProgress(count);
                    layoutCounts.addView(countView);
                }
                TextView tvTotal = new TextView(getContext());
                tvTotal.setText("Total Votes: " + totalVotes);
                tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTotal.setPadding(0, 16, 0, 0);
                layoutCounts.addView(tvTotal);
            }
            resultsContainer.addView(cardView);
        }
    }
}
```
---

## AdminUserListFragment.java
```java
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
        Button btnAddUser = view.findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(v -> showAddUserDialog());
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
```
---

## CustomAlert.java
```java
public class CustomAlert {
    public static void showSuccess(Context context, String title, String message) {
        showAlert(context, title, message, R.color.success, android.R.drawable.checkbox_on_background);
    }
    public static void showError(Context context, String title, String message) {
        showAlert(context, title, message, R.color.error, android.R.drawable.ic_delete);
    }
    public static void showWarning(Context context, String title, String message) {
        showAlert(context, title, message, R.color.warning, android.R.drawable.ic_dialog_alert);
    }
    public static void showInfo(Context context, String title, String message) {
        showAlert(context, title, message, R.color.primary, android.R.drawable.ic_dialog_info);
    }
    private static void showAlert(Context context, String title, String message, int colorResId, int iconResId) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_custom_alert);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }
        TextView tvTitle = dialog.findViewById(R.id.tvAlertTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvAlertMessage);
        ImageView imgIcon = dialog.findViewById(R.id.imgAlertIcon);
        View viewColor = dialog.findViewById(R.id.viewAlertColor);
        ImageView btnClose = dialog.findViewById(R.id.btnCloseAlert);
        tvTitle.setText(title);
        tvMessage.setText(message);
        int color = ContextCompat.getColor(context, colorResId);
        imgIcon.setImageResource(iconResId);
        imgIcon.setColorFilter(color);
        viewColor.setBackgroundColor(color);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        new android.os.Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 4000);
    }
}
```
---

## Election.java
```java
public class Election {
    private int id;
    private String title;
    private String state;
    private int minAge;
    private String status;
    private String stopDate;
    private String resultDate;
    public Election(int id, String title, String state, int minAge, String status, String stopDate) {
        this(id, title, state, minAge, status, stopDate, null);
    }
    public Election(int id, String title, String state, int minAge, String status, String stopDate, String resultDate) {
        this.id = id;
        this.title = title;
        this.state = state;
        this.minAge = minAge;
        this.status = status;
        this.stopDate = stopDate;
        this.resultDate = resultDate;
    }
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getState() {
        return state;
    }
    public int getMinAge() {
        return minAge;
    }
    public String getStatus() {
        return status;
    }
    public String getStopDate() {
        return stopDate;
    }
    public String getResultDate() {
        return resultDate;
    }
    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }
}
```
---

## ElectionAdapter.java
```java
public class ElectionAdapter extends RecyclerView.Adapter<ElectionAdapter.ViewHolder> {
    private List<Election> elections;
    private OnElectionClickListener listener;
    public interface OnElectionClickListener {
        void onElectionClick(Election election);
    }
    public ElectionAdapter(List<Election> elections, OnElectionClickListener listener) {
        this.elections = elections;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_election_card, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Election election = elections.get(position);
        holder.title.setText(election.getTitle());
        holder.state.setText(election.getState());
        holder.minAge.setText(election.getMinAge() + "+");
        holder.date.setText(election.getStopDate());
        holder.status.setText(election.getStatus());
        String status = election.getStatus().toLowerCase();
        if (status.equals("running") || status.equals("active") || status.equals("open")) {
            holder.status.setTextColor(0xFF059669); // Green
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            holder.btnVote.setEnabled(true);
            holder.btnVote.setAlpha(1.0f);
        } else {
            holder.status.setTextColor(0xFFDC2626); // Red
            holder.status.setBackgroundResource(R.drawable.bg_status_active); // Ideally different bg
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEE2E2)); // Light Red
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(0.5f);
            holder.btnVote.setText("Closed");
        }
        holder.btnVote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onElectionClick(election);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.btnVote.isEnabled()) {
                listener.onElectionClick(election);
            }
        });
    }
    @Override
    public int getItemCount() {
        return elections.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status, state, minAge, date;
        android.widget.Button btnVote;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvElectionTitle);
            status = itemView.findViewById(R.id.tvStatus);
            state = itemView.findViewById(R.id.tvState);
            minAge = itemView.findViewById(R.id.tvMinAge);
            date = itemView.findViewById(R.id.tvDate);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}
```
---

## ElectionManager.java
```java
public class ElectionManager {
    private static final String FILE_NAME = "elections.json";
    private Context context;
    public ElectionManager(Context context) {
        this.context = context;
    }
    public List<Election> getAllElections() {
        List<Election> list = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return loadDefaultElections();
        }
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("minAge"),
                        obj.getString("status"),
                        obj.optString("stopDate", ""),
                        obj.optString("resultDate", null)));
            }
        } catch (Exception e) {
            Log.e("ElectionManager", "Error reading elections", e);
        }
        return list;
    }
    public void addElection(Election election) {
        List<Election> list = getAllElections();
        list.add(election);
        saveElections(list);
    }
    public void updateElection(Election election) {
        List<Election> list = getAllElections();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == election.getId()) {
                list.set(i, election);
                break;
            }
        }
        saveElections(list);
    }
    public void deleteElection(int id) {
        List<Election> list = getAllElections();
        list.removeIf(e -> e.getId() == id);
        saveElections(list);
    }
    private void saveElections(List<Election> list) {
        JSONArray array = new JSONArray();
        for (Election e : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", e.getId());
                obj.put("title", e.getTitle());
                obj.put("state", e.getState());
                obj.put("minAge", e.getMinAge());
                obj.put("status", e.getStatus());
                obj.put("stopDate", e.getStopDate());
                if (e.getResultDate() != null) {
                    obj.put("resultDate", e.getResultDate());
                }
                array.put(obj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("ElectionManager", "Error saving elections", e);
        }
    }
    private List<Election> loadDefaultElections() {
        List<Election> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("elections_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("min_age"), // Note: JSON uses min_age, verify this matches
                        obj.getString("status"),
                        obj.optString("stopDate", ""),
                        obj.optString("resultDate", null)));
            }
            saveElections(list);
        } catch (Exception e) {
            Log.e("ElectionManager", "Error reading default elections", e);
        }
        return list;
    }
}
```
---

## ElectionUtils.java
```java
public class ElectionUtils {
    public static List<Election> loadElectionsFromAssets(Context context) {
        List<Election> elections = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("elections_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                elections.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("min_age"),
                        obj.getString("status"),
                        obj.optString("stop_date", "")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elections;
    }
}
```
---

## Feedback.java
```java
public class Feedback {
    private String id;
    private String userId;
    private String userName;
    private String userAadhaar;
    private String title;
    private String description;
    private String status; // "pending", "in_progress", "resolved"
    private String adminResponse;
    private long timestamp;
    private long resolvedTimestamp;
    public Feedback() {
    }
    public Feedback(String id, String userId, String userName, String userAadhaar,
            String title, String description, String status,
            String adminResponse, long timestamp, long resolvedTimestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userAadhaar = userAadhaar;
        this.title = title;
        this.description = description;
        this.status = status;
        this.adminResponse = adminResponse;
        this.timestamp = timestamp;
        this.resolvedTimestamp = resolvedTimestamp;
    }
    public String getId() {
        return id;
    }
    public String getUserId() {
        return userId;
    }
    public String getUserName() {
        return userName;
    }
    public String getUserAadhaar() {
        return userAadhaar;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public String getAdminResponse() {
        return adminResponse;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public long getResolvedTimestamp() {
        return resolvedTimestamp;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserAadhaar(String userAadhaar) {
        this.userAadhaar = userAadhaar;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void setResolvedTimestamp(long resolvedTimestamp) {
        this.resolvedTimestamp = resolvedTimestamp;
    }
}
```
---

## FeedbackManager.java
```java
public class FeedbackManager {
    private static final String FEEDBACK_FILE = "feedback_data.json";
    private Context context;
    public FeedbackManager(Context context) {
        this.context = context;
    }
    public boolean addFeedback(Feedback feedback) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            feedbackList.add(feedback);
            return saveFeedbackList(feedbackList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FEEDBACK_FILE);
            if (!file.exists()) {
                return feedbackList;
            }
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Feedback feedback = new Feedback(
                        obj.getString("id"),
                        obj.getString("userId"),
                        obj.getString("userName"),
                        obj.getString("userAadhaar"),
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("status"),
                        obj.optString("adminResponse", ""),
                        obj.getLong("timestamp"),
                        obj.optLong("resolvedTimestamp", 0));
                feedbackList.add(feedback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedbackList;
    }
    public List<Feedback> getFeedbackByUserId(String userId) {
        List<Feedback> userFeedback = new ArrayList<>();
        List<Feedback> allFeedback = getAllFeedback();
        for (Feedback feedback : allFeedback) {
            if (feedback.getUserId().equals(userId)) {
                userFeedback.add(feedback);
            }
        }
        return userFeedback;
    }
    public Feedback getFeedbackById(String id) {
        List<Feedback> feedbackList = getAllFeedback();
        for (Feedback feedback : feedbackList) {
            if (feedback.getId().equals(id)) {
                return feedback;
            }
        }
        return null;
    }
    public boolean updateFeedback(Feedback updatedFeedback) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            for (int i = 0; i < feedbackList.size(); i++) {
                if (feedbackList.get(i).getId().equals(updatedFeedback.getId())) {
                    feedbackList.set(i, updatedFeedback);
                    return saveFeedbackList(feedbackList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteFeedback(String id) {
        try {
            List<Feedback> feedbackList = getAllFeedback();
            feedbackList.removeIf(feedback -> feedback.getId().equals(id));
            return saveFeedbackList(feedbackList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public int getUnresolvedCount(String userId) {
        int count = 0;
        List<Feedback> userFeedback = getFeedbackByUserId(userId);
        for (Feedback feedback : userFeedback) {
            if (!feedback.getStatus().equals("resolved")) {
                count++;
            }
        }
        return count;
    }
    public int getNewlyResolvedCount(String userId, long lastCheckTimestamp) {
        int count = 0;
        List<Feedback> userFeedback = getFeedbackByUserId(userId);
        for (Feedback feedback : userFeedback) {
            if (feedback.getStatus().equals("resolved") &&
                    feedback.getResolvedTimestamp() > lastCheckTimestamp) {
                count++;
            }
        }
        return count;
    }
    private boolean saveFeedbackList(List<Feedback> feedbackList) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Feedback feedback : feedbackList) {
                JSONObject obj = new JSONObject();
                obj.put("id", feedback.getId());
                obj.put("userId", feedback.getUserId());
                obj.put("userName", feedback.getUserName());
                obj.put("userAadhaar", feedback.getUserAadhaar());
                obj.put("title", feedback.getTitle());
                obj.put("description", feedback.getDescription());
                obj.put("status", feedback.getStatus());
                obj.put("adminResponse", feedback.getAdminResponse());
                obj.put("timestamp", feedback.getTimestamp());
                obj.put("resolvedTimestamp", feedback.getResolvedTimestamp());
                jsonArray.put(obj);
            }
            File file = new File(context.getFilesDir(), FEEDBACK_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
```
---

## GovernmentLoginActivity.java
```java
public class GovernmentLoginActivity extends AppCompatActivity {
    EditText deptCodeInput, passwordInput;
    Button loginButton;
    private static final String TAG = "GovLogin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    Toast.makeText(this, "Error launching Admin Dashboard:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
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
```
---

## HistoryFragment.java
```java
public class HistoryFragment extends Fragment {
    private RecyclerView rvHistory;
    public HistoryFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHistory();
        return view;
    }
    private void loadHistory() {
        ElectionManager electionManager = new ElectionManager(getContext());
        List<Election> elections = electionManager.getAllElections();
        HistoryAdapter adapter = new HistoryAdapter(elections, getContext());
        rvHistory.setAdapter(adapter);
    }
    private String readFile(String filename) {
        File file = new File(getContext().getFilesDir(), filename);
        if (!file.exists())
            return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<Election> elections;
        private VoteManager voteManager;
        private VotingOptionManager optionManager;
        public HistoryAdapter(List<Election> elections, android.content.Context context) {
            this.elections = elections;
            this.voteManager = new VoteManager(context);
            this.optionManager = new VotingOptionManager(context);
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_card, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Election election = elections.get(position);
            holder.tvTitle.setText(election.getTitle());
            boolean isDeclared = false;
            String resultDateStr = election.getResultDate();
            Date resultDate = null;
            if (resultDateStr != null && !resultDateStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    resultDate = sdf.parse(resultDateStr);
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    today.set(java.util.Calendar.MINUTE, 0);
                    today.set(java.util.Calendar.SECOND, 0);
                    today.set(java.util.Calendar.MILLISECOND, 0);
                    java.util.Calendar resultCal = java.util.Calendar.getInstance();
                    resultCal.setTime(resultDate);
                    resultCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    resultCal.set(java.util.Calendar.MINUTE, 0);
                    resultCal.set(java.util.Calendar.SECOND, 0);
                    resultCal.set(java.util.Calendar.MILLISECOND, 0);
                    if (!today.before(resultCal)) {
                        isDeclared = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (isDeclared && resultDate != null) {
                holder.tvStatusBadge.setText("Declared");
                holder.tvStatusBadge.setTextColor(0xFF059669); // Green
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active); // Reuse green bg
                holder.tvDate.setText("Results declared on: "
                        + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(resultDate));
                holder.layoutResults.setVisibility(View.VISIBLE);
                holder.layoutWaiting.setVisibility(View.GONE);
                Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
                List<VotingOption> options = optionManager.getOptionsByElection(election.getId());
                if (options.isEmpty()) {
                    holder.tvResultSummary.setText("No candidates/options found.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    String winner = "N/A";
                    int maxVotes = -1;
                    for (VotingOption option : options) {
                        int count = voteCounts.getOrDefault(option.getId(), 0);
                        if (count > maxVotes) {
                            maxVotes = count;
                            winner = option.getOptionName();
                        }
                        sb.append("• ").append(option.getOptionName()).append(": ").append(count).append("\n");
                    }
                    String summary = "🎉 Congratulations to " + winner + "!\n\n" +
                            "🏆 Winner: " + winner + " (" + maxVotes + " votes)\n\n" +
                            "Detailed Results:\n" + sb.toString();
                    holder.tvResultSummary.setText(summary.trim());
                }
            } else {
                holder.tvStatusBadge.setText("Pending");
                holder.tvStatusBadge.setTextColor(0xFFD97706); // Amber/Orange
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
                holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEF3C7)); // Light
                if (resultDate != null) {
                    holder.tvDate.setText("Results on: "
                            + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(resultDate));
                } else {
                    holder.tvDate.setText("Results Date: To be announced");
                }
                holder.layoutResults.setVisibility(View.GONE);
                holder.layoutWaiting.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public int getItemCount() {
            return elections.size();
        }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatusBadge, tvDate, tvResultSummary;
            LinearLayout layoutResults, layoutWaiting;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvElectionTitle);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvResultSummary = itemView.findViewById(R.id.tvResultSummary);
                layoutResults = itemView.findViewById(R.id.layoutResults);
                layoutWaiting = itemView.findViewById(R.id.layoutWaiting);
            }
        }
    }
}
```
---

## HomeFragment.java
```java
public class HomeFragment extends Fragment implements SearchableFragment {
    private LinearLayout newsContainer;
    private NewsManager newsManager;
    private List<News> allNews;
    public HomeFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        newsManager = new NewsManager(getContext());
        newsContainer = view.findViewById(R.id.newsContainer);
        loadSampleNewsIfNeeded();
        allNews = newsManager.getAllNews();
        loadNews(allNews);
        return view;
    }
    @Override
    public void onSearch(String query) {
        if (allNews == null)
            return;
        if (query == null || query.isEmpty()) {
            loadNews(allNews);
            return;
        }
        List<News> filteredList = new java.util.ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (News news : allNews) {
            if (news.getTitle().toLowerCase().contains(lowerQuery) ||
                    news.getDescription().toLowerCase().contains(lowerQuery)) {
                filteredList.add(news);
            }
        }
        loadNews(filteredList);
    }
    private void loadSampleNewsIfNeeded() {
        if (newsManager.getAllNews().isEmpty()) {
            try {
                java.io.InputStream is = getContext().getAssets().open("sample_news.json");
                java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8");
                String json = scanner.useDelimiter("\\A").next();
                scanner.close();
                org.json.JSONArray array = new org.json.JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);
                    News news = new News(
                            obj.getString("id"),
                            obj.getString("title"),
                            obj.getString("description"),
                            obj.getString("date"),
                            obj.getLong("timestamp"),
                            obj.optString("imageUrl", ""));
                    newsManager.addNews(news);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void loadNews(List<News> newsList) {
        newsContainer.removeAllViews();
        if (newsList.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No news available.");
            emptyView.setPadding(20, 20, 20, 20);
            newsContainer.addView(emptyView);
            return;
        }
        for (News news : newsList) {
            View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_card, newsContainer, false);
            TextView title = newsView.findViewById(R.id.tvTitle);
            TextView date = newsView.findViewById(R.id.tvDate);
            TextView desc = newsView.findViewById(R.id.tvDescription);
            android.widget.ImageView imgNews = newsView.findViewById(R.id.imgNews);
            title.setText(news.getTitle());
            date.setText(news.getDate());
            desc.setText(news.getDescription());
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                imgNews.setVisibility(View.VISIBLE);
                try {
                    android.net.Uri uri = android.net.Uri.parse(news.getImageUrl());
                    if (uri.getScheme() != null
                            && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                        imgNews.setImageURI(uri);
                    } else {
                        new Thread(() -> {
                            try {
                                java.io.InputStream in = new java.net.URL(news.getImageUrl()).openStream();
                                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                                newsView.post(() -> imgNews.setImageBitmap(bmp));
                            } catch (Exception e) {
                                e.printStackTrace();
                                newsView.post(() -> imgNews.setImageResource(R.drawable.ic_news_placeholder));
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imgNews.setImageResource(R.drawable.ic_news_placeholder);
                }
            } else {
                imgNews.setImageResource(R.drawable.ic_news_placeholder);
            }
            android.view.animation.Animation animation = android.view.animation.AnimationUtils
                    .loadAnimation(getContext(), R.anim.slide_in_up);
            animation.setStartOffset(newsContainer.getChildCount() * 100); // Staggered effect
            newsView.startAnimation(animation);
            newsContainer.addView(newsView);
        }
    }
}
```
---

## ListFragment.java
```java
public class ListFragment extends Fragment implements SearchableFragment {
    private GridLayout gridParties;
    private PartyManager partyManager;
    private List<Party> allParties;
    public ListFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        gridParties = view.findViewById(R.id.gridParties);
        partyManager = new PartyManager(getContext());
        allParties = partyManager.getAllParties();
        loadParties(allParties);
        return view;
    }
    @Override
    public void onSearch(String query) {
        if (allParties == null)
            return;
        if (query == null || query.isEmpty()) {
            loadParties(allParties);
            return;
        }
        List<Party> filteredList = new java.util.ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Party party : allParties) {
            if (party.getName().toLowerCase().contains(lowerQuery) ||
                    party.getSymbol().toLowerCase().contains(lowerQuery)) {
                filteredList.add(party);
            }
        }
        loadParties(filteredList);
    }
    private void loadParties(List<Party> parties) {
        gridParties.removeAllViews();
        for (Party party : parties) {
            createPartyCard(party);
        }
    }
    private void createPartyCard(Party party) {
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_party_card, gridParties, false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(params);
        ImageView imgLogo = cardView.findViewById(R.id.imgPartyLogo);
        TextView tvName = cardView.findViewById(R.id.tvPartyName);
        tvName.setText(party.getName());
        if (party.getLogoPath() != null) {
            loadPartyLogo(party.getLogoPath(), imgLogo);
        } else {
            imgLogo.setImageResource(R.drawable.ic_bjp); // Fallback
        }
        cardView.setOnClickListener(v -> showPartyDetails(
                party.getName(),
                party.getSymbol(),
                party.getDescription(),
                party.getLogoPath()));
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_in_up);
        animation.setStartOffset(gridParties.getChildCount() * 50); // Staggered effect
        cardView.startAnimation(animation);
        gridParties.addView(cardView);
    }
    private void loadPartyLogo(String filename, ImageView imageView) {
        if (filename == null)
            return;
        if (filename.startsWith("res:")) {
            String resName = filename.substring(4);
            int resId = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
            } else {
                imageView.setImageResource(R.drawable.ic_bjp);
            }
            return;
        }
        try {
            File file = new File(getContext().getFilesDir(), filename);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_bjp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showPartyDetails(String name, String symbol, String description, String logoPath) {
        if (getContext() == null)
            return;
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_party_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ImageView imgLogo = dialog.findViewById(R.id.imgPartyLogo);
        TextView tvName = dialog.findViewById(R.id.tvPartyName);
        TextView tvSymbol = dialog.findViewById(R.id.tvPartySymbol);
        TextView tvDescription = dialog.findViewById(R.id.tvPartyDescription);
        Button btnClose = dialog.findViewById(R.id.btnClose);
        tvName.setText(name);
        tvSymbol.setText("Symbol: " + symbol);
        tvDescription.setText(description);
        if (logoPath != null) {
            loadPartyLogo(logoPath, imgLogo);
        } else {
            imgLogo.setImageResource(R.drawable.ic_bjp);
        }
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
```
---

## LoginActivity.java
```java
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String CHANNEL_ID = "otp_channel_01";
    private static final int OTP_NOTIFICATION_ID = 1001;
    EditText aadhaarInput, dobInput;
    Button loginButton;
    TextView govtLoginLink;
    Button sendOtpButton, verifyOtpButton, resendOtpButton;
    EditText otpInput;
    LinearLayout otpArea;
    TextView otpTimerText;
    private String currentOtp = null;
    private long otpExpiryMillis = 0;
    private CountDownTimer otpTimer;
    private boolean otpVerified = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        createNotificationChannel();
        aadhaarInput = findViewById(R.id.aadhaarInput);
        dobInput = findViewById(R.id.dobInput);
        loginButton = findViewById(R.id.loginButton);
        govtLoginLink = findViewById(R.id.govtLoginLink);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        otpInput = findViewById(R.id.otpInput);
        otpArea = findViewById(R.id.otpArea);
        otpTimerText = findViewById(R.id.otpTimerText);
        showOtpArea(false);
        otpVerified = false;
        setLoginButtonEnabled(false);
        govtLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GovernmentLoginActivity.class);
            startActivity(intent);
        });
        dobInput.setFocusable(false);
        dobInput.setClickable(true);
        dobInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    LoginActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = selectedYear + "-" +
                                String.format("%02d", (selectedMonth + 1)) + "-" +
                                String.format("%02d", selectedDay);
                        dobInput.setText(formattedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
        sendOtpButton.setOnClickListener(v -> {
            String aadhaar = aadhaarInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();
            if (aadhaar.isEmpty() || aadhaar.length() != 12) {
                CustomAlert.showError(this, "Invalid Input", "Enter valid 12-digit Aadhaar first");
                return;
            }
            if (dob.isEmpty()) {
                CustomAlert.showError(this, "Missing Info", "Please select DOB");
                return;
            }
            if (!validateUser(aadhaar, dob)) {
                CustomAlert.showError(this, "Authentication Failed", "Aadhaar/DOB not found. Please check details.");
                return;
            }
            currentOtp = generateOtp(6);
            otpExpiryMillis = System.currentTimeMillis() + (2 * 60 * 1000); // 2 minutes validity
            showOtpArea(true);
            startOtpCountdown(2 * 60 * 1000L);
            CustomAlert.showSuccess(this, "OTP Sent", "OTP sent successfully. Check popup.");
            sendOtpNotification(currentOtp);
            showOtpDialog(currentOtp);
            otpVerified = false;
            setLoginButtonEnabled(false);
        });
        verifyOtpButton.setOnClickListener(v -> {
            String entered = otpInput.getText().toString().trim();
            if (entered.isEmpty()) {
                CustomAlert.showWarning(this, "Input Required", "Please enter OTP");
                return;
            }
            if (currentOtp == null) {
                CustomAlert.showWarning(this, "Action Required", "No OTP requested yet. Click Send OTP.");
                return;
            }
            if (System.currentTimeMillis() > otpExpiryMillis) {
                CustomAlert.showError(this, "Expired", "OTP expired. Please resend.");
                return;
            }
            if (entered.equals(currentOtp)) {
                CustomAlert.showSuccess(this, "Verified", "OTP Verified. You can now Login.");
                otpVerified = true;
                setLoginButtonEnabled(true);
            } else {
                CustomAlert.showError(this, "Failed", "Invalid OTP. Please try again.");
            }
        });
        resendOtpButton.setOnClickListener(v -> {
            if (otpTimer != null)
                otpTimer.cancel();
            currentOtp = generateOtp(6);
            otpExpiryMillis = System.currentTimeMillis() + (2 * 60 * 1000);
            startOtpCountdown(2 * 60 * 1000L);
            CustomAlert.showInfo(this, "Resent", "OTP has been resent.");
            sendOtpNotification(currentOtp);
            showOtpDialog(currentOtp);
            otpVerified = false;
            setLoginButtonEnabled(false);
        });
        loginButton.setOnClickListener(v -> {
            if (!otpVerified) {
                CustomAlert.showWarning(this, "Verification Pending",
                        "Please verify OTP first (use Send OTP → Verify).");
                return;
            }
            String aadhaar = aadhaarInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();
            JSONObject user = getUserDetails(aadhaar, dob);
            if (user != null) {
                UserUtils.saveUserSession(LoginActivity.this, user.optString("aadhaar_id"));
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("aadhaar_id", user.optString("aadhaar_id"));
                intent.putExtra("dob", user.optString("dob"));
                intent.putExtra("name", user.optString("name"));
                intent.putExtra("email", user.optString("email"));
                intent.putExtra("mobile", user.optString("mobile"));
                intent.putExtra("photo", user.optString("photo"));
                intent.putExtra("address", user.optString("address"));
                intent.putExtra("city", user.optString("city"));
                intent.putExtra("pincode", user.optString("pincode"));
                intent.putExtra("eligible", user.optBoolean("eligible"));
                startActivity(intent);
                finish();
            } else {
                CustomAlert.showError(this, "Error", "User details not found. Please check Aadhaar/DOB.");
            }
        });
    }
    private void setLoginButtonEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        loginButton.setAlpha(enabled ? 1f : 0.5f);
    }
    private void showOtpDialog(String otp) {
        if (otp == null)
            return;
        String title = "Demo OTP";
        String message = "Your OTP is:\n\n" + otp + "\n\nValid for 2 minutes.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("Copy & Autofill", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OTP", otp);
            if (clipboard != null)
                clipboard.setPrimaryClip(clip);
            otpInput.setText(otp);
            otpInput.setSelection(otp.length());
            Toast.makeText(this, "OTP autofilled", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Resend", (dialog, which) -> {
            resendOtpButton.performClick();
        });
        builder.create().show();
    }
    private String generateOtp(int length) {
        Random rnd = new Random();
        int bound = (int) Math.pow(10, length);
        int number = rnd.nextInt(bound - (bound / 10)) + (bound / 10);
        return String.format(Locale.getDefault(), "%0" + length + "d", number);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "OTP Channel";
            String description = "Channel for OTP notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendOtpNotification(String otp) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT);
        String title = "Your SmartVoting OTP";
        String text = "Your OTP is: " + otp + " (valid 2 minutes)";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(OTP_NOTIFICATION_ID, builder.build());
    }
    private void showOtpArea(boolean show) {
        if (show) {
            otpArea.setVisibility(View.VISIBLE);
            otpTimerText.setVisibility(View.VISIBLE);
            otpInput.requestFocus();
        } else {
            otpArea.setVisibility(View.GONE);
            otpTimerText.setVisibility(View.GONE);
            if (otpTimer != null)
                otpTimer.cancel();
            currentOtp = null;
        }
    }
    private void startOtpCountdown(long millisInFuture) {
        otpTimerText.setVisibility(View.VISIBLE);
        otpTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                otpTimerText.setText(
                        String.format(Locale.getDefault(),
                                "Time left: %02d:%02d",
                                sec / 60, sec % 60));
            }
            public void onFinish() {
                otpTimerText.setText("OTP expired");
                Toast.makeText(LoginActivity.this, "OTP expired", Toast.LENGTH_SHORT).show();
                currentOtp = null;
                otpVerified = false;
                setLoginButtonEnabled(false);
            }
        }.start();
    }
    private boolean validateUser(String aadhaar, String dob) {
        try {
            InputStream is = getAssets().open("aadhaar_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (aadhaar.equals(obj.getString("aadhaar_id"))
                        && dob.equals(obj.getString("dob"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private JSONObject getUserDetails(String aadhaar, String dob) {
        try {
            InputStream is = getAssets().open("aadhaar_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (aadhaar.equals(obj.getString("aadhaar_id"))
                        && dob.equals(obj.getString("dob"))) {
                    return obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null)
            otpTimer.cancel();
    }
}
```
---

## MainActivity.java
```java
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ImageView searchButton, notificationIcon;
    private TextView dashboardTitle;
    private boolean doubleBackToExitPressedOnce = false;
    public static String aadhaarId, dob, name, email, mobile, photo, address, city, pincode;
    public static boolean eligible;
    private android.widget.EditText etSearch;
    private android.widget.TextView tvNotificationBadge;
    private NotificationHelper notificationHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        notificationIcon = findViewById(R.id.icon_notification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        dashboardTitle = findViewById(R.id.dashboard_title);
        etSearch = findViewById(R.id.etSearch);
        notificationHelper = new NotificationHelper(this);
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
        loadFragment(new HomeFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
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
                etSearch.setText("");
            }
            return true;
        });
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
        notificationIcon.setOnClickListener(v -> {
            Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        });
        updateNotificationBadge();
        handleNavigationIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
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
        updateNotificationBadge();
    }
    private void updateNotificationBadge() {
        int count = notificationHelper.getUnreadCount();
        if (count > 0) {
            tvNotificationBadge.setVisibility(android.view.View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(count));
        } else {
            tvNotificationBadge.setVisibility(android.view.View.GONE);
        }
    }
    private void performSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearch(query);
        }
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
```
---

## News.java
```java
public class News {
    private String id;
    private String title;
    private String description;
    private String date;
    private long timestamp;
    private String imageUrl;
    public News(String id, String title, String description, String date, long timestamp, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getDate() {
        return date;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}
```
---

## NewsManager.java
```java
public class NewsManager {
    private static final String FILE_NAME = "news.json";
    private Context context;
    public NewsManager(Context context) {
        this.context = context;
    }
    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return newsList;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                newsList.add(new News(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("date"),
                        obj.getLong("timestamp"),
                        obj.optString("imageUrl", "")));
            }
        } catch (Exception e) {
            Log.e("NewsManager", "Error reading news", e);
        }
        return newsList;
    }
    public void addNews(News news) {
        List<News> list = getAllNews();
        list.add(0, news); // Add to top
        saveNews(list);
    }
    public void updateNews(News news) {
        List<News> list = getAllNews();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(news.getId())) {
                list.set(i, news);
                break;
            }
        }
        saveNews(list);
    }
    public void deleteNews(String id) {
        List<News> list = getAllNews();
        list.removeIf(n -> n.getId().equals(id));
        saveNews(list);
    }
    private void saveNews(List<News> list) {
        JSONArray array = new JSONArray();
        for (News n : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", n.getId());
                obj.put("title", n.getTitle());
                obj.put("description", n.getDescription());
                obj.put("date", n.getDate());
                obj.put("timestamp", n.getTimestamp());
                obj.put("imageUrl", n.getImageUrl());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("NewsManager", "Error saving news", e);
        }
    }
}
```
---

## NotificationActivity.java
```java
public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationContainer;
    private LinearLayout layoutEmptyState;
    private NotificationHelper notificationHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        notificationContainer = findViewById(R.id.notificationContainer);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        ImageView btnBack = findViewById(R.id.btnBack);
        notificationHelper = new NotificationHelper(this);
        btnBack.setOnClickListener(v -> finish());
        loadNotifications();
        notificationHelper.markAllAsRead();
    }
    private void loadNotifications() {
        notificationContainer.removeAllViews();
        List<NotificationItem> notifications = notificationHelper.getAllNotifications();
        if (notifications.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            for (NotificationItem item : notifications) {
                addNotificationCard(item);
            }
        }
    }
    private void addNotificationCard(NotificationItem item) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_notification_card, notificationContainer,
                false);
        TextView tvTitle = cardView.findViewById(R.id.tvNotificationTitle);
        TextView tvMessage = cardView.findViewById(R.id.tvNotificationMessage);
        TextView tvDate = cardView.findViewById(R.id.tvNotificationDate);
        TextView tvSender = cardView.findViewById(R.id.tvNotificationSender);
        tvTitle.setText(item.getTitle());
        tvMessage.setText(item.getMessage());
        String sender = "System";
        if (item.getType() == NotificationItem.TYPE_NEWS)
            sender = "Admin";
        else if (item.getType() == NotificationItem.TYPE_ELECTION)
            sender = "Election Commission";
        else if (item.getType() == NotificationItem.TYPE_FEEDBACK)
            sender = "Support Team";
        tvSender.setText("From: " + sender);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(item.getTimestamp())));
        cardView.setOnClickListener(v -> handleNotificationClick(item));
        notificationContainer.addView(cardView);
    }
    private void handleNotificationClick(NotificationItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (item.getType() == NotificationItem.TYPE_NEWS) {
            intent.putExtra("navigate_to", "home");
        } else if (item.getType() == NotificationItem.TYPE_ELECTION) {
            intent.putExtra("navigate_to", "vote");
        } else if (item.getType() == NotificationItem.TYPE_FEEDBACK) {
            intent.putExtra("navigate_to", "account");
        }
        startActivity(intent);
        finish();
    }
}
```
---

## NotificationHelper.java
```java
public class NotificationHelper {
    private static final String PREF_NAME = "SmartVotingNotifications";
    private static final String KEY_LAST_CHECK = "last_check_timestamp";
    private Context context;
    private SharedPreferences prefs;
    private NewsManager newsManager;
    private ElectionManager electionManager;
    private FeedbackManager feedbackManager;
    public NotificationHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.newsManager = new NewsManager(context);
        this.electionManager = new ElectionManager(context);
        this.feedbackManager = new FeedbackManager(context);
    }
    public long getLastCheckTimestamp() {
        return prefs.getLong(KEY_LAST_CHECK, 0);
    }
    public void markAllAsRead() {
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
    }
    public List<NotificationItem> getAllNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        long lastCheck = getLastCheckTimestamp();
        List<News> allNews = newsManager.getAllNews();
        for (News news : allNews) {
            notifications.add(new NotificationItem(
                    NotificationItem.TYPE_NEWS,
                    "New Announcement: " + news.getTitle(),
                    news.getDescription(),
                    news.getTimestamp(),
                    news.getId()));
        }
        List<Election> elections = electionManager.getAllElections();
        for (Election election : elections) {
            if ("Active".equalsIgnoreCase(election.getStatus())) {
                notifications.add(new NotificationItem(
                        NotificationItem.TYPE_ELECTION,
                        "Election Live: " + election.getTitle(),
                        "Voting is now open for " + election.getState(),
                        System.currentTimeMillis(), // Placeholder as we don't have creation time
                        String.valueOf(election.getId())));
            }
        }
        User user = UserUtils.getCurrentUser(context);
        if (user != null) {
            List<Feedback> feedbackList = feedbackManager.getFeedbackByUserId(user.getAadhaarId());
            for (Feedback feedback : feedbackList) {
                if ("resolved".equalsIgnoreCase(feedback.getStatus())) {
                    notifications.add(new NotificationItem(
                            NotificationItem.TYPE_FEEDBACK,
                            "Feedback Resolved",
                            "Admin responded: " + feedback.getAdminResponse(),
                            feedback.getResolvedTimestamp(),
                            feedback.getId()));
                }
            }
        }
        Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
        return notifications;
    }
    public int getUnreadCount() {
        long lastCheck = getLastCheckTimestamp();
        List<NotificationItem> all = getAllNotifications();
        int count = 0;
        for (NotificationItem item : all) {
            if (item.getTimestamp() > lastCheck) {
                count++;
            }
        }
        return count;
    }
}
```
---

## NotificationItem.java
```java
public class NotificationItem {
    public static final int TYPE_NEWS = 1;
    public static final int TYPE_ELECTION = 2;
    public static final int TYPE_FEEDBACK = 3;
    private int type;
    private String title;
    private String message;
    private long timestamp;
    private String referenceId; // ID to navigate to (e.g., news ID, election ID)
    public NotificationItem(int type, String title, String message, long timestamp, String referenceId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.referenceId = referenceId;
    }
    public int getType() {
        return type;
    }
    public String getTitle() {
        return title;
    }
    public String getMessage() {
        return message;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getReferenceId() {
        return referenceId;
    }
}
```
---

## Party.java
```java
public class Party {
    private String id;
    private String name;
    private String symbol;
    private String description;
    private String logoPath;
    public Party(String id, String name, String symbol, String description, String logoPath) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.description = description;
        this.logoPath = logoPath;
    }
    public Party(String id, String name, String symbol, String description) {
        this(id, name, symbol, description, null);
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getSymbol() {
        return symbol;
    }
    public String getDescription() {
        return description;
    }
    public String getLogoPath() {
        return logoPath;
    }
}
```
---

## PartyDetailsActivity.java
```java
public class PartyDetailsActivity extends AppCompatActivity {
    private ImageView imgPartyLogo;
    private TextView tvPartyName, tvLeader, tvNominees, tvHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_details);
        imgPartyLogo = findViewById(R.id.imgPartyLogo);
        tvPartyName = findViewById(R.id.tvPartyName);
        tvLeader = findViewById(R.id.tvLeader);
        tvNominees = findViewById(R.id.tvNominees);
        tvHistory = findViewById(R.id.tvHistory);
        String partyId = getIntent().getStringExtra("party_id");
        if (partyId != null) {
            loadPartyDetails(partyId);
        }
    }
    private void loadPartyDetails(String partyId) {
        switch (partyId) {
            case "BJP":
                imgPartyLogo.setImageResource(R.drawable.ic_bjp);
                tvPartyName.setText("Bharatiya Janata Party (BJP)");
                tvLeader.setText("Leader: Narendra Modi");
                tvNominees.setText("Key Nominees: Amit Shah, Rajnath Singh, Nirmala Sitharaman");
                tvHistory.setText("The Bharatiya Janata Party (BJP) was founded in 1980. It emerged from the Bharatiya Jana Sangh and has since become one of India's two major national parties. It advocates for nationalism and development-focused governance.");
                break;
            case "INC":
                imgPartyLogo.setImageResource(R.drawable.ic_inc);
                tvPartyName.setText("Indian National Congress (INC)");
                tvLeader.setText("Leader: Mallikarjun Kharge");
                tvNominees.setText("Key Nominees: Rahul Gandhi, Priyanka Gandhi Vadra, Sonia Gandhi");
                tvHistory.setText("The Indian National Congress (INC), founded in 1885, played a pivotal role in India's independence movement. It is one of the oldest political parties and advocates secularism, democracy, and social justice.");
                break;
            case "AAP":
                imgPartyLogo.setImageResource(R.drawable.ic_aap);
                tvPartyName.setText("Aam Aadmi Party (AAP)");
                tvLeader.setText("Leader: Arvind Kejriwal");
                tvNominees.setText("Key Nominees: Manish Sisodia, Atishi Marlena, Saurabh Bharadwaj");
                tvHistory.setText("The Aam Aadmi Party (AAP) was founded in 2012 by Arvind Kejriwal after the India Against Corruption movement. It promotes transparency, anti-corruption measures, and pro-people governance.");
                break;
            case "BSP":
                imgPartyLogo.setImageResource(R.drawable.ic_bsp);
                tvPartyName.setText("Bahujan Samaj Party (BSP)");
                tvLeader.setText("Leader: Mayawati");
                tvNominees.setText("Key Nominees: Satish Mishra, Danish Ali, Uma Shankar");
                tvHistory.setText("The Bahujan Samaj Party (BSP) was formed in 1984 by Kanshi Ram. It represents the Bahujan community including Scheduled Castes, Scheduled Tribes, and Other Backward Classes.");
                break;
            case "CPI":
                imgPartyLogo.setImageResource(R.drawable.ic_cpi);
                tvPartyName.setText("Communist Party of India (CPI)");
                tvLeader.setText("Leader: D. Raja");
                tvNominees.setText("Key Nominees: Binoy Viswam, Annie Raja, Kanhaiya Kumar");
                tvHistory.setText("The Communist Party of India (CPI) was founded in 1925. It advocates Marxist–Leninist ideology, working for labor rights, equality, and socialism.");
                break;
            case "DMK":
                imgPartyLogo.setImageResource(R.drawable.ic_dmk);
                tvPartyName.setText("Dravida Munnetra Kazhagam (DMK)");
                tvLeader.setText("Leader: M. K. Stalin");
                tvNominees.setText("Key Nominees: Kanimozhi Karunanidhi, T. R. Baalu, A. Raja");
                tvHistory.setText("The Dravida Munnetra Kazhagam (DMK) was founded in 1949 in Tamil Nadu. It promotes Dravidian ideals, social justice, and Tamil cultural pride.");
                break;
            case "TMC":
                imgPartyLogo.setImageResource(R.drawable.ic_tmc);
                tvPartyName.setText("All India Trinamool Congress (TMC)");
                tvLeader.setText("Leader: Mamata Banerjee");
                tvNominees.setText("Key Nominees: Abhishek Banerjee, Derek O’Brien, Sukhendu Sekhar Roy");
                tvHistory.setText("The All India Trinamool Congress (TMC) was formed in 1998 by Mamata Banerjee after splitting from the INC. It has a strong base in West Bengal and advocates secularism and grassroots democracy.");
                break;
            case "NCP":
                imgPartyLogo.setImageResource(R.drawable.ic_ncp);
                tvPartyName.setText("Nationalist Congress Party (NCP)");
                tvLeader.setText("Leader: Sharad Pawar");
                tvNominees.setText("Key Nominees: Ajit Pawar, Supriya Sule, Praful Patel");
                tvHistory.setText("The Nationalist Congress Party (NCP) was formed in 1999 by Sharad Pawar, P. A. Sangma, and Tariq Anwar. It promotes secularism, democracy, and federalism.");
                break;
            default:
                tvPartyName.setText("Unknown Party");
                tvLeader.setText("");
                tvNominees.setText("");
                tvHistory.setText("Party details not found.");
                break;
        }
    }
}
```
---

## PartyManager.java
```java
public class PartyManager {
    private static final String FILE_NAME = "parties.json";
    private Context context;
    public PartyManager(Context context) {
        this.context = context;
        seedDefaultParties();
    }
    private void seedDefaultParties() {
        List<Party> existingParties = getAllParties();
        boolean changed = false;
        List<Party> defaults = new ArrayList<>();
        defaults.add(new Party("1", "Bharatiya Janata Party (BJP)", "Lotus",
                "The Bharatiya Janata Party is one of two major political parties in India.", "res:ic_bjp"));
        defaults.add(new Party("2", "Indian National Congress (INC)", "Hand",
                "The Indian National Congress is a political party in India with widespread roots.", "res:img_inc"));
        defaults.add(new Party("3", "Aam Aadmi Party (AAP)", "Broom",
                "The Aam Aadmi Party is a political party in India that was founded in November 2012.", "res:ic_aap"));
        defaults.add(new Party("4", "Trinamool Congress (TMC)", "Flowers & Grass",
                "The All India Trinamool Congress is an Indian political party which is predominantly active in West Bengal.",
                "res:ic_tmc"));
        defaults.add(new Party("5", "Dravida Munnetra Kazhagam (DMK)", "Rising Sun",
                "Dravida Munnetra Kazhagam is a political party in India, particularly in the state of Tamil Nadu and Puducherry.",
                "res:ic_dmk"));
        defaults.add(new Party("6", "AIADMK", "Two Leaves",
                "All India Anna Dravida Munnetra Kazhagam is an Indian regional political party with great influence in the state of Tamil Nadu.",
                "res:ic_aiadmk"));
        defaults.add(new Party("7", "Samajwadi Party (SP)", "Bicycle",
                "The Samajwadi Party is a socialist political party in India, headquartered in New Delhi.",
                "res:ic_sp"));
        defaults.add(new Party("8", "Bahujan Samaj Party (BSP)", "Elephant",
                "The Bahujan Samaj Party is a national level political party in India that was formed to represent the Bahujans.",
                "res:ic_bsp"));
        for (Party def : defaults) {
            boolean exists = false;
            for (Party p : existingParties) {
                if (p.getName().equalsIgnoreCase(def.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                Party partyToAdd = def;
                final String defId = def.getId();
                boolean idExists = existingParties.stream().anyMatch(p -> p.getId().equals(defId));
                if (idExists) {
                    partyToAdd = new Party(java.util.UUID.randomUUID().toString(), def.getName(), def.getSymbol(),
                            def.getDescription(), def.getLogoPath());
                }
                existingParties.add(partyToAdd);
                changed = true;
            }
        }
        if (changed) {
            saveParties(existingParties);
        }
    }
    public List<Party> getAllParties() {
        List<Party> parties = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return parties;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                parties.add(new Party(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.optString("symbol", ""),
                        obj.optString("description", ""),
                        obj.optString("logoPath", null)));
            }
        } catch (Exception e) {
            Log.e("PartyManager", "Error reading parties", e);
        }
        return parties;
    }
    public void addParty(Party party) {
        List<Party> parties = getAllParties();
        parties.add(party);
        saveParties(parties);
    }
    public void updateParty(Party party) {
        List<Party> parties = getAllParties();
        for (int i = 0; i < parties.size(); i++) {
            if (parties.get(i).getId().equals(party.getId())) {
                parties.set(i, party);
                break;
            }
        }
        saveParties(parties);
    }
    public void deleteParty(String id) {
        List<Party> parties = getAllParties();
        parties.removeIf(p -> p.getId().equals(id));
        saveParties(parties);
    }
    private void saveParties(List<Party> parties) {
        JSONArray array = new JSONArray();
        for (Party p : parties) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", p.getId());
                obj.put("name", p.getName());
                obj.put("symbol", p.getSymbol());
                obj.put("description", p.getDescription());
                if (p.getLogoPath() != null) {
                    obj.put("logoPath", p.getLogoPath());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("PartyManager", "Error saving parties", e);
        }
    }
}
```
---

## SearchableFragment.java
```java
public interface SearchableFragment {
    void onSearch(String query);
}
```
---

## SplashActivity.java
```java
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView logo = findViewById(R.id.splash_logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
        logo.startAnimation(fadeIn);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
```
---

## TabAdapter.java
```java
public class TabAdapter extends FragmentStateAdapter {
    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ListFragment();  // updated: List fragment
            case 2:
                return new VoteFragment();  // updated: Vote is center-highlighted
            case 3:
                return new HistoryFragment();
            case 4:
                return new AccountFragment();  // updated: Account section
            default:
                return new HomeFragment();
        }
    }
    @Override
    public int getItemCount() {
        return 5;  // updated: Only Home, List, Vote, History, Account
    }
}
```
---

## User.java
```java
public class User {
    private String aadhaarId;
    private String name;
    private String dob;
    private String email;
    private String mobile;
    private String photo;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private boolean eligible;
    public User(String aadhaarId, String name, String dob, String email,
                String mobile, String photo, String address,
                String city, String state, String pincode, boolean eligible) {
        this.aadhaarId = aadhaarId;
        this.name = name;
        this.dob = dob;
        this.email = email;
        this.mobile = mobile;
        this.photo = photo;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.eligible = eligible;
    }
    public String getAadhaarId() { return aadhaarId; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getPhoto() { return photo; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public boolean isEligible() { return eligible; }
}
```
---

## UserManager.java
```java
public class UserManager {
    private static final String FILE_NAME = "aadhaar_data.json";
    private Context context;
    public UserManager(Context context) {
        this.context = context;
        ensureFileExists();
    }
    private void ensureFileExists() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try {
                InputStream is = context.getAssets().open(FILE_NAME);
                Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
                String json = scanner.useDelimiter("\\A").next();
                scanner.close();
                saveJsonToFile(json);
            } catch (IOException e) {
                Log.e("UserManager", "Error copying asset to internal storage", e);
            }
        }
    }
    private void saveJsonToFile(String json) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            Log.e("UserManager", "Error saving JSON to file", e);
        }
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                users.add(new User(
                        obj.getString("aadhaar_id"),
                        obj.getString("name"),
                        obj.getString("dob"),
                        obj.optString("email", ""),
                        obj.optString("mobile", ""),
                        obj.optString("photo", ""),
                        obj.optString("address", ""),
                        obj.optString("city", ""),
                        obj.optString("state", ""),
                        obj.optString("pincode", ""),
                        obj.optBoolean("eligible", true)));
            }
        } catch (Exception e) {
            Log.e("UserManager", "Error reading users", e);
        }
        return users;
    }
    public void updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        JSONArray array = new JSONArray();
        for (User u : users) {
            JSONObject obj = new JSONObject();
            try {
                if (u.getAadhaarId().equals(updatedUser.getAadhaarId())) {
                    obj.put("aadhaar_id", updatedUser.getAadhaarId());
                    obj.put("name", updatedUser.getName());
                    obj.put("dob", updatedUser.getDob());
                    obj.put("email", updatedUser.getEmail());
                    obj.put("mobile", updatedUser.getMobile());
                    obj.put("photo", updatedUser.getPhoto());
                    obj.put("address", updatedUser.getAddress());
                    obj.put("city", updatedUser.getCity());
                    obj.put("state", updatedUser.getState());
                    obj.put("pincode", updatedUser.getPincode());
                    obj.put("eligible", updatedUser.isEligible());
                } else {
                    obj.put("aadhaar_id", u.getAadhaarId());
                    obj.put("name", u.getName());
                    obj.put("dob", u.getDob());
                    obj.put("email", u.getEmail());
                    obj.put("mobile", u.getMobile());
                    obj.put("photo", u.getPhoto());
                    obj.put("address", u.getAddress());
                    obj.put("city", u.getCity());
                    obj.put("state", u.getState());
                    obj.put("pincode", u.getPincode());
                    obj.put("eligible", u.isEligible());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        saveJsonToFile(array.toString());
    }
    public void addUser(User newUser) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getAadhaarId().equals(newUser.getAadhaarId())) {
                Log.w("UserManager", "User with Aadhaar ID " + newUser.getAadhaarId() + " already exists");
                return;
            }
        }
        users.add(newUser);
        JSONArray array = new JSONArray();
        for (User u : users) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("aadhaar_id", u.getAadhaarId());
                obj.put("name", u.getName());
                obj.put("dob", u.getDob());
                obj.put("email", u.getEmail());
                obj.put("mobile", u.getMobile());
                obj.put("photo", u.getPhoto());
                obj.put("address", u.getAddress());
                obj.put("city", u.getCity());
                obj.put("state", u.getState());
                obj.put("pincode", u.getPincode());
                obj.put("eligible", u.isEligible());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        saveJsonToFile(array.toString());
    }
}
```
---

## UserUtils.java
```java
public class UserUtils {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_AADHAAR = "aadhaar_id";
    public static void saveUserSession(Context context, String aadhaarId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AADHAAR, aadhaarId)
                .apply();
    }
    public static void clearUserSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
    public static User getCurrentUser(Context context) {
        try {
            String storedAadhaar = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_AADHAAR, null);
            if (storedAadhaar == null)
                return null;
            InputStream is = context.getAssets().open("aadhaar_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.getString("aadhaar_id").equals(storedAadhaar)) {
                    return new User(
                            obj.getString("aadhaar_id"),
                            obj.getString("name"),
                            obj.getString("dob"),
                            obj.getString("email"),
                            obj.getString("mobile"),
                            obj.getString("photo"),
                            obj.getString("address"),
                            obj.getString("city"),
                            obj.getString("state"),
                            obj.getString("pincode"),
                            obj.getBoolean("eligible"));
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static int calculateAge(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date birthDate = sdf.parse(dob);
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
```
---

## VoteFragment.java
```java
public class VoteFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Election> electionList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);
        recyclerView = view.findViewById(R.id.electionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ElectionManager electionManager = new ElectionManager(getContext());
        electionList = electionManager.getAllElections();
        ElectionAdapter adapter = new ElectionAdapter(electionList, this::checkEligibility);
        recyclerView.setAdapter(adapter);
        return view;
    }
    private void checkEligibility(Election election) {
        User user = UserUtils.getCurrentUser(getContext());
        if (user == null) {
            CustomAlert.showError(getContext(), "Error", "User data not found!");
            return;
        }
        int age = UserUtils.calculateAge(user.getDob());
        String status = election.getStatus().toLowerCase();
        if (!status.equals("running") && !status.equals("active") && !status.equals("open")) {
            CustomAlert.showWarning(getContext(), "Election Closed",
                    "This election is currently " + election.getStatus());
            return;
        }
        if (age < election.getMinAge()) {
            CustomAlert.showError(getContext(), "Not Eligible",
                    "You must be at least " + election.getMinAge() + " years old to vote in this election.");
            return;
        }
        String userState = user.getState().trim();
        String electionState = election.getState().trim();
        android.util.Log.d("VoteFragment",
                "Checking eligibility: User State='" + userState + "', Election State='" + electionState + "'");
        if (!userState.equalsIgnoreCase(electionState)) {
            CustomAlert.showError(getContext(), "Not Eligible",
                    "This election is for residents of " + electionState + " only.\nYour registered state is "
                            + userState + ".");
            return;
        }
        Intent intent = new Intent(getContext(), VotingActivity.class);
        intent.putExtra("election_id", election.getId());
        intent.putExtra("user_name", user.getName());
        startActivity(intent);
    }
}
```
---

## VoteManager.java
```java
public class VoteManager {
    private static final String FILE_NAME = "votes.json";
    private Context context;
    public VoteManager(Context context) {
        this.context = context;
    }
    public boolean hasUserVoted(String aadhaarId, int electionId) {
        List<VoteRecord> votes = getAllVotes();
        for (VoteRecord vote : votes) {
            if (vote.getAadhaarId().equals(aadhaarId) && vote.getElectionId() == electionId) {
                return true;
            }
        }
        return false;
    }
    public void recordVote(VoteRecord vote) {
        List<VoteRecord> votes = getAllVotes();
        votes.add(vote);
        saveVotes(votes);
    }
    public List<VoteRecord> getVotesByElection(int electionId) {
        List<VoteRecord> result = new ArrayList<>();
        List<VoteRecord> allVotes = getAllVotes();
        for (VoteRecord vote : allVotes) {
            if (vote.getElectionId() == electionId) {
                result.add(vote);
            }
        }
        return result;
    }
    public Map<String, Integer> getVoteCountsByElection(int electionId) {
        Map<String, Integer> counts = new HashMap<>();
        List<VoteRecord> votes = getVotesByElection(electionId);
        for (VoteRecord vote : votes) {
            String optionId = vote.getOptionId();
            counts.put(optionId, counts.getOrDefault(optionId, 0) + 1);
        }
        return counts;
    }
    public List<VoteRecord> getAllVotes() {
        List<VoteRecord> votes = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return votes;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                votes.add(new VoteRecord(
                        obj.getString("aadhaarId"),
                        obj.getInt("electionId"),
                        obj.getString("optionId"),
                        obj.getLong("timestamp")));
            }
        } catch (Exception e) {
            Log.e("VoteManager", "Error reading votes", e);
        }
        return votes;
    }
    private void saveVotes(List<VoteRecord> votes) {
        JSONArray array = new JSONArray();
        for (VoteRecord vote : votes) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("aadhaarId", vote.getAadhaarId());
                obj.put("electionId", vote.getElectionId());
                obj.put("optionId", vote.getOptionId());
                obj.put("timestamp", vote.getTimestamp());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("VoteManager", "Error saving votes", e);
        }
    }
}
```
---

## VoteRecord.java
```java
public class VoteRecord {
    private String aadhaarId;
    private int electionId;
    private String optionId;
    private long timestamp;
    public VoteRecord(String aadhaarId, int electionId, String optionId, long timestamp) {
        this.aadhaarId = aadhaarId;
        this.electionId = electionId;
        this.optionId = optionId;
        this.timestamp = timestamp;
    }
    public String getAadhaarId() {
        return aadhaarId;
    }
    public int getElectionId() {
        return electionId;
    }
    public String getOptionId() {
        return optionId;
    }
    public long getTimestamp() {
        return timestamp;
    }
}
```
---

## VotingActivity.java
```java
public class VotingActivity extends AppCompatActivity {
    private TextView electionInfo, userInfo, tvSelectedOption;
    private Button voteButton;
    private RecyclerView rvVotingOptions;
    private LinearLayout bottomBar;
    private int electionId;
    private String aadhaarId;
    private VotingOptionAdapter adapter;
    private String selectedOptionId = null;
    private String selectedOptionName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);
        electionInfo = findViewById(R.id.textViewElectionId); // Note: ID might be missing in new layout, check XML
        userInfo = findViewById(R.id.textViewUserInfo);
        voteButton = findViewById(R.id.voteButton);
        rvVotingOptions = findViewById(R.id.rvVotingOptions);
        bottomBar = findViewById(R.id.bottomBar);
        tvSelectedOption = findViewById(R.id.tvSelectedOption);
        electionId = getIntent().getIntExtra("election_id", -1);
        String userName = getIntent().getStringExtra("user_name");
        aadhaarId = MainActivity.aadhaarId;
        if (electionInfo != null)
            electionInfo.setText("Election ID: " + electionId);
        userInfo.setText("Welcome " + userName + ", you are eligible to vote!");
        rvVotingOptions.setLayoutManager(new LinearLayoutManager(this));
        VoteManager voteManager = new VoteManager(this);
        if (voteManager.hasUserVoted(aadhaarId, electionId)) {
            userInfo.setText("You have already voted in this election!");
            userInfo.setTextColor(android.graphics.Color.RED);
            rvVotingOptions.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            return;
        }
        loadVotingOptions();
        voteButton.setOnClickListener(v -> {
            if (selectedOptionId == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }
            VoteRecord vote = new VoteRecord(aadhaarId, electionId, selectedOptionId, System.currentTimeMillis());
            voteManager.recordVote(vote);
            Toast.makeText(VotingActivity.this,
                    "✅ Vote recorded for " + selectedOptionName,
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }
    private void loadVotingOptions() {
        VotingOptionManager optionManager = new VotingOptionManager(this);
        List<VotingOption> options = optionManager.getOptionsByElection(electionId);
        if (options.isEmpty()) {
            userInfo.setText("No voting options available for this election.");
            return;
        }
        adapter = new VotingOptionAdapter(options);
        rvVotingOptions.setAdapter(adapter);
    }
    private class VotingOptionAdapter extends RecyclerView.Adapter<VotingOptionAdapter.ViewHolder> {
        private List<VotingOption> options;
        private int selectedPosition = -1;
        public VotingOptionAdapter(List<VotingOption> options) {
            this.options = options;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_voting_option_card, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VotingOption option = options.get(position);
            holder.tvCandidateName.setText(option.getOptionName());
            holder.tvPartyName.setText(option.getDescription());
            if (option.getLogoPath() != null) {
                try {
                    if (option.getLogoPath().startsWith("res:")) {
                        String resName = option.getLogoPath().substring(4);
                        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                        if (resId != 0) {
                            holder.imgPartyLogo.setImageResource(resId);
                        }
                    } else {
                        java.io.File imgFile = new java.io.File(getFilesDir(), option.getLogoPath());
                        if (imgFile.exists()) {
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                                    .decodeFile(imgFile.getAbsolutePath());
                            holder.imgPartyLogo.setImageBitmap(bitmap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.imgPartyLogo.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            boolean isSelected = selectedPosition == position;
            holder.rbSelect.setChecked(isSelected);
            if (isSelected) {
                holder.cardView.setStrokeColor(0xFF1E3A8A); // Dark Blue
                holder.cardView.setStrokeWidth(4);
                holder.cardView.setCardElevation(8);
            } else {
                holder.cardView.setStrokeColor(0xFFE5E7EB); // Gray
                holder.cardView.setStrokeWidth(2);
                holder.cardView.setCardElevation(4);
            }
            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                selectedOptionId = option.getId();
                selectedOptionName = option.getOptionName();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                bottomBar.setVisibility(View.VISIBLE);
                tvSelectedOption.setText("Selected: " + selectedOptionName);
            });
        }
        @Override
        public int getItemCount() {
            return options.size();
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            ImageView imgPartyLogo;
            TextView tvCandidateName, tvPartyName;
            RadioButton rbSelect;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardVotingOption);
                imgPartyLogo = itemView.findViewById(R.id.imgPartyLogo);
                tvCandidateName = itemView.findViewById(R.id.tvCandidateName);
                tvPartyName = itemView.findViewById(R.id.tvPartyName);
                rbSelect = itemView.findViewById(R.id.rbSelect);
            }
        }
    }
}
```
---

## VotingOption.java
```java
public class VotingOption {
    private String id;
    private int electionId;
    private String optionName;
    private String description;
    private String logoPath;
    public VotingOption(String id, int electionId, String optionName, String description) {
        this(id, electionId, optionName, description, null);
    }
    public VotingOption(String id, int electionId, String optionName, String description, String logoPath) {
        this.id = id;
        this.electionId = electionId;
        this.optionName = optionName;
        this.description = description;
        this.logoPath = logoPath;
    }
    public String getId() {
        return id;
    }
    public int getElectionId() {
        return electionId;
    }
    public String getOptionName() {
        return optionName;
    }
    public String getDescription() {
        return description;
    }
    public String getLogoPath() {
        return logoPath;
    }
}
```
---

## VotingOptionManager.java
```java
public class VotingOptionManager {
    private static final String FILE_NAME = "voting_options.json";
    private Context context;
    public VotingOptionManager(Context context) {
        this.context = context;
    }
    public List<VotingOption> getOptionsByElection(int electionId) {
        List<VotingOption> options = new ArrayList<>();
        List<VotingOption> allOptions = getAllOptions();
        for (VotingOption option : allOptions) {
            if (option.getElectionId() == electionId) {
                options.add(option);
            }
        }
        return options;
    }
    public List<VotingOption> getAllOptions() {
        List<VotingOption> options = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return options;
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                options.add(new VotingOption(
                        obj.getString("id"),
                        obj.getInt("electionId"),
                        obj.getString("optionName"),
                        obj.optString("description", ""),
                        obj.optString("logoPath", null)));
            }
        } catch (Exception e) {
            Log.e("VotingOptionManager", "Error reading options", e);
        }
        return options;
    }
    public void addOption(VotingOption option) {
        List<VotingOption> options = getAllOptions();
        options.add(option);
        saveOptions(options);
    }
    public void updateOption(VotingOption option) {
        List<VotingOption> options = getAllOptions();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId().equals(option.getId())) {
                options.set(i, option);
                break;
            }
        }
        saveOptions(options);
    }
    public void deleteOption(String optionId) {
        List<VotingOption> options = getAllOptions();
        options.removeIf(o -> o.getId().equals(optionId));
        saveOptions(options);
    }
    private void saveOptions(List<VotingOption> options) {
        JSONArray array = new JSONArray();
        for (VotingOption option : options) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", option.getId());
                obj.put("electionId", option.getElectionId());
                obj.put("optionName", option.getOptionName());
                obj.put("description", option.getDescription());
                if (option.getLogoPath() != null) {
                    obj.put("logoPath", option.getLogoPath());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("VotingOptionManager", "Error saving options", e);
        }
    }
}
```
---


# XML Layouts

## activity_admin_dashboard.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageView
        android:id="@+id/backgroundImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/app_background"
        android:scaleType="centerCrop"
        android:alpha="0.2"
        android:contentDescription="@null" />
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp"
        android:background="@android:color/transparent">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:background="@android:color/transparent"
            android:gravity="center_vertical"
            android:padding="10dp"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/dashboard_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Admin Dashboard"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="@android:color/black"
                android:visibility="gone"/>
            <EditText
                android:id="@+id/etSearch"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_weight="1"
                android:hint="Search..."
                android:background="@drawable/bg_search_input"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:textSize="14sp"
                android:imeOptions="actionSearch"
                android:inputType="text"
                android:maxLines="1"
                android:layout_marginEnd="16dp"
                android:drawableStart="@drawable/ic_search"
                android:drawablePadding="8dp"/>
            <FrameLayout
                android:id="@+id/notificationContainer"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content">
                <ImageView
                    android:id="@+id/icon_notification"
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@drawable/ic_notifications"
                    app:tint="#111827"/>
                <TextView
                    android:id="@+id/tvNotificationBadge"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:background="@drawable/bg_notification_badge"
                    android:text="1"
                    android:textColor="#FFFFFF"
                    android:textSize="10sp"
                    android:gravity="center"
                    android:layout_gravity="top|end"
                    android:visibility="gone"/>
            </FrameLayout>
        </LinearLayout>
        <FrameLayout
            android:id="@+id/fragment_container"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:background="@android:color/transparent"/>
        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottom_navigation"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@android:color/transparent"
            app:menu="@menu/admin_bottom_nav_menu"
            app:itemIconTint="@color/selector_bottom_nav"
            app:itemTextColor="@color/selector_bottom_nav"
            app:labelVisibilityMode="auto" />
    </LinearLayout>
</FrameLayout>
```
---

## activity_government_login.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/app_background"
        android:scaleType="centerCrop"
        android:alpha="0.2"
        android:contentDescription="@null" />
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:background="@android:color/transparent"
        android:padding="24dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:layout_gravity="center">
            <TextView
                android:text="Admin Login - Election Dept"
                android:textSize="22sp"
                android:textStyle="bold"
                android:textColor="@android:color/black"
                android:layout_marginBottom="24dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />
            <EditText
                android:id="@+id/deptCodeInput"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Enter Department Code"
                android:layout_marginBottom="16dp"
                android:padding="12dp"
                android:background="@drawable/edittext_box_style"
                android:textColor="@android:color/black" />
            <EditText
                android:id="@+id/passwordInput"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Enter Password"
                android:inputType="textPassword"
                android:layout_marginBottom="24dp"
                android:padding="12dp"
                android:background="@drawable/edittext_box_style"
                android:textColor="@android:color/black" />
            <Button
                android:id="@+id/adminLoginBtn"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Login" />
        </LinearLayout>
    </ScrollView>
</FrameLayout>
```
---

## activity_login.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageView
        android:id="@+id/backgroundImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/app_background"
        android:scaleType="centerCrop"
        android:alpha="0.2"
        android:contentDescription="@null" />
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="24dp"
        android:fillViewport="true">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center_horizontal"
            android:background="@android:color/transparent">
            <com.airbnb.lottie.LottieAnimationView
                android:id="@+id/loginAnimation"
                android:layout_width="300dp"
                android:layout_height="300dp"
                app:lottie_rawRes="@raw/login_animation"
                app:lottie_autoPlay="true"
                app:lottie_loop="true" />
            <EditText
                android:id="@+id/aadhaarInput"
                android:hint="Enter Aadhaar Number"
                android:inputType="number"
                android:maxLength="12"
                android:digits="0123456789"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="12dp"
                android:layout_marginTop="10dp"
                android:background="@drawable/edittext_box_style"/>
            <EditText
                android:id="@+id/dobInput"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Enter DOB (yyyy-mm-dd)"
                android:focusable="false"
                android:clickable="true"
                android:inputType="none"
                android:drawableEnd="@drawable/baseline_calendar_month_24"
                android:padding="12dp"
                android:layout_marginTop="10dp"
                android:background="@drawable/edittext_box_style" />
            <Button
                android:id="@+id/sendOtpButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Send OTP"
                android:layout_marginTop="12dp"/>
            <LinearLayout
                android:id="@+id/otpArea"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:visibility="gone"
                android:layout_marginTop="12dp">
                <EditText
                    android:id="@+id/otpInput"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:hint="Enter OTP"
                    android:inputType="number"
                    android:maxLength="6"
                    android:padding="12dp"
                    android:background="@drawable/edittext_box_style"/>
                <TextView
                    android:id="@+id/otpTimerText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Time left: 02:00"
                    android:layout_marginTop="6dp"
                    android:visibility="gone"/>
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center"
                    android:layout_marginTop="8dp">
                    <Button
                        android:id="@+id/verifyOtpButton"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Verify OTP" />
                    <Button
                        android:id="@+id/resendOtpButton"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Resend OTP"
                        android:layout_marginStart="12dp"/>
                </LinearLayout>
            </LinearLayout>
            <Button
                android:id="@+id/loginButton"
                android:text="Login"
                android:layout_marginTop="20dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>
            <TextView
                android:id="@+id/govtLoginLink"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Government Login"
                android:textColor="@android:color/holo_blue_dark"
                android:textStyle="bold"
                android:paddingTop="34dp"
                android:clickable="true"
                android:focusable="true" />
        </LinearLayout>
    </ScrollView>
</FrameLayout>
```
---

## activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageView
        android:id="@+id/backgroundImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/app_background"
        android:scaleType="centerCrop"
        android:alpha="0.2"
        android:contentDescription="@null" />
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp"
        android:background="@android:color/transparent">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:background="@android:color/transparent"
            android:gravity="center_vertical"
            android:padding="10dp"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/dashboard_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Dashboard"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="@android:color/black"
                android:visibility="gone"/>
            <EditText
                android:id="@+id/etSearch"
                android:layout_width="0dp"
                android:layout_height="40dp"
                android:layout_weight="1"
                android:hint="Search..."
                android:background="@drawable/bg_search_input"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:textSize="14sp"
                android:imeOptions="actionSearch"
                android:inputType="text"
                android:maxLines="1"
                android:layout_marginEnd="16dp"
                android:drawableStart="@drawable/ic_search"
                android:drawablePadding="8dp"/>
            <FrameLayout
                android:id="@+id/notificationContainer"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content">
                <ImageView
                    android:id="@+id/icon_notification"
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@drawable/ic_notifications"
                    app:tint="#111827"/>
                <TextView
                    android:id="@+id/tvNotificationBadge"
                    android:layout_width="16dp"
                    android:layout_height="16dp"
                    android:background="@drawable/bg_notification_badge"
                    android:text="1"
                    android:textColor="#FFFFFF"
                    android:textSize="10sp"
                    android:gravity="center"
                    android:layout_gravity="top|end"
                    android:visibility="gone"/>
            </FrameLayout>
        </LinearLayout>
        <FrameLayout
            android:id="@+id/fragment_container"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:background="@android:color/transparent"/>
        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottom_navigation"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@android:color/transparent"
            app:menu="@menu/bottom_nav_menu"
            app:itemIconTint="@color/selector_bottom_nav"
            app:itemTextColor="@color/selector_bottom_nav" />
    </LinearLayout>
</FrameLayout>
```
---

## activity_notifications.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F8F9FA">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@drawable/bg_header_gradient"
        android:padding="20dp"
        android:elevation="4dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">
            <ImageView
                android:id="@+id/btnBack"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@drawable/ic_back_arrow"
                app:tint="#FFFFFF"
                android:layout_marginEnd="16dp"/>
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Notifications"
                android:textColor="#FFFFFF"
                android:textSize="24sp"
                android:textStyle="bold"/>
        </LinearLayout>
    </LinearLayout>
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            <LinearLayout
                android:id="@+id/notificationContainer"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"/>
            <LinearLayout
                android:id="@+id/layoutEmptyState"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="40dp"
                android:visibility="gone">
                <ImageView
                    android:layout_width="100dp"
                    android:layout_height="100dp"
                    android:src="@drawable/ic_notifications"
                    app:tint="#D1D5DB"
                    android:layout_marginBottom="16dp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="No Notifications"
                    android:textColor="#6B7280"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="You're all caught up!"
                    android:textColor="#9CA3AF"
                    android:textSize="14sp"/>
            </LinearLayout>
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</LinearLayout>
```
---

## activity_party_details.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F9FAFB"
    android:padding="16dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center_horizontal">
        <ImageView
            android:id="@+id/imgPartyLogo"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:layout_marginBottom="12dp"
            android:contentDescription="Party Logo"
            android:background="@drawable/bg_party_circle"
            android:padding="16dp"
            android:scaleType="centerInside" />
        <TextView
            android:id="@+id/tvPartyName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Party Name"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#1E3A8A"
            android:gravity="center"
            android:paddingBottom="8dp" />
        <TextView
            android:id="@+id/tvLeader"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Leader:"
            android:textSize="16sp"
            android:textColor="#111827"
            android:paddingTop="8dp"
            android:gravity="center" />
        <TextView
            android:id="@+id/tvNominees"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Nominees:"
            android:textSize="15sp"
            android:textColor="#374151"
            android:lineSpacingExtra="4dp"
            android:paddingTop="10dp"
            android:gravity="center_horizontal" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Party History"
            android:textStyle="bold"
            android:textSize="18sp"
            android:textColor="#1E3A8A"
            android:paddingTop="18dp"
            android:paddingBottom="6dp" />
        <TextView
            android:id="@+id/tvHistory"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="15sp"
            android:textColor="#374151"
            android:lineSpacingExtra="4dp"
            android:paddingBottom="24dp" />
    </LinearLayout>
</ScrollView>
```
---

## activity_splash.xml
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF"
    android:gravity="center">
    <ImageView
        android:id="@+id/splash_logo"
        android:layout_width="180dp"
        android:layout_height="180dp"
        android:layout_centerInParent="true"
        android:src="@drawable/ic_logo"
        android:contentDescription="App Logo" />
</RelativeLayout>
```
---

## activity_voting.xml
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="20dp">
    <TextView
        android:id="@+id/textViewElectionId"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Election ID"
        android:textSize="20sp"
        android:textStyle="bold"
        android:padding="8dp"
        android:textColor="@android:color/black"/>
    <TextView
        android:id="@+id/textViewUserInfo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Welcome User, you are eligible to vote!"
        android:textSize="16sp"
        android:textColor="#374151"
        android:layout_marginBottom="16dp"/>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvVotingOptions"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:clipToPadding="false"
        android:paddingBottom="80dp"/>
    <LinearLayout
        android:id="@+id/bottomBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#FFFFFF"
        android:elevation="16dp"
        android:padding="16dp"
        android:visibility="gone">
        <TextView
            android:id="@+id/tvSelectedOption"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Selected: None"
            android:gravity="center"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginBottom="12dp"/>
        <Button
            android:id="@+id/voteButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Confirm Vote"
            android:backgroundTint="#1E3A8A"
            android:textColor="#FFFFFF"
            android:padding="12dp"/>
    </LinearLayout>
</LinearLayout>
```
---

## dialog_add_election.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Add/Edit Election"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp"/>
        <EditText
            android:id="@+id/etTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Election Title"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etState"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="State (or National)"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etMinAge"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Minimum Age (e.g. 18)"
            android:inputType="number"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etStatus"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Status (Active/Closed)"
            android:layout_marginBottom="8dp"/>
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Stop Date:"
            android:textSize="14sp"
            android:layout_marginTop="8dp"/>
        <EditText
            android:id="@+id/etStopDate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="YYYY-MM-DD"
            android:focusable="false"
            android:clickable="true"
            android:layout_marginBottom="8dp"/>
    </LinearLayout>
</ScrollView>
```
---

## dialog_add_news.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add/Edit News"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <EditText
        android:id="@+id/etTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Title"
        android:layout_marginBottom="12dp"/>
    <EditText
        android:id="@+id/etDescription"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Description"
        android:minLines="3"
        android:gravity="top"
        android:layout_marginBottom="12dp"/>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_marginBottom="12dp">
        <EditText
            android:id="@+id/etImageUrl"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Image URL (optional)"
            android:inputType="textUri"/>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="— OR —"
            android:layout_gravity="center_horizontal"
            android:layout_marginVertical="8dp"
            android:textColor="#888888"/>
        <Button
            android:id="@+id/btnPickImage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Pick Image from Gallery"
            android:backgroundTint="#4CAF50"
            android:textColor="#FFFFFF"/>
        <ImageView
            android:id="@+id/imgPreview"
            android:layout_width="match_parent"
            android:layout_height="150dp"
            android:layout_marginTop="8dp"
            android:scaleType="centerCrop"
            android:visibility="gone"
            android:background="#EEEEEE"/>
    </LinearLayout>
</LinearLayout>
```
---

## dialog_add_party.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add/Edit Party"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <EditText
        android:id="@+id/etPartyName"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Party Name"
        android:layout_marginBottom="12dp"/>
    <EditText
        android:id="@+id/etPartySymbol"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Symbol (e.g., Lotus, Hand)"
        android:layout_marginBottom="12dp"/>
    <EditText
        android:id="@+id/etPartyDescription"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Description (optional)"
        android:minLines="2"
        android:gravity="top"
        android:layout_marginBottom="12dp"/>
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Party Logo:"
        android:textStyle="bold"
        android:layout_marginTop="8dp"
        android:layout_marginBottom="8dp"/>
    <ImageView
        android:id="@+id/ivPartyLogo"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_gravity="center"
        android:scaleType="centerCrop"
        android:background="#F0F0F0"
        android:padding="8dp"
        android:layout_marginBottom="12dp"
        android:contentDescription="Party Logo Preview"/>
    <Button
        android:id="@+id/btnSelectLogo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Select Logo Image"
        android:backgroundTint="#2563EB"
        android:textColor="#FFFFFF"/>
</LinearLayout>
```
---

## dialog_add_user.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Aadhaar Number (12 digits)">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etAadhaar"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="number"
                android:maxLength="12" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Full Name">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPersonName" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Date of Birth (YYYY-MM-DD)">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etDob"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:focusable="false"
                android:clickable="true"
                android:inputType="none" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Email">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etEmail"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textEmailAddress" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Mobile Number">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etMobile"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="phone"
                android:maxLength="10" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Address">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etAddress"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPostalAddress"
                android:minLines="2" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="City">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etCity"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="State">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etState"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Pincode">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etPincode"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="number"
                android:maxLength="6" />
        </com.google.android.material.textfield.TextInputLayout>
        <CheckBox
            android:id="@+id/cbEligible"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Eligible to Vote"
            android:checked="true"
            android:layout_marginTop="8dp" />
    </LinearLayout>
</ScrollView>
```
---

## dialog_add_voting_option.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add Voting Option"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Select Existing Party (Optional)"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>
        <Spinner
            android:id="@+id/spinnerParties"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:minHeight="48dp" />
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp"
            android:hint="Candidate Name">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etOptionName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPersonName" />
        </com.google.android.material.textfield.TextInputLayout>
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Party Name (Auto-filled)">
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etOptionDescription"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:enabled="false"
                android:textColor="#111827"/>
        </com.google.android.material.textfield.TextInputLayout>
    </LinearLayout>
</LinearLayout>
```
---

## dialog_edit_user.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Edit User Details"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp"/>
        <EditText
            android:id="@+id/etName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Name"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etEmail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Email"
            android:inputType="textEmailAddress"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etMobile"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Mobile"
            android:inputType="phone"
            android:layout_marginBottom="8dp"/>
        <EditText
            android:id="@+id/etAddress"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Address"
            android:layout_marginBottom="8dp"/>
        <CheckBox
            android:id="@+id/cbEligible"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Eligible to Vote"/>
    </LinearLayout>
</ScrollView>
```
---

## dialog_manage_voting_options.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">
    <TextView
        android:id="@+id/tvElectionTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Manage Voting Options"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <Button
        android:id="@+id/btnAddOption"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add Voting Option"
        android:backgroundTint="#1E3A8A"
        android:textColor="#FFFFFF"
        android:layout_marginBottom="16dp"/>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="300dp">
        <LinearLayout
            android:id="@+id/optionsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```
---

## dialog_party_details.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="24dp"
    app:cardElevation="12dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="140dp"
            android:background="#F3F4F6">
            <ImageView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:alpha="0.1"
                android:scaleType="centerCrop"
                android:src="@drawable/ic_launcher_background" />
            <com.google.android.material.card.MaterialCardView
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:layout_gravity="center"
                app:cardBackgroundColor="#FFFFFF"
                app:cardCornerRadius="50dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp">
                <ImageView
                    android:id="@+id/imgPartyLogo"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:padding="16dp"
                    android:scaleType="fitCenter"
                    android:src="@drawable/ic_bjp" />
            </com.google.android.material.card.MaterialCardView>
        </FrameLayout>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp">
            <TextView
                android:id="@+id/tvPartyName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center"
                android:text="Party Name"
                android:textColor="#111827"
                android:textSize="22sp"
                android:textStyle="bold" />
            <TextView
                android:id="@+id/tvPartySymbol"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:layout_marginTop="8dp"
                android:background="@drawable/bg_party_circle"
                android:backgroundTint="#EFF6FF"
                android:paddingHorizontal="12dp"
                android:paddingVertical="4dp"
                android:text="Symbol: Lotus"
                android:textColor="#1D4ED8"
                android:textSize="14sp"
                android:textStyle="bold" />
            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:layout_marginVertical="16dp"
                android:background="#F3F4F6" />
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="8dp"
                android:text="About"
                android:textColor="#6B7280"
                android:textSize="12sp"
                android:textStyle="bold"
                android:textAllCaps="true" />
            <TextView
                android:id="@+id/tvPartyDescription"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:lineSpacingExtra="6dp"
                android:text="Party Description goes here..."
                android:textColor="#374151"
                android:textSize="15sp" />
            <Button
                android:id="@+id/btnClose"
                style="@style/Widget.MaterialComponents.Button.TextButton"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Close"
                android:textColor="#1E3A8A"
                android:textStyle="bold" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## dialog_resolve_feedback.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp"
    android:background="@android:color/white">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Resolve Feedback"
        android:textColor="#1E3A8A"
        android:textSize="22sp"
        android:textStyle="bold"
        android:layout_marginBottom="8dp"/>
    <TextView
        android:id="@+id/tvDialogFeedbackTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Issue Title"
        android:textColor="#374151"
        android:textSize="16sp"
        android:textStyle="bold"
        android:layout_marginBottom="4dp"/>
    <TextView
        android:id="@+id/tvDialogUserInfo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="User: Name • Aadhaar"
        android:textColor="#6B7280"
        android:textSize="13sp"
        android:layout_marginBottom="16dp"/>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="User's Issue:"
        android:textColor="#374151"
        android:textSize="14sp"
        android:textStyle="bold"
        android:layout_marginBottom="6dp"/>
    <TextView
        android:id="@+id/tvDialogFeedbackDescription"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Description of the issue..."
        android:textColor="#6B7280"
        android:textSize="14sp"
        android:background="@drawable/bg_status_active"
        android:backgroundTint="#F9FAFB"
        android:padding="12dp"
        android:layout_marginBottom="20dp"
        android:maxLines="6"
        android:scrollbars="vertical"/>
    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        app:boxBackgroundMode="outline"
        app:boxCornerRadiusTopStart="12dp"
        app:boxCornerRadiusTopEnd="12dp"
        app:boxCornerRadiusBottomStart="12dp"
        app:boxCornerRadiusBottomEnd="12dp"
        app:boxStrokeColor="#1E3A8A"
        app:hintTextColor="#1E3A8A">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etAdminResponse"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Your response to the user"
            android:inputType="textMultiLine|textCapSentences"
            android:minLines="4"
            android:maxLines="8"
            android:gravity="top|start"
            android:textColor="#374151"
            android:textSize="16sp"/>
    </com.google.android.material.textfield.TextInputLayout>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="end"
        android:layout_marginTop="8dp">
        <Button
            android:id="@+id/btnCancelResolve"
            style="@style/Widget.MaterialComponents.Button.OutlinedButton"
            android:layout_width="wrap_content"
            android:layout_height="56dp"
            android:text="Cancel"
            android:textColor="#6B7280"
            android:layout_marginEnd="12dp"
            app:strokeColor="#D1D5DB"
            app:cornerRadius="12dp"/>
        <Button
            android:id="@+id/btnMarkResolved"
            android:layout_width="wrap_content"
            android:layout_height="56dp"
            android:text="Mark as Resolved"
            android:backgroundTint="#059669"
            android:textColor="#FFFFFF"
            android:textStyle="bold"
            app:cornerRadius="12dp"
            app:icon="@android:drawable/checkbox_on_background"
            app:iconTint="#FFFFFF"/>
    </LinearLayout>
</LinearLayout>
```
---

## dialog_submit_feedback.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp"
    android:background="@android:color/white">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Submit Feedback"
        android:textColor="#1E3A8A"
        android:textSize="22sp"
        android:textStyle="bold"
        android:layout_marginBottom="8dp"/>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="We're here to help! Describe your issue or query."
        android:textColor="#6B7280"
        android:textSize="14sp"
        android:layout_marginBottom="20dp"/>
    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        app:boxBackgroundMode="outline"
        app:boxCornerRadiusTopStart="12dp"
        app:boxCornerRadiusTopEnd="12dp"
        app:boxCornerRadiusBottomStart="12dp"
        app:boxCornerRadiusBottomEnd="12dp"
        app:boxStrokeColor="#1E3A8A"
        app:hintTextColor="#1E3A8A">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etFeedbackTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Issue Title"
            android:inputType="textCapSentences"
            android:maxLines="1"
            android:textColor="#374151"
            android:textSize="16sp"/>
    </com.google.android.material.textfield.TextInputLayout>
    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        app:boxBackgroundMode="outline"
        app:boxCornerRadiusTopStart="12dp"
        app:boxCornerRadiusTopEnd="12dp"
        app:boxCornerRadiusBottomStart="12dp"
        app:boxCornerRadiusBottomEnd="12dp"
        app:boxStrokeColor="#1E3A8A"
        app:hintTextColor="#1E3A8A">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etFeedbackDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Describe your issue in detail"
            android:inputType="textMultiLine|textCapSentences"
            android:minLines="4"
            android:maxLines="8"
            android:gravity="top|start"
            android:textColor="#374151"
            android:textSize="16sp"/>
    </com.google.android.material.textfield.TextInputLayout>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="end"
        android:layout_marginTop="8dp">
        <Button
            android:id="@+id/btnCancelFeedback"
            style="@style/Widget.MaterialComponents.Button.OutlinedButton"
            android:layout_width="wrap_content"
            android:layout_height="56dp"
            android:text="Cancel"
            android:textColor="#6B7280"
            android:layout_marginEnd="12dp"
            app:strokeColor="#D1D5DB"
            app:cornerRadius="12dp"/>
        <Button
            android:id="@+id/btnSubmitFeedback"
            android:layout_width="wrap_content"
            android:layout_height="56dp"
            android:text="Submit"
            android:backgroundTint="#1E3A8A"
            android:textColor="#FFFFFF"
            android:textStyle="bold"
            app:cornerRadius="12dp"/>
    </LinearLayout>
</LinearLayout>
```
---

## fragment_account.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F8F9FA">
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/transparent"
        app:elevation="0dp">
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">
            <View
                android:layout_width="match_parent"
                android:layout_height="180dp"
                android:background="@drawable/bg_header_gradient"
                android:layout_marginBottom="60dp"/>
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center_horizontal"
                android:layout_marginTop="100dp"
                app:layout_collapseMode="parallax">
                <RelativeLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content">
                    <com.google.android.material.card.MaterialCardView
                        android:layout_width="140dp"
                        android:layout_height="140dp"
                        app:cardCornerRadius="70dp"
                        app:cardElevation="12dp"
                        app:strokeColor="#FFFFFF"
                        app:strokeWidth="5dp">
                        <ImageView
                            android:id="@+id/img_profile"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:scaleType="centerCrop"
                            android:src="@drawable/ic_account"
                            android:background="#E0E0E0"/>
                    </com.google.android.material.card.MaterialCardView>
                    <com.google.android.material.floatingactionbutton.FloatingActionButton
                        android:id="@+id/btn_change_photo"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_alignEnd="@id/img_profile"
                        android:layout_alignBottom="@id/img_profile"
                        android:layout_marginEnd="4dp"
                        android:layout_marginBottom="4dp"
                        android:src="@android:drawable/ic_menu_camera"
                        app:backgroundTint="#1E3A8A"
                        app:tint="#FFFFFF"
                        app:fabSize="mini"
                        app:elevation="6dp" />
                </RelativeLayout>
                <TextView
                    android:id="@+id/txt_name"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:text="User Name"
                    android:textColor="#111827"
                    android:textSize="26sp"
                    android:textStyle="bold"
                    android:fontFamily="sans-serif-medium"/>
                <TextView
                    android:id="@+id/txt_aadhaar"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Aadhaar: XXXX-XXXX-XXXX"
                    android:textColor="#6B7280"
                    android:textSize="14sp"
                    android:background="@drawable/bg_status_active"
                    android:backgroundTint="#F3F4F6"
                    android:paddingHorizontal="12dp"
                    android:paddingVertical="4dp"/>
            </LinearLayout>
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        <LinearLayout
            android:id="@+id/container_details"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp"
            android:layoutAnimation="@anim/layout_animation_slide_up">
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardInfo"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="20dp"
                app:cardBackgroundColor="#FFFFFF"
                app:cardCornerRadius="20dp"
                app:cardElevation="2dp"
                app:strokeColor="#F3F4F6"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="24dp">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:gravity="center_vertical"
                        android:layout_marginBottom="20dp">
                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@android:drawable/ic_menu_my_calendar"
                            app:tint="#1E3A8A"
                            android:layout_marginEnd="12dp"/>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Personal Details"
                            android:textColor="#1E3A8A"
                            android:textSize="18sp"
                            android:textStyle="bold" />
                    </LinearLayout>
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Mobile Number"
                            android:textColor="#9CA3AF"
                            android:textSize="12sp"
                            android:textAllCaps="true"
                            android:letterSpacing="0.05"/>
                        <TextView
                            android:id="@+id/txt_mobile"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="+91 9876543210"
                            android:textColor="#374151"
                            android:textSize="16sp"
                            android:layout_marginTop="4dp"
                            android:fontFamily="sans-serif-medium"/>
                    </LinearLayout>
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="#F3F4F6"
                        android:layout_marginBottom="16dp"/>
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Email Address"
                            android:textColor="#9CA3AF"
                            android:textSize="12sp"
                            android:textAllCaps="true"
                            android:letterSpacing="0.05"/>
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:layout_marginTop="4dp">
                            <EditText
                                android:id="@+id/edit_email"
                                android:layout_width="0dp"
                                android:layout_height="wrap_content"
                                android:layout_weight="1"
                                android:background="@android:color/transparent"
                                android:hint="Enter email"
                                android:inputType="textEmailAddress"
                                android:textColor="#374151"
                                android:textSize="16sp"
                                android:fontFamily="sans-serif-medium"/>
                            <ImageView
                                android:id="@+id/btn_update_email"
                                android:layout_width="32dp"
                                android:layout_height="32dp"
                                android:src="@android:drawable/ic_menu_save"
                                app:tint="#1E3A8A"
                                android:clickable="true"
                                android:focusable="true"
                                android:background="?attr/selectableItemBackgroundBorderless"
                                android:padding="4dp"/>
                        </LinearLayout>
                    </LinearLayout>
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="#F3F4F6"
                        android:layout_marginBottom="16dp"/>
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginBottom="16dp"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Address"
                            android:textColor="#9CA3AF"
                            android:textSize="12sp"
                            android:textAllCaps="true"
                            android:letterSpacing="0.05"/>
                        <TextView
                            android:id="@+id/txt_address"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="123, Street Name"
                            android:textColor="#374151"
                            android:textSize="16sp"
                            android:layout_marginTop="4dp"
                            android:fontFamily="sans-serif-medium"/>
                    </LinearLayout>
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal">
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="City"
                                android:textColor="#9CA3AF"
                                android:textSize="12sp"
                                android:textAllCaps="true"
                                android:letterSpacing="0.05"/>
                            <TextView
                                android:id="@+id/txt_city"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="City Name"
                                android:textColor="#374151"
                                android:textSize="16sp"
                                android:layout_marginTop="4dp"
                                android:fontFamily="sans-serif-medium"/>
                        </LinearLayout>
                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:orientation="vertical">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Pincode"
                                android:textColor="#9CA3AF"
                                android:textSize="12sp"
                                android:textAllCaps="true"
                                android:letterSpacing="0.05"/>
                            <TextView
                                android:id="@+id/txt_pincode"
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:text="560001"
                                android:textColor="#374151"
                                android:textSize="16sp"
                                android:layout_marginTop="4dp"
                                android:fontFamily="sans-serif-medium"/>
                        </LinearLayout>
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardStatus"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardBackgroundColor="#FFFFFF"
                app:cardCornerRadius="20dp"
                app:cardElevation="2dp"
                app:strokeColor="#F3F4F6"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:orientation="horizontal"
                    android:padding="20dp">
                    <ImageView
                        android:layout_width="48dp"
                        android:layout_height="48dp"
                        android:background="@drawable/circle_background"
                        android:backgroundTint="#ECFDF5"
                        android:padding="10dp"
                        android:src="@android:drawable/checkbox_on_background"
                        app:tint="#059669" />
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="20dp"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Voting Eligibility"
                            android:textColor="#9CA3AF"
                            android:textSize="12sp"
                            android:textAllCaps="true"
                            android:letterSpacing="0.05"/>
                        <TextView
                            android:id="@+id/txt_eligible"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:text="Eligible to Vote"
                            android:textColor="#059669"
                            android:textSize="18sp"
                            android:textStyle="bold"
                            android:layout_marginTop="2dp"/>
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardFeedback"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="20dp"
                app:cardBackgroundColor="#FFFFFF"
                app:cardCornerRadius="20dp"
                app:cardElevation="2dp"
                app:strokeColor="#F3F4F6"
                app:strokeWidth="1dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="24dp">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:gravity="center_vertical"
                        android:layout_marginBottom="16dp">
                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@android:drawable/ic_dialog_email"
                            app:tint="#1E3A8A"
                            android:layout_marginEnd="12dp"/>
                        <TextView
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:text="My Feedback &amp; Queries"
                            android:textColor="#1E3A8A"
                            android:textSize="18sp"
                            android:textStyle="bold" />
                        <TextView
                            android:id="@+id/tvFeedbackBadge"
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:text="0"
                            android:textColor="#FFFFFF"
                            android:textSize="12sp"
                            android:textStyle="bold"
                            android:gravity="center"
                            android:background="@drawable/circle_background"
                            android:backgroundTint="#DC2626"
                            android:visibility="gone"/>
                    </LinearLayout>
                    <Button
                        android:id="@+id/btnSubmitFeedback"
                        android:layout_width="match_parent"
                        android:layout_height="56dp"
                        android:text="Submit New Feedback"
                        android:backgroundTint="#1E3A8A"
                        android:textColor="#FFFFFF"
                        android:textStyle="bold"
                        android:layout_marginBottom="16dp"
                        app:cornerRadius="14dp"
                        app:icon="@android:drawable/ic_input_add"
                        app:iconTint="#FFFFFF"/>
                    <LinearLayout
                        android:id="@+id/feedbackListContainer"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"/>
                    <TextView
                        android:id="@+id/tvNoFeedback"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="No feedback submitted yet.\nTap the button above to submit your first query."
                        android:textColor="#9CA3AF"
                        android:textSize="14sp"
                        android:gravity="center"
                        android:padding="20dp"
                        android:visibility="gone"/>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <Button
                android:id="@+id/btn_save_photo"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:layout_marginTop="24dp"
                android:text="Save Profile Photo"
                android:backgroundTint="#1E3A8A"
                android:textColor="#FFFFFF"
                android:textStyle="bold"
                android:visibility="gone"
                app:cornerRadius="16dp"
                app:elevation="4dp"/>
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```
---

## fragment_admin_election.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <Button
        android:id="@+id/btnAddElection"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add New Election"
        android:backgroundTint="#1E3A8A"
        android:textColor="#FFFFFF"
        android:layout_marginBottom="16dp"/>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <LinearLayout
            android:id="@+id/electionContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```
---

## fragment_admin_feedback.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F8F9FA">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@drawable/bg_header_gradient"
        android:padding="20dp"
        android:elevation="4dp">
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="User Feedback"
            android:textColor="#FFFFFF"
            android:textSize="28sp"
            android:textStyle="bold"
            android:layout_marginBottom="6dp"/>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Manage user queries and issues"
            android:textColor="#E0E7FF"
            android:textSize="14sp"/>
    </LinearLayout>
    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardBackgroundColor="#FFFFFF"
        app:cardCornerRadius="16dp"
        app:cardElevation="2dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="8dp">
            <Button
                android:id="@+id/btnFilterAll"
                style="@style/Widget.MaterialComponents.Button"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:text="All"
                android:textSize="13sp"
                android:backgroundTint="#1E3A8A"
                android:textColor="#FFFFFF"
                app:cornerRadius="10dp"
                android:layout_marginEnd="4dp"/>
            <Button
                android:id="@+id/btnFilterPending"
                style="@style/Widget.MaterialComponents.Button.OutlinedButton"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:text="Pending"
                android:textSize="13sp"
                android:textColor="#6B7280"
                app:strokeColor="#D1D5DB"
                app:cornerRadius="10dp"
                android:layout_marginEnd="4dp"/>
            <Button
                android:id="@+id/btnFilterResolved"
                style="@style/Widget.MaterialComponents.Button.OutlinedButton"
                android:layout_width="0dp"
                android:layout_height="48dp"
                android:layout_weight="1"
                android:text="Resolved"
                android:textSize="13sp"
                android:textColor="#6B7280"
                app:strokeColor="#D1D5DB"
                app:cornerRadius="10dp"/>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:fillViewport="true">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingHorizontal="16dp"
            android:paddingBottom="16dp">
            <LinearLayout
                android:id="@+id/feedbackContainer"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"/>
            <LinearLayout
                android:id="@+id/layoutEmptyState"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:gravity="center"
                android:padding="40dp"
                android:visibility="gone">
                <ImageView
                    android:layout_width="120dp"
                    android:layout_height="120dp"
                    android:src="@android:drawable/ic_dialog_email"
                    app:tint="#D1D5DB"
                    android:layout_marginBottom="16dp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="No Feedback Yet"
                    android:textColor="#6B7280"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="8dp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="User feedback will appear here"
                    android:textColor="#9CA3AF"
                    android:textSize="14sp"
                    android:gravity="center"/>
            </LinearLayout>
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</LinearLayout>
```
---

## fragment_admin_home.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="16dp">
        <Button
            android:id="@+id/btnAddNews"
            android:layout_width="0dp"
            android:layout_height="56dp"
            android:layout_weight="1"
            android:text="Add News"
            android:backgroundTint="#1E3A8A"
            android:textColor="#FFFFFF"
            android:textStyle="bold"
            app:cornerRadius="12dp"
            app:icon="@android:drawable/ic_input_add"
            app:iconTint="#FFFFFF"
            android:layout_marginEnd="8dp"/>
        <Button
            android:id="@+id/btnViewFeedback"
            android:layout_width="0dp"
            android:layout_height="56dp"
            android:layout_weight="1"
            android:text="Feedback"
            android:backgroundTint="#059669"
            android:textColor="#FFFFFF"
            android:textStyle="bold"
            app:cornerRadius="12dp"
            app:icon="@android:drawable/ic_dialog_email"
            app:iconTint="#FFFFFF"/>
    </LinearLayout>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <LinearLayout
            android:id="@+id/newsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```
---

## fragment_admin_party.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Manage Political Parties"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <Button
        android:id="@+id/btnAddParty"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Add Party"
        android:backgroundTint="#1E3A8A"
        android:textColor="#FFFFFF"
        android:layout_marginBottom="16dp"/>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <LinearLayout
            android:id="@+id/partyContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"/>
    </ScrollView>
</LinearLayout>
```
---

## fragment_admin_result.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F3F4F6">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Election Results Management"
        android:textSize="20sp"
        android:textStyle="bold"
        android:textColor="#111827"
        android:padding="20dp"
        android:background="#FFFFFF"
        android:elevation="4dp"/>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"
        android:clipToPadding="false">
        <LinearLayout
            android:id="@+id/resultsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```
---

## fragment_admin_user_list.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Registered Users"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>
    <Button
        android:id="@+id/btnAddUser"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="+ Add New User"
        android:backgroundTint="#1E3A8A"
        android:textColor="#FFFFFF"
        android:layout_marginBottom="16dp"/>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <LinearLayout
            android:id="@+id/userContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```
---

## fragment_history.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="#F9FAFB">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Election History"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#111827"
        android:layout_marginBottom="4dp"/>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Past results and upcoming announcements"
        android:textSize="14sp"
        android:textColor="#6B7280"
        android:layout_marginBottom="16dp"/>
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvHistory"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:paddingBottom="16dp"/>
</LinearLayout>
```
---

## fragment_home.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F7F8FA"
    android:padding="12dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <TextView
            android:id="@+id/tvHeader"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="🇮🇳 Indian Election News"
            android:textColor="#1E3A8A"
            android:textStyle="bold"
            android:textSize="22sp"
            android:paddingBottom="8dp" />
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="48dp"
            app:cardCornerRadius="24dp"
            app:cardElevation="3dp"
            android:layout_marginBottom="12dp">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingHorizontal="12dp">
                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/ic_menu_search"
                    android:tint="#6B7280"
                    android:contentDescription="Search" />
                <EditText
                    android:id="@+id/etSearch"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="8dp"
                    android:background="@android:color/transparent"
                    android:hint="Search latest election updates..."
                    android:textSize="14sp"
                    android:inputType="text" />
            </LinearLayout>
        </androidx.cardview.widget.CardView>
        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:scrollbars="none"
            android:layout_marginBottom="12dp">
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal">
                <com.google.android.material.chip.Chip
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="National"
                    style="@style/Widget.MaterialComponents.Chip.Choice"
                    android:checked="true" />
                <com.google.android.material.chip.Chip
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="State"
                    style="@style/Widget.MaterialComponents.Chip.Choice" />
                <com.google.android.material.chip.Chip
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="EC Updates"
                    style="@style/Widget.MaterialComponents.Chip.Choice" />
                <com.google.android.material.chip.Chip
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Party News"
                    style="@style/Widget.MaterialComponents.Chip.Choice" />
            </LinearLayout>
        </HorizontalScrollView>
        <LinearLayout
            android:id="@+id/newsContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```
---

## fragment_list.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/scrollRoot"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F9FAFB"
    android:padding="12dp">
    <LinearLayout
        android:id="@+id/rootLinear"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center_horizontal">
        <TextView
            android:id="@+id/tvHeader"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="🇮🇳 Indian Political Parties"
            android:textSize="22sp"
            android:textColor="#1E3A8A"
            android:textStyle="bold"
            android:gravity="center"
            android:paddingBottom="12dp" />
        <TextView
            android:id="@+id/tvSubtitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Tap any card to view details"
            android:textSize="12sp"
            android:textColor="#6B7280"
            android:gravity="center"
            android:paddingBottom="12dp" />
        <GridLayout
            android:id="@+id/gridParties"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:columnCount="2"
            android:orientation="horizontal"
            android:useDefaultMargins="true"
            android:alignmentMode="alignMargins"
            android:rowOrderPreserved="false">
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardBJP"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/img_bjp"
                        android:contentDescription="BJP Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Bharatiya Janata Party (BJP)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardINC"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/img_inc"
                        android:contentDescription="INC Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Indian National Congress (INC)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardJDS"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="JDS">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/img_jds"
                        android:contentDescription="JDS Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Janata Dal (Secular) (JDS)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardAAP"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="AAP">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_aap"
                        android:contentDescription="AAP Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Aam Aadmi Party (AAP)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardTMC"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="TMC">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_tmc"
                        android:contentDescription="TMC Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Trinamool Congress (TMC)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardDMK"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="DMK">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_dmk"
                        android:contentDescription="DMK Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Dravida Munnetra Kazhagam (DMK)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardAIADMK"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="AIADMK">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_aiadmk"
                        android:contentDescription="AIADMK Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="AIADMK"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardSP"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="SP">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_sp"
                        android:contentDescription="SP Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Samajwadi Party (SP)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/cardBSP"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_columnWeight="1"
                app:cardCornerRadius="14dp"
                app:cardElevation="4dp"
                app:strokeColor="#E5E7EB"
                app:strokeWidth="1dp"
                android:layout_margin="8dp"
                android:clickable="true"
                android:focusable="true"
                android:tag="BSP">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">
                    <ImageView
                        android:layout_width="72dp"
                        android:layout_height="72dp"
                        android:src="@drawable/ic_bsp"
                        android:contentDescription="BSP Logo"
                        android:background="@drawable/bg_party_circle"
                        android:padding="8dp" />
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Bahujan Samaj Party (BSP)"
                        android:textColor="#111827"
                        android:textStyle="bold"
                        android:textSize="13sp"
                        android:gravity="center"
                        android:paddingTop="8dp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </GridLayout>
        <TextView
            android:id="@+id/tvFooterNote"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Made by @BNPR.com."
            android:textSize="12sp"
            android:textColor="#6B7280"
            android:gravity="center"
            android:paddingTop="18dp"
            android:paddingBottom="24dp" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```
---

## fragment_vote.xml
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/electionRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</LinearLayout>
```
---

## item_admin_feedback_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="3dp"
    app:strokeColor="#E5E7EB"
    app:strokeWidth="1dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="18dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="10dp">
            <TextView
                android:id="@+id/tvAdminFeedbackTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Issue Title"
                android:textColor="#111827"
                android:textSize="17sp"
                android:textStyle="bold"
                android:maxLines="2"
                android:ellipsize="end"/>
            <TextView
                android:id="@+id/tvAdminFeedbackStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Pending"
                android:textColor="#FFFFFF"
                android:textSize="11sp"
                android:textStyle="bold"
                android:background="@drawable/bg_status_active"
                android:backgroundTint="#F59E0B"
                android:paddingHorizontal="12dp"
                android:paddingVertical="5dp"
                android:layout_marginStart="8dp"/>
        </LinearLayout>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="@drawable/bg_status_active"
            android:backgroundTint="#F9FAFB"
            android:padding="10dp"
            android:layout_marginBottom="10dp">
            <ImageView
                android:layout_width="16dp"
                android:layout_height="16dp"
                android:src="@android:drawable/ic_menu_myplaces"
                app:tint="#6B7280"
                android:layout_marginEnd="6dp"/>
            <TextView
                android:id="@+id/tvAdminFeedbackUser"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="User Name • XXXX-XXXX-1234"
                android:textColor="#374151"
                android:textSize="13sp"
                android:textStyle="bold"/>
        </LinearLayout>
        <TextView
            android:id="@+id/tvAdminFeedbackDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Description of the issue goes here..."
            android:textColor="#4B5563"
            android:textSize="14sp"
            android:lineSpacingExtra="2dp"
            android:layout_marginBottom="10dp"/>
        <TextView
            android:id="@+id/tvAdminFeedbackDate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Submitted on: 2024-12-05 10:30 AM"
            android:textColor="#9CA3AF"
            android:textSize="12sp"
            android:layout_marginBottom="12dp"/>
        <LinearLayout
            android:id="@+id/layoutAdminResponsePreview"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="@drawable/bg_status_active"
            android:backgroundTint="#ECFDF5"
            android:padding="12dp"
            android:layout_marginBottom="12dp"
            android:visibility="gone">
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Your Response:"
                android:textColor="#059669"
                android:textSize="12sp"
                android:textStyle="bold"
                android:layout_marginBottom="4dp"/>
            <TextView
                android:id="@+id/tvAdminResponsePreview"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Response text..."
                android:textColor="#065F46"
                android:textSize="13sp"/>
        </LinearLayout>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="end">
            <Button
                android:id="@+id/btnDeleteFeedback"
                style="@style/Widget.MaterialComponents.Button.OutlinedButton"
                android:layout_width="wrap_content"
                android:layout_height="48dp"
                android:text="Delete"
                android:textColor="#DC2626"
                android:layout_marginEnd="8dp"
                app:strokeColor="#FCA5A5"
                app:cornerRadius="10dp"
                app:icon="@android:drawable/ic_menu_delete"
                app:iconTint="#DC2626"/>
            <Button
                android:id="@+id/btnResolveFeedback"
                android:layout_width="wrap_content"
                android:layout_height="48dp"
                android:text="Resolve"
                android:backgroundTint="#1E3A8A"
                android:textColor="#FFFFFF"
                android:textStyle="bold"
                app:cornerRadius="10dp"
                app:icon="@android:drawable/ic_menu_edit"
                app:iconTint="#FFFFFF"/>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_admin_result_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="16dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    app:strokeColor="#E5E7EB"
    app:strokeWidth="1dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Election Title"
            android:textColor="#111827"
            android:textSize="18sp"
            android:textStyle="bold" />
        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Status: Active"
            android:textColor="#6B7280"
            android:textSize="14sp" />
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:gravity="center_vertical"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/tvResultDate"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Results Date: Not Set"
                android:textColor="#374151"
                android:textSize="14sp" />
            <Button
                android:id="@+id/btnSetDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Set Date"
                style="@style/Widget.MaterialComponents.Button.TextButton"
                android:textColor="#1E3A8A"/>
        </LinearLayout>
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginVertical="12dp"
            android:background="#F3F4F6" />
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Live Count"
            android:textColor="#1E3A8A"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>
        <LinearLayout
            android:id="@+id/layoutCounts"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_election.xml
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="12dp"
    android:background="?android:attr/selectableItemBackground">
    <TextView
        android:id="@+id/election_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Election Title"
        android:textSize="18sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"/>
</LinearLayout>
```
---

## item_election_admin.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Election Title"
            android:textStyle="bold"
            android:textSize="16sp"
            android:textColor="#000000"/>
        <TextView
            android:id="@+id/tvState"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="State: India"
            android:textSize="14sp"
            android:textColor="#666666"/>
        <TextView
            android:id="@+id/tvStatus"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Status: Active"
            android:textSize="14sp"
            android:textColor="#1E88E5"/>
        <TextView
            android:id="@+id/tvStopDate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Stop Date: 2023-12-31"
            android:textSize="14sp"
            android:textColor="#D32F2F"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="end"
            android:layout_marginTop="8dp">
            <Button
                android:id="@+id/btnManageOptions"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:text="Options"
                android:textSize="12sp"
                android:backgroundTint="#4CAF50"
                android:textColor="#FFFFFF"
                android:layout_marginEnd="8dp"/>
            <Button
                android:id="@+id/btnEdit"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:text="Edit"
                android:textSize="12sp"
                android:layout_marginEnd="8dp"/>
            <Button
                android:id="@+id/btnDelete"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:text="Delete"
                android:textSize="12sp"
                android:backgroundTint="#D32F2F"
                android:textColor="#FFFFFF"/>
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_election_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:clickable="true"
    android:focusable="true"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    app:rippleColor="#E0E0E0">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/tvElectionTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="General Election 2024"
                android:textColor="#111827"
                android:textSize="18sp"
                android:textStyle="bold" />
            <TextView
                android:id="@+id/tvStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_status_active"
                android:paddingHorizontal="12dp"
                android:paddingVertical="4dp"
                android:text="Active"
                android:textColor="#059669"
                android:textSize="12sp"
                android:textStyle="bold" />
        </LinearLayout>
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginVertical="12dp"
            android:background="#F3F4F6" />
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="State"
                    android:textColor="#6B7280"
                    android:textSize="12sp" />
                <TextView
                    android:id="@+id/tvState"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Karnataka"
                    android:textColor="#374151"
                    android:textSize="14sp"
                    android:textStyle="bold" />
            </LinearLayout>
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Min Age"
                    android:textColor="#6B7280"
                    android:textSize="12sp" />
                <TextView
                    android:id="@+id/tvMinAge"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="18+"
                    android:textColor="#374151"
                    android:textSize="14sp"
                    android:textStyle="bold" />
            </LinearLayout>
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Ends On"
                    android:textColor="#6B7280"
                    android:textSize="12sp" />
                <TextView
                    android:id="@+id/tvDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="12 Dec 2024"
                    android:textColor="#374151"
                    android:textSize="14sp"
                    android:textStyle="bold" />
            </LinearLayout>
        </LinearLayout>
        <Button
            android:id="@+id/btnVote"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:backgroundTint="#1E3A8A"
            android:text="Vote Now"
            android:textAllCaps="false"
            android:textColor="#FFFFFF"
            app:cornerRadius="8dp" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_feedback_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp"
    app:strokeColor="#E5E7EB"
    app:strokeWidth="1dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="8dp">
            <TextView
                android:id="@+id/tvFeedbackTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Issue Title"
                android:textColor="#111827"
                android:textSize="16sp"
                android:textStyle="bold"
                android:maxLines="2"
                android:ellipsize="end"/>
            <TextView
                android:id="@+id/tvFeedbackStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Pending"
                android:textColor="#FFFFFF"
                android:textSize="11sp"
                android:textStyle="bold"
                android:background="@drawable/bg_status_active"
                android:backgroundTint="#F59E0B"
                android:paddingHorizontal="10dp"
                android:paddingVertical="4dp"
                android:layout_marginStart="8dp"/>
        </LinearLayout>
        <TextView
            android:id="@+id/tvFeedbackDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Description of the issue goes here..."
            android:textColor="#6B7280"
            android:textSize="14sp"
            android:maxLines="3"
            android:ellipsize="end"
            android:layout_marginBottom="8dp"/>
        <TextView
            android:id="@+id/tvFeedbackDate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Submitted on: 2024-12-05"
            android:textColor="#9CA3AF"
            android:textSize="12sp"
            android:layout_marginBottom="8dp"/>
        <LinearLayout
            android:id="@+id/layoutAdminResponse"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="@drawable/bg_status_active"
            android:backgroundTint="#F0FDF4"
            android:padding="12dp"
            android:layout_marginTop="4dp"
            android:visibility="gone">
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="6dp">
                <ImageView
                    android:layout_width="18dp"
                    android:layout_height="18dp"
                    android:src="@android:drawable/ic_dialog_info"
                    app:tint="#059669"
                    android:layout_marginEnd="6dp"/>
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Admin Response"
                    android:textColor="#059669"
                    android:textSize="13sp"
                    android:textStyle="bold"/>
            </LinearLayout>
            <TextView
                android:id="@+id/tvAdminResponse"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Your issue has been resolved..."
                android:textColor="#065F46"
                android:textSize="13sp"
                android:lineSpacingExtra="2dp"/>
            <TextView
                android:id="@+id/tvResolvedDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Resolved on: 2024-12-05"
                android:textColor="#059669"
                android:textSize="11sp"
                android:layout_marginTop="4dp"
                android:textStyle="italic"/>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_history_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal">
            <TextView
                android:id="@+id/tvElectionTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="General Election 2024"
                android:textColor="#111827"
                android:textSize="18sp"
                android:textStyle="bold" />
            <TextView
                android:id="@+id/tvStatusBadge"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_status_active"
                android:paddingHorizontal="12dp"
                android:paddingVertical="4dp"
                android:text="Declared"
                android:textColor="#059669"
                android:textSize="12sp"
                android:textStyle="bold" />
        </LinearLayout>
        <TextView
            android:id="@+id/tvDate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Results Date: 12 Dec 2024"
            android:textColor="#6B7280"
            android:textSize="12sp" />
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginVertical="12dp"
            android:background="#F3F4F6" />
        <LinearLayout
            android:id="@+id/layoutResults"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">
            <TextView
                android:id="@+id/tvResultSummary"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Winner: BJP (300 votes)"
                android:textColor="#374151"
                android:textSize="14sp"
                android:lineSpacingExtra="4dp"/>
        </LinearLayout>
        <LinearLayout
            android:id="@+id/layoutWaiting"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:visibility="gone"
            android:gravity="center"
            android:padding="16dp">
            <ImageView
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:src="@android:drawable/ic_menu_recent_history"
                app:tint="#9CA3AF"
                android:layout_marginBottom="8dp"/>
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:gravity="center"
                android:text="Results will be announced soon"
                android:textColor="#6B7280"
                android:textSize="14sp"
                android:textStyle="italic"/>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_news_admin.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="News Title"
            android:textStyle="bold"
            android:textSize="16sp"
            android:textColor="#000000"/>
        <TextView
            android:id="@+id/tvDate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="2023-10-27"
            android:textSize="12sp"
            android:textColor="#666666"
            android:layout_marginBottom="8dp"/>
        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Description goes here..."
            android:textSize="14sp"
            android:textColor="#333333"
            android:layout_marginBottom="8dp"/>
        <ImageView
            android:id="@+id/imgNews"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:scaleType="centerCrop"
            android:visibility="gone"
            android:layout_marginBottom="8dp"
            android:contentDescription="News Image" />
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="end">
            <Button
                android:id="@+id/btnEdit"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:text="Edit"
                android:textSize="12sp"
                android:layout_marginEnd="8dp"/>
            <Button
                android:id="@+id/btnDelete"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:text="Delete"
                android:textSize="12sp"
                android:backgroundTint="#D32F2F"
                android:textColor="#FFFFFF"/>
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_news_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="6dp"
    app:cardBackgroundColor="#FFFFFF">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="220dp">
            <ImageView
                android:id="@+id/imgNews"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop"
                android:src="@android:drawable/ic_menu_gallery"
                android:background="#E0E0E0"
                android:contentDescription="News Poster" />
            <View
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/gradient_overlay" />
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_gravity="bottom"
                android:orientation="vertical"
                android:padding="16dp">
                <TextView
                    android:id="@+id/tvTitle"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="News Title"
                    android:textColor="#FFFFFF"
                    android:textSize="20sp"
                    android:textStyle="bold"
                    android:shadowColor="#000000"
                    android:shadowDx="1"
                    android:shadowDy="1"
                    android:shadowRadius="2" />
                <TextView
                    android:id="@+id/tvDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Oct 27, 2023"
                    android:textColor="#E0E0E0"
                    android:textSize="12sp"
                    android:layout_marginTop="4dp" />
            </LinearLayout>
        </FrameLayout>
        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp"
            android:text="This is a short description of the news event. It gives users a quick overview of what happened."
            android:textColor="#424242"
            android:textSize="14sp"
            android:lineSpacingExtra="4dp" />
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_notification_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    app:strokeWidth="1dp"
    app:strokeColor="#E5E7EB">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="8dp">
            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@drawable/ic_notifications"
                app:tint="#1E3A8A"
                android:layout_marginEnd="8dp"/>
            <TextView
                android:id="@+id/tvNotificationTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Notification Title"
                android:textSize="16sp"
                android:textStyle="bold"
                android:textColor="#111827"/>
        </LinearLayout>
        <TextView
            android:id="@+id/tvNotificationMessage"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Notification message content goes here..."
            android:textSize="14sp"
            android:textColor="#4B5563"
            android:layout_marginBottom="12dp"
            android:lineSpacingExtra="4dp"/>
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#F3F4F6"
            android:layout_marginBottom="8dp"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">
            <TextView
                android:id="@+id/tvNotificationSender"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="From: System"
                android:textSize="12sp"
                android:textColor="#6B7280"
                android:textStyle="bold"/>
            <View
                android:layout_width="0dp"
                android:layout_height="0dp"
                android:layout_weight="1"/>
            <TextView
                android:id="@+id/tvNotificationDate"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Dec 05, 2025"
                android:textSize="12sp"
                android:textColor="#9CA3AF"/>
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_party_admin.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical">
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">
            <TextView
                android:id="@+id/tvPartyName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Party Name"
                android:textStyle="bold"
                android:textSize="16sp"
                android:textColor="#000000"/>
            <TextView
                android:id="@+id/tvPartySymbol"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Symbol"
                android:textSize="14sp"
                android:textColor="#666666"/>
        </LinearLayout>
        <Button
            android:id="@+id/btnEdit"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:text="Edit"
            android:textSize="12sp"
            android:layout_marginEnd="8dp"/>
        <Button
            android:id="@+id/btnDelete"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:text="Delete"
            android:textSize="12sp"
            android:backgroundTint="#D32F2F"
            android:textColor="#FFFFFF"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_party_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardParty"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:clickable="true"
    android:focusable="true"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="16dp"
    app:cardElevation="6dp"
    app:rippleColor="#E0E0E0"
    app:strokeColor="#F3F4F6"
    app:strokeWidth="1dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="16dp">
        <FrameLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="12dp">
            <View
                android:layout_width="80dp"
                android:layout_height="80dp"
                android:background="@drawable/bg_party_circle"
                android:backgroundTint="#F3F4F6" />
            <ImageView
                android:id="@+id/imgPartyLogo"
                android:layout_width="80dp"
                android:layout_height="80dp"
                android:contentDescription="Party Logo"
                android:padding="12dp"
                android:scaleType="fitCenter"
                android:src="@drawable/ic_bjp" />
        </FrameLayout>
        <TextView
            android:id="@+id/tvPartyName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:ellipsize="end"
            android:gravity="center"
            android:maxLines="2"
            android:text="Party Name"
            android:textColor="#111827"
            android:textSize="16sp"
            android:textStyle="bold" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## item_result_count_row.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:layout_marginBottom="8dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">
        <TextView
            android:id="@+id/tvOptionName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Option Name"
            android:textColor="#374151"
            android:textSize="14sp"/>
        <TextView
            android:id="@+id/tvVoteCount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="0 votes"
            android:textColor="#111827"
            android:textStyle="bold"
            android:textSize="14sp"/>
    </LinearLayout>
    <ProgressBar
        android:id="@+id/progressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="8dp"
        android:layout_marginTop="4dp"
        android:progressDrawable="@drawable/bg_progress_gradient"
        android:progressTint="#1E3A8A"/>
</LinearLayout>
```
---

## item_user_admin.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical">
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">
            <TextView
                android:id="@+id/tvName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="User Name"
                android:textStyle="bold"
                android:textSize="16sp"
                android:textColor="#000000"/>
            <TextView
                android:id="@+id/tvAadhaar"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="1234 5678 9012"
                android:textSize="14sp"
                android:textColor="#666666"/>
        </LinearLayout>
        <Button
            android:id="@+id/btnEdit"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:text="Edit"
            android:textSize="12sp"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_voting_option_admin.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical">
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">
            <TextView
                android:id="@+id/tvOptionName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Option Name"
                android:textStyle="bold"
                android:textSize="16sp"
                android:textColor="#000000"/>
            <TextView
                android:id="@+id/tvOptionDescription"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Description"
                android:textSize="14sp"
                android:textColor="#666666"/>
        </LinearLayout>
        <Button
            android:id="@+id/btnDelete"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:text="Delete"
            android:textSize="12sp"
            android:backgroundTint="#D32F2F"
            android:textColor="#FFFFFF"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```
---

## item_voting_option_card.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardVotingOption"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:clickable="true"
    android:focusable="true"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:strokeColor="#E5E7EB"
    app:strokeWidth="2dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="16dp">
        <ImageView
            android:id="@+id/imgPartyLogo"
            android:layout_width="64dp"
            android:layout_height="64dp"
            android:src="@android:drawable/ic_menu_gallery"
            android:layout_marginRight="16dp"/>
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">
            <TextView
                android:id="@+id/tvCandidateName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Candidate Name"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#111827"/>
            <TextView
                android:id="@+id/tvPartyName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Party Name"
                android:textSize="14sp"
                android:textColor="#6B7280"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        <RadioButton
            android:id="@+id/rbSelect"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:clickable="false"
            android:focusable="false"/>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```
---

## layout_custom_alert.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    android:clipToPadding="false">
    <androidx.cardview.widget.CardView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal|top"
        app:cardCornerRadius="16dp"
        app:cardElevation="8dp"
        app:cardBackgroundColor="#FFFFFF"
        android:minWidth="300dp">
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal">
            <View
                android:id="@+id/viewAlertColor"
                android:layout_width="6dp"
                android:layout_height="match_parent"
                android:background="#2563EB"/>
            <LinearLayout
                android:id="@+id/alertContainer"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="12dp"
                android:gravity="center_vertical">
                <ImageView
                    android:id="@+id/imgAlertIcon"
                    android:layout_width="32dp"
                    android:layout_height="32dp"
                    android:src="@android:drawable/ic_dialog_info"
                    app:tint="#2563EB" />
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical"
                    android:layout_marginStart="12dp"
                    android:layout_marginEnd="8dp">
                    <TextView
                        android:id="@+id/tvAlertTitle"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Alert Title"
                        android:textSize="16sp"
                        android:textStyle="bold"
                        android:textColor="#111827" />
                    <TextView
                        android:id="@+id/tvAlertMessage"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Message content"
                        android:textSize="13sp"
                        android:textColor="#4B5563"
                        android:layout_marginTop="2dp" />
                </LinearLayout>
                <ImageView
                    android:id="@+id/btnCloseAlert"
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/ic_menu_close_clear_cancel"
                    app:tint="#9CA3AF"
                    android:layout_marginStart="4dp"/>
            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>
</FrameLayout>
```
---


# Animations

## fade_in.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<alpha xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:anim/decelerate_interpolator"
    android:fromAlpha="0.0"
    android:toAlpha="1.0"
    android:duration="300" />
```
---

## layout_animation_slide_up.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<layoutAnimation xmlns:android="http://schemas.android.com/apk/res/android"
    android:animation="@anim/slide_up"
    android:animationOrder="normal"
    android:delay="15%" />
```
---

## scale_up.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="200"
    android:interpolator="@android:anim/overshoot_interpolator">
    <scale
        android:fromXScale="0.8"
        android:toXScale="1.0"
        android:fromYScale="0.8"
        android:toYScale="1.0"
        android:pivotX="50%"
        android:pivotY="50%" />
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
</set>
```
---

## slide_in_top.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:fromYDelta="-100%"
    android:toYDelta="0%"
    android:interpolator="@android:anim/decelerate_interpolator" />
```
---

## slide_in_up.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="400"
    android:interpolator="@android:anim/decelerate_interpolator">
    <translate
        android:fromYDelta="50%"
        android:toYDelta="0" />
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
</set>
```
---

## slide_out_top.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:fromYDelta="0%"
    android:toYDelta="-100%"
    android:interpolator="@android:anim/accelerate_interpolator" />
```
---

## slide_up.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="400"
    android:interpolator="@android:anim/decelerate_interpolator">
    <translate
        android:fromYDelta="50%"
        android:toYDelta="0%" />
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
</set>
```
---

## splash_fade_in.xml
```xml
<alpha xmlns:android="http://schemas.android.com/apk/res/android"
    android:fromAlpha="0.0"
    android:toAlpha="1.0"
    android:duration="1200"
    android:interpolator="@android:anim/accelerate_interpolator" />
```
---


# Assets (Data)

## aadhaar_data.json
```json
[
  {
    "aadhaar_id": "234567890123",
    "dob": "1995-03-15",
    "name": "Rajesh Kumar Singh",
    "email": "rajesh.singh@gmail.com",
    "mobile": "9876543210",
    "photo": "rajesh.jpg",
    "address": "Block A, Flat 302, Vasant Vihar Apartments",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400067",
    "eligible": true
  },
  {
    "aadhaar_id": "345678901234",
    "dob": "1988-07-22",
    "name": "Priya Sharma",
    "email": "priya.sharma@yahoo.com",
    "mobile": "9123456789",
    "photo": "priya.jpg",
    "address": "House No. 45, Sector 15, Rohini",
    "city": "New Delhi",
    "state": "Karnataka",
    "pincode": "110085",
    "eligible": true
  },
  {
    "aadhaar_id": "456789012345",
    "dob": "2001-11-08",
    "name": "Mohammed Aamir Khan",
    "email": "aamir.khan@outlook.com",
    "mobile": "8765432109",
    "photo": "aamir.jpg",
    "address": "23/7, Charminar Road, Old City",
    "city": "Hyderabad",
    "state": "Telangana",
    "pincode": "500002",
    "eligible": true
  },
  {
    "aadhaar_id": "112233445566",
    "dob": "1990-01-01",
    "name": "Ramesh Gowda",
    "email": "ramesh.gowda@example.com",
    "mobile": "9900112233",
    "photo": "ramesh.jpg",
    "address": "123, MG Road, Indiranagar",
    "city": "Bengaluru",
    "state": "Karnataka",
    "pincode": "560038",
    "eligible": true
  },
  {
    "aadhaar_id": "556677889900",
    "dob": "2010-05-20",
    "name": "Arjun Reddy",
    "email": "arjun.reddy@example.com",
    "mobile": "8877665544",
    "photo": "arjun.jpg",
    "address": "45, 2nd Cross, Jayanagar",
    "city": "Bengaluru",
    "state": "Karnataka",
    "pincode": "560041",
    "eligible": false
  },
  {
    "aadhaar_id": "667788990011",
    "dob": "1998-12-12",
    "name": "Sneha Patil",
    "email": "sneha.patil@example.com",
    "mobile": "7766554433",
    "photo": "sneha.jpg",
    "address": "89, Main Road, Hubli",
    "city": "Hubli",
    "state": "Karnataka",
    "pincode": "580020",
    "eligible": true
  },
  {
    "aadhaar_id": "778899001122",
    "dob": "2012-08-15",
    "name": "Rahul K",
    "email": "rahul.k@example.com",
    "mobile": "6655443322",
    "photo": "rahul.jpg",
    "address": "12, Temple Street, Mysore",
    "city": "Mysore",
    "state": "Karnataka",
    "pincode": "570001",
    "eligible": false
  },
  {
    "aadhaar_id": "998877665544",
    "dob": "1995-08-15",
    "name": "Rahul Koo",
    "email": "rahul.ii@example.com",
    "mobile": "6655443322",
    "photo": "rahul.jpg",
    "address": "12, Temple Street, sollapur",
    "city": "sollapur",
    "state": "Maharashtra",
    "pincode": "570001",
    "eligible": true
  }
]
```
---

## elections_data.json
```json
[
  {
    "id": 1,
    "title": "Karnataka Assembly Elections 2025",
    "state": "Karnataka",
    "min_age": 18,
    "status": "active",
    "stopDate": "2025-05-31"
  },
  {
    "id": 2,
    "title": "Maharashtra Legislative Assembly Elections 2025",
    "state": "Maharashtra",
    "min_age": 18,
    "status": "active",
    "stopDate": "2025-06-15"
  },
  {
    "id": 3,
    "title": "Delhi Municipal Corporation Elections 2025",
    "state": "Delhi",
    "min_age": 18,
    "status": "active",
    "stopDate": "2025-04-30"
  }
]
```
---

## gov_login_data.json
```json
[
  {
    "dept_code": "ECI-INDIA",
    "password": "eci@2024"
  },
  {
    "dept_code": "KAR-GOVT",
    "password": "kar12345"
  },
  {
    "dept_code": "TN-GOVT",
    "password": "tn2024@vote"
  }
]
```
---

## party_data.json
```json
[
  {
    "party_id": "BJP",
    "name": "Bharatiya Janata Party (BJP)",
    "leader": "Narendra Modi",
    "nominees": ["Amit Shah", "Rajnath Singh", "Nirmala Sitharaman"],
    "founded": "1980",
    "history": "The BJP emerged from the Jana Sangh, founded by Syama Prasad Mukherjee..."
  },
  {
    "party_id": "INC",
    "name": "Indian National Congress (INC)",
    "leader": "Mallikarjun Kharge",
    "nominees": ["Rahul Gandhi", "Priyanka Gandhi", "Sonia Gandhi"],
    "founded": "1885",
    "history": "The Indian National Congress is one of the oldest political parties in India..."
  }
]
```
---

## sample_news.json
```json
[
    {
        "id": "news001",
        "title": "2024 General Elections Announced",
        "description": "The Election Commission of India has announced the schedule for the 2024 General Elections. Voting will take place in 7 phases starting from April 19, 2024. All eligible citizens are encouraged to register and participate in this democratic process.",
        "date": "2024-01-15 10:30",
        "timestamp": 1705311000000,
        "imageUrl": "https://images.unsplash.com/photo-1541872703-74c34d2b0f7c?w=800"
    },
    {
        "id": "news002",
        "title": "New Voter ID Cards with QR Code",
        "description": "The government has introduced new voter ID cards featuring QR codes for enhanced security and faster verification. Existing voters can apply for the updated cards through the official portal. The new cards will help prevent electoral fraud and streamline the voting process.",
        "date": "2024-01-10 14:15",
        "timestamp": 1704896100000,
        "imageUrl": "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=800"
    },
    {
        "id": "news003",
        "title": "Register to Vote - Last Date Extended",
        "description": "Great news for new voters! The last date for voter registration has been extended to February 28, 2024. Don't miss this opportunity to exercise your democratic right. Visit your nearest electoral office or register online through the official website.",
        "date": "2024-01-08 09:00",
        "timestamp": 1704700800000,
        "imageUrl": "https://images.unsplash.com/photo-1495556650867-99590cea3657?w=800"
    },
    {
        "id": "ad001",
        "title": "📢 Know Your Candidate Campaign",
        "description": "Make an informed choice! Visit our candidate information portal to learn about their backgrounds, manifestos, and track records. Democracy thrives on informed voters. Research before you vote!",
        "date": "2024-01-12 16:45",
        "timestamp": 1705074300000,
        "imageUrl": "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=800"
    },
    {
        "id": "news004",
        "title": "Free Transportation for Senior Citizens",
        "description": "Special arrangements have been made to provide free transportation for senior citizens (60+) and differently-abled voters on election day. Contact your local electoral office to avail this facility. Let's ensure everyone can vote!",
        "date": "2024-01-05 11:20",
        "timestamp": 1704448800000,
        "imageUrl": "https://images.unsplash.com/photo-1544027993-37dbfe43562a?w=800"
    },
    {
        "id": "ad002",
        "title": "🗳️ Every Vote Counts!",
        "description": "Your vote is your voice! Don't let others decide your future. Make sure you're registered and ready to vote. Check your name in the electoral roll today. Together, we build a stronger democracy.",
        "date": "2024-01-03 13:30",
        "timestamp": 1704285000000,
        "imageUrl": "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=800"
    },
    {
        "id": "news005",
        "title": "Digital Voting Awareness Campaign",
        "description": "Learn about the EVM (Electronic Voting Machine) and VVPAT (Voter Verifiable Paper Audit Trail) systems. Free demonstrations are being conducted at all district headquarters. Understand how your vote is recorded and counted securely.",
        "date": "2024-01-01 10:00",
        "timestamp": 1704096000000,
        "imageUrl": "https://images.unsplash.com/photo-1495556650867-99590cea3657?w=800"
    }
]
```
---


