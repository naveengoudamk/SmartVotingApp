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

            // Load existing logo if available
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
