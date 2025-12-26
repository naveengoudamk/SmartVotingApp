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

        if (filename.startsWith("res:")) {
            String resName = filename.substring(4);
            int resId = getResources().getIdentifier(resName, "drawable", getContext().getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
            } else {
                // Fallback if resource not found
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
                // Try to load from drawable if it matches a known party code/name (optional
                // fallback logic)
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
