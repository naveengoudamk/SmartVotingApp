package com.example.smartvotingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class AdminPartyFragment extends Fragment implements PartyManager.PartyUpdateListener {

    private LinearLayout partyContainer;
    private PartyManager partyManager;
    private androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher;
    private ImageView currentLogoPreview;
    private String tempBase64Image;

    public AdminPartyFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Convert to Base64
                            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

                            // Resize if too big (max 500x500 for logos)
                            int width = originalBitmap.getWidth();
                            int height = originalBitmap.getHeight();
                            float maxSz = 500f;
                            if (width > maxSz || height > maxSz) {
                                float ratio = Math.min(maxSz / width, maxSz / height);
                                width = (int) (width * ratio);
                                height = (int) (height * ratio);
                                originalBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
                            }

                            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream.toByteArray();
                            String base64Image = "data:image/jpeg;base64,"
                                    + android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);

                            // Store internally
                            tempBase64Image = base64Image;

                            if (currentLogoPreview != null) {
                                currentLogoPreview.setImageBitmap(originalBitmap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_party, container, false);

        partyManager = new PartyManager(getContext());
        partyContainer = view.findViewById(R.id.partyContainer);
        Button btnAddParty = view.findViewById(R.id.btnAddParty);

        // Add listener AFTER views are initialized to avoid NPE in callback
        partyManager.addListener(this);

        btnAddParty.setOnClickListener(v -> showAddEditDialog(null));

        loadParties();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (partyManager != null) {
            partyManager.removeListener(this);
        }
    }

    @Override
    public void onPartiesUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadParties);
        }
    }

    private void loadParties() {
        partyContainer.removeAllViews();
        List<Party> parties = partyManager.getAllParties();

        for (Party party : parties) {
            View partyView = LayoutInflater.from(getContext()).inflate(R.layout.item_party_admin, partyContainer,
                    false);

            TextView name = partyView.findViewById(R.id.tvPartyName);
            TextView symbol = partyView.findViewById(R.id.tvPartySymbol);
            ImageView logo = partyView.findViewById(R.id.ivPartyLogo);
            Button btnEdit = partyView.findViewById(R.id.btnEdit);
            Button btnDelete = partyView.findViewById(R.id.btnDelete);

            name.setText(party.getName());
            symbol.setText("Symbol: " + party.getSymbol());

            if (party.getLogoPath() != null) {
                loadImageToView(party.getLogoPath(), logo);
            }

            btnEdit.setOnClickListener(v -> showAddEditDialog(party));
            btnDelete.setOnClickListener(v -> {
                partyManager.deleteParty(party.getId());
                // loadParties(); // Handled by listener
            });

            partyContainer.addView(partyView);
        }
    }

    private void showAddEditDialog(Party party) {
        tempBase64Image = null; // Reset
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

            // Load existing logo if available
            if (party.getLogoPath() != null) {
                // If it's a Base64 string, set it as temp default so we don't lose it on save
                // unless replaced
                if (party.getLogoPath().startsWith("data:")) {
                    tempBase64Image = party.getLogoPath();
                }
                loadImageToView(party.getLogoPath(), currentLogoPreview);
            }
        }

        btnSelectLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String symbol = etSymbol.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Party name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use tempBase64Image if set (new pick or existing base64), else fall back to
            // existing path (res: or file path)
            String logoPath = tempBase64Image;
            if (logoPath == null && party != null) {
                logoPath = party.getLogoPath();
            }

            if (party == null) {
                Party newParty = new Party(UUID.randomUUID().toString(), name, symbol, description, logoPath);
                partyManager.addParty(newParty);
            } else {
                Party updatedParty = new Party(party.getId(), name, symbol, description, logoPath);
                partyManager.updateParty(updatedParty);
            }
            // loadParties(); // Handled by listener
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadImageToView(String path, ImageView imageView) {
        if (path == null)
            return;

        try {
            if (path.startsWith("data:")) {
                String base64 = path.substring(path.indexOf(",") + 1);
                byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } else if (path.startsWith("res:")) {
                String resName = path.substring(4);
                int resId = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
                if (resId != 0)
                    imageView.setImageResource(resId);
            } else {
                // Fallback for legacy local files
                File file = new File(getContext().getFilesDir(), path);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
