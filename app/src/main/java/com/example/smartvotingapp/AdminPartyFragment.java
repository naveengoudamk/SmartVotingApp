package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.List;
import java.util.UUID;

public class AdminPartyFragment extends Fragment {

    private LinearLayout partyContainer;
    private PartyManager partyManager;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_party, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etPartyName);
        EditText etSymbol = view.findViewById(R.id.etPartySymbol);
        EditText etDescription = view.findViewById(R.id.etPartyDescription);

        if (party != null) {
            etName.setText(party.getName());
            etSymbol.setText(party.getSymbol());
            etDescription.setText(party.getDescription());
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String symbol = etSymbol.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Party name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (party == null) {
                Party newParty = new Party(UUID.randomUUID().toString(), name, symbol, description);
                partyManager.addParty(newParty);
            } else {
                Party updatedParty = new Party(party.getId(), name, symbol, description);
                partyManager.updateParty(updatedParty);
            }
            loadParties();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
