package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AdminElectionFragment extends Fragment implements ElectionManager.ElectionUpdateListener {

    private LinearLayout electionContainer;
    private ElectionManager electionManager;

    public AdminElectionFragment() {
    }

    private String adminScope;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (getArguments() != null) {
                adminScope = getArguments().getString("admin_scope");
            }
            View view = inflater.inflate(R.layout.fragment_admin_election, container, false);

            electionManager = new ElectionManager(getContext());
            electionManager.addListener(this);

            electionContainer = view.findViewById(R.id.electionContainer);
            Button btnAddElection = view.findViewById(R.id.btnAddElection);

            btnAddElection.setOnClickListener(v -> showAddEditDialog(null));

            loadElections();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading elections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext()); // Return dummy view to prevent crash
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (electionManager != null) {
            electionManager.removeListener(this);
        }
    }

    @Override
    public void onElectionsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadElections);
        }
    }

    private void loadElections() {
        electionContainer.removeAllViews();
        List<Election> elections = electionManager.getAllElections();

        for (Election election : elections) {
            // FILTER: Scope Logic
            if (adminScope != null && !adminScope.isEmpty()) {
                if (!election.getState().equalsIgnoreCase(adminScope)) {
                    continue;
                }
            }

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
            // Only allow deletions if they match scope (Redundant check but safe)
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
        } else {
            // New Election: Pre-fill and lock state if scoped
            if (adminScope != null && !adminScope.isEmpty()) {
                etState.setText(adminScope);
                etState.setEnabled(false);
            }
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
                // Add new
                int id = new Random().nextInt(10000);
                Election newElection = new Election(id, title, state, minAge, status, stopDate);
                electionManager.addElection(newElection);
            } else {
                // Update existing
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

        // Load and display options - use array wrapper for effectively final
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

        // UI Elements
        android.widget.Spinner spinnerParties = view.findViewById(R.id.spinnerParties);
        EditText etOptionName = view.findViewById(R.id.etOptionName);
        EditText etOptionDescription = view.findViewById(R.id.etOptionDescription);

        // Load Parties
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

        // Handle Selection
        final String[] selectedLogoPath = { null };
        spinnerParties.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Party selectedParty = parties.get(position - 1);
                    // Set Party Name in Description field
                    etOptionDescription.setText(selectedParty.getName());
                    // Clear Candidate Name for user input
                    // etOptionName.setText(""); // Optional: keep empty or pre-fill
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
            // Option Name = Candidate Name
            // Description = Party Name
            VotingOption option = new VotingOption(id, election.getId(), candidateName, partyName, selectedLogoPath[0]);
            optionManager.addOption(option);
            onComplete.run();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
