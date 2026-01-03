package com.example.smartvotingapp;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import java.io.File;
import java.util.List;

public class ListFragment extends Fragment implements SearchableFragment, PartyManager.PartyUpdateListener {

    private GridLayout gridParties;
    private PartyManager partyManager;
    private List<Party> allParties;

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_list, container, false);

            gridParties = view.findViewById(R.id.gridParties);
            partyManager = new PartyManager(getContext());
            partyManager.addListener(this);

            allParties = partyManager.getAllParties();
            loadParties(allParties);

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(getContext(), "Error loading parties: " + e.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
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
            getActivity().runOnUiThread(() -> {
                allParties = partyManager.getAllParties();
                // Pass current query if we were filtering, but for now just reload all or reset
                // Ideally we should keep track of search query but simple reload is fine
                loadParties(allParties);
            });
        }
    }

    @Override
    public void onSearch(String query) {
        if (allParties == null) {
            allParties = partyManager.getAllParties();
        }

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
        if (parties == null)
            return;

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

        // Add animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(),
                R.anim.slide_in_up);
        animation.setStartOffset(gridParties.getChildCount() * 50); // Staggered effect
        cardView.startAnimation(animation);

        gridParties.addView(cardView);
    }

    private void loadPartyLogo(String filename, ImageView imageView) {
        if (filename == null)
            return;

        try {
            if (filename.startsWith("data:")) {
                // Base64 Image
                String base64 = filename.substring(filename.indexOf(",") + 1);
                byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
                return;
            } else if (filename.startsWith("res:")) {
                String resName = filename.substring(4);
                int resId = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
                if (resId != 0) {
                    imageView.setImageResource(resId);
                } else {
                    imageView.setImageResource(R.drawable.ic_bjp);
                }
                return;
            }

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

        // Make dialog background transparent to show card corners
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
