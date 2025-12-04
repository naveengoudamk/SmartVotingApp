package com.example.smartvotingapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;

public class ListFragment extends Fragment {

    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        setupPartyCard(view, R.id.cardBJP, "Bharatiya Janata Party (BJP)", "Lotus",
                "The Bharatiya Janata Party is one of two major political parties in India, along with the Indian National Congress. It is the ruling political party of the Republic of India since 2014.",
                R.drawable.img_bjp);

        setupPartyCard(view, R.id.cardINC, "Indian National Congress (INC)", "Hand",
                "The Indian National Congress is a political party in India with widespread roots. Founded in 1885, it was the first modern nationalist movement to emerge in the British Empire in Asia and Africa.",
                R.drawable.img_inc);

        setupPartyCard(view, R.id.cardJDS, "Janata Dal (Secular) (JDS)", "Lady Farmer carrying Paddy",
                "Janata Dal (Secular) is an Indian regional political party recognized as a State Party in the states of Karnataka, Kerala and Arunachal Pradesh.",
                R.drawable.img_jds);

        setupPartyCard(view, R.id.cardAAP, "Aam Aadmi Party (AAP)", "Broom",
                "The Aam Aadmi Party is a political party in India. It was founded in November 2012 by Arvind Kejriwal and his companions.",
                R.drawable.ic_aap);

        setupPartyCard(view, R.id.cardTMC, "Trinamool Congress (TMC)", "Flowers & Grass",
                "All India Trinamool Congress is an Indian political party which is predominantly active in West Bengal.",
                R.drawable.ic_tmc);

        setupPartyCard(view, R.id.cardDMK, "Dravida Munnetra Kazhagam (DMK)", "Rising Sun",
                "Dravida Munnetra Kazhagam is a political party in India, particularly in the state of Tamil Nadu and the union territory of Puducherry.",
                R.drawable.ic_dmk);

        setupPartyCard(view, R.id.cardAIADMK, "AIADMK", "Two Leaves",
                "All India Anna Dravida Munnetra Kazhagam is an Indian regional political party with great influence in the state of Tamil Nadu and union territory of Puducherry.",
                R.drawable.ic_aiadmk);

        setupPartyCard(view, R.id.cardSP, "Samajwadi Party (SP)", "Bicycle",
                "The Samajwadi Party is a socialist political party in India, headquartered in New Delhi and also a recognised state party in Uttar Pradesh.",
                R.drawable.ic_sp);

        setupPartyCard(view, R.id.cardBSP, "Bahujan Samaj Party (BSP)", "Elephant",
                "The Bahujan Samaj Party is a national level political party in India that was formed to represent the Bahujans, referring to Scheduled Castes, Scheduled Tribes, and Other Backward Classes.",
                R.drawable.ic_bsp);

        return view;
    }

    private void setupPartyCard(View view, int cardId, String name, String symbol, String description, int logoResId) {
        MaterialCardView card = view.findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> showPartyDetails(name, symbol, description, logoResId));
        }
    }

    private void showPartyDetails(String name, String symbol, String description, int logoResId) {
        if (getContext() == null)
            return;

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_party_details);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView imgLogo = dialog.findViewById(R.id.imgPartyLogo);
        TextView tvName = dialog.findViewById(R.id.tvPartyName);
        TextView tvSymbol = dialog.findViewById(R.id.tvPartySymbol);
        TextView tvDesc = dialog.findViewById(R.id.tvPartyDescription);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        imgLogo.setImageResource(logoResId);
        tvName.setText(name);
        tvSymbol.setText("Symbol: " + symbol);
        tvDesc.setText(description);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
