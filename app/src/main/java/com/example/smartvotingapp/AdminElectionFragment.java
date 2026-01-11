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
    private String targetId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (getArguments() != null) {
                adminScope = getArguments().getString("admin_scope");
                targetId = getArguments().getString("target_id");
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

            // Tag logic for scrolling
            if (targetId != null && String.valueOf(election.getId()).equals(targetId)) {
                final String tid = targetId;
                electionView.post(() -> {
                    int y = electionView.getTop();
                    if (getView() != null && getView().findViewById(R.id.electionContainer)
                            .getParent() instanceof android.widget.ScrollView) {
                        ((android.widget.ScrollView) getView().findViewById(R.id.electionContainer).getParent())
                                .smoothScrollTo(0, y);
                    } else {
                        electionView.getParent().requestChildFocus(electionView, electionView);
                    }
                    electionView.setBackgroundColor(0x33FFC107);
                    electionView.postDelayed(() -> electionView.setBackgroundColor(0x00000000), 2000);
                });
                targetId = null; // Found it
            }
        }
    }

    private void showAddEditDialog(Election election) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_election, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etTitle);
        android.widget.Spinner spinnerState = view.findViewById(R.id.spinnerState);
        EditText etMinAge = view.findViewById(R.id.etMinAge);
        android.widget.RadioGroup rgStatus = view.findViewById(R.id.rgStatus);
        android.widget.RadioButton rbActive = view.findViewById(R.id.rbActive);
        android.widget.RadioButton rbClosed = view.findViewById(R.id.rbClosed);
        EditText etStopDate = view.findViewById(R.id.etStopDate);

        // Populate State Spinner
        String[] indianStates = {
                "National", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
                "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
                "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
                "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
                "Uttar Pradesh", "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands",
                "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Jammu and Kashmir",
                "Ladakh", "Lakshadweep", "Puducherry"
        };

        android.widget.ArrayAdapter<String> stateAdapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, indianStates);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateAdapter);

        etStopDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (dp, year, month, day) -> {
                etStopDate.setText(year + "-" + (month + 1) + "-" + day);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (election != null) {
            etTitle.setText(election.getTitle());

            // Set Spinner Selection
            String currentState = election.getState();
            for (int i = 0; i < indianStates.length; i++) {
                if (indianStates[i].equalsIgnoreCase(currentState)) {
                    spinnerState.setSelection(i);
                    break;
                }
            }

            etMinAge.setText(String.valueOf(election.getMinAge()));

            // Set status radio button
            if (election.getStatus().equalsIgnoreCase("Active") ||
                    election.getStatus().equalsIgnoreCase("Running") ||
                    election.getStatus().equalsIgnoreCase("Open")) {
                rbActive.setChecked(true);
            } else {
                rbClosed.setChecked(true);
            }

            etStopDate.setText(election.getStopDate());
        } else {
            // New Election: Pre-fill and lock state if scoped
            if (adminScope != null && !adminScope.isEmpty()) {
                for (int i = 0; i < indianStates.length; i++) {
                    if (indianStates[i].equalsIgnoreCase(adminScope)) {
                        spinnerState.setSelection(i);
                        spinnerState.setEnabled(false);
                        break;
                    }
                }
            }
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String state = spinnerState.getSelectedItem().toString(); // Get from Spinner
            String minAgeStr = etMinAge.getText().toString().trim();
            String status = rbActive.isChecked() ? "Active" : "Closed";
            String stopDate = etStopDate.getText().toString().trim();

            if (title.isEmpty() || minAgeStr.isEmpty()) {
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
        LinearLayout optionsContainer = view.findViewById(R.id.optionsContainer);

        // New Inline Add Form Views
        android.widget.Spinner spinnerNewParty = view.findViewById(R.id.spinnerNewParty);
        EditText etNewCandidateName = view.findViewById(R.id.etNewCandidateName);
        EditText etNewPartyName = view.findViewById(R.id.etNewPartyName);
        Button btnQuickAdd = view.findViewById(R.id.btnQuickAdd);

        tvElectionTitle.setText("Manage Options: " + election.getTitle());

        VotingOptionManager optionManager = new VotingOptionManager(getContext());
        PartyManager partyManager = new PartyManager(getContext());

        // Setup Spinner
        final List<Party> parties = new java.util.ArrayList<>();
        final List<String> partyNames = new java.util.ArrayList<>();
        final android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, partyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNewParty.setAdapter(adapter);

        // Spinner Selection Logic
        final String[] selectedLogoPath = { null };
        spinnerNewParty.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= parties.size()) {
                    Party selectedParty = parties.get(position - 1);
                    etNewPartyName.setText(selectedParty.getName());
                    selectedLogoPath[0] = selectedParty.getLogoPath();
                } else {
                    // "Select Party" selected
                    // Don't clear manually entered text if user is just switching back
                    if (selectedLogoPath[0] != null) { // Only clear if we previously selected a party
                        etNewPartyName.setText("");
                        selectedLogoPath[0] = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Quick Add Button Logic
        btnQuickAdd.setOnClickListener(v -> {
            String candidateName = etNewCandidateName.getText().toString().trim();
            String partyName = etNewPartyName.getText().toString().trim();

            if (candidateName.isEmpty()) {
                Toast.makeText(getContext(), "Candidate name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (partyName.isEmpty()) {
                Toast.makeText(getContext(), "Party name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = java.util.UUID.randomUUID().toString();
            VotingOption option = new VotingOption(id, election.getId(), candidateName, partyName, selectedLogoPath[0]);
            optionManager.addOption(option);

            // Reset form
            etNewCandidateName.setText("");
            if (spinnerNewParty.getSelectedItemPosition() > 0) {
                // Keep party selected? User might want to add multiple candidates for same
                // party?
                // Or reset? Let's reset for clarity.
                spinnerNewParty.setSelection(0);
                etNewPartyName.setText("");
            } else {
                etNewPartyName.setText("");
            }
            selectedLogoPath[0] = null;

            Toast.makeText(getContext(), "Option Added", Toast.LENGTH_SHORT).show();
        });

        // Create listener for real-time voting options updates
        VotingOptionManager.VotingOptionUpdateListener optionListener = new VotingOptionManager.VotingOptionUpdateListener() {
            @Override
            public void onVotingOptionsUpdated() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        optionsContainer.removeAllViews();
                        List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

                        if (options.isEmpty()) {
                            TextView emptyView = new TextView(getContext());
                            emptyView.setText("No options added yet.");
                            emptyView.setPadding(20, 20, 20, 20);
                            emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            optionsContainer.addView(emptyView);
                        } else {
                            for (VotingOption option : options) {
                                View optionView = LayoutInflater.from(getContext()).inflate(
                                        R.layout.item_voting_option_admin, optionsContainer, false);

                                TextView tvOptionName = optionView.findViewById(R.id.tvOptionName);
                                TextView tvOptionDescription = optionView.findViewById(R.id.tvOptionDescription);
                                Button btnDelete = optionView.findViewById(R.id.btnDelete);
                                Button btnEdit = optionView.findViewById(R.id.btnEdit);

                                tvOptionName.setText(option.getOptionName());
                                tvOptionDescription.setText(option.getDescription());

                                btnDelete.setOnClickListener(v -> {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Delete Option")
                                            .setMessage(
                                                    "Are you sure you want to delete " + option.getOptionName() + "?")
                                            .setPositiveButton("Delete",
                                                    (d, w) -> optionManager.deleteOption(option.getId()))
                                            .setNegativeButton("Cancel", null)
                                            .show();
                                });

                                if (btnEdit != null) {
                                    btnEdit.setOnClickListener(v -> {
                                        showAddVotingOptionDialog(election, optionManager, partyManager, option);
                                    });
                                }

                                optionsContainer.addView(optionView);
                            }
                        }
                    });
                }
            }
        };

        // Create listener for real-time party updates
        PartyManager.PartyUpdateListener partyListener = () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    parties.clear();
                    partyNames.clear();
                    partyNames.add("Select Party (Optional)");
                    List<Party> loaded = partyManager.getAllParties();
                    if (loaded != null) {
                        parties.addAll(loaded);
                        for (Party p : loaded)
                            partyNames.add(p.getName());
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        };

        optionManager.addListener(optionListener);
        partyManager.addListener(partyListener);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> {
            optionManager.removeListener(optionListener);
            partyManager.removeListener(partyListener);
        });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done", (d, which) -> {
        });
        dialog.show();
    }

    private void showAddVotingOptionDialog(Election election, VotingOptionManager optionManager,
            PartyManager partyManager, VotingOption existingOption) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_voting_option, null);
        builder.setView(view);
        builder.setTitle(existingOption == null ? "Add Voting Option" : "Edit Voting Option");

        // UI Elements
        android.widget.Spinner spinnerParties = view.findViewById(R.id.spinnerParties);
        EditText etOptionName = view.findViewById(R.id.etOptionName);
        EditText etOptionDescription = view.findViewById(R.id.etOptionDescription);

        // Create lists for party data
        final List<Party> parties = new java.util.ArrayList<>();
        final List<String> partyNames = new java.util.ArrayList<>();

        // Create adapter with custom layout
        final android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(),
                R.layout.spinner_party_item, partyNames);
        adapter.setDropDownViewResource(R.layout.spinner_party_item);
        spinnerParties.setAdapter(adapter);

        // Function to load parties
        final Runnable loadParties = () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    parties.clear();
                    partyNames.clear();

                    partyNames.add("-- Select a Party --");

                    List<Party> allParties = partyManager.getAllParties();
                    if (allParties != null && !allParties.isEmpty()) {
                        parties.addAll(allParties);
                        for (Party p : allParties) {
                            String displayName = p.getName();
                            if (p.getSymbol() != null && !p.getSymbol().isEmpty()) {
                                displayName = p.getSymbol() + " " + p.getName();
                            }
                            partyNames.add(displayName);
                        }
                    } else {
                        partyNames.add("No parties available - Add parties in Parties fragment");
                    }

                    adapter.notifyDataSetChanged();
                });
            }
        };

        // Load parties initially
        loadParties.run();

        // Add listener for real-time party updates
        PartyManager.PartyUpdateListener partyListener = () -> {
            loadParties.run();
        };
        partyManager.addListener(partyListener);

        // Pre-fill if editing
        final String[] selectedLogoPath = { null };
        final String[] selectedPartyId = { null };

        if (existingOption != null) {
            etOptionName.setText(existingOption.getOptionName());
            etOptionDescription.setText(existingOption.getDescription());
            selectedLogoPath[0] = existingOption.getLogoPath();

            // Try to find and select the matching party by name
            String existingPartyName = existingOption.getDescription();
            if (existingPartyName != null && !existingPartyName.isEmpty()) {
                // Determine selection index
                // Note: parties list might update async, but since we passed partyManager with
                // data,
                // loadParties.run() populated 'parties' list synchronously on UI thread if data
                // was ready.
                for (int i = 0; i < parties.size(); i++) {
                    if (parties.get(i).getName().equals(existingPartyName)) {
                        spinnerParties.setSelection(i + 1); // +1 because of header
                        selectedPartyId[0] = parties.get(i).getId();
                        break;
                    }
                }
            }
        }

        // Handle Party Selection
        spinnerParties.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= parties.size()) {
                    // Valid party selected
                    Party selectedParty = parties.get(position - 1);

                    // Auto-fill details
                    etOptionDescription.setText(selectedParty.getName());
                    selectedLogoPath[0] = selectedParty.getLogoPath();
                    selectedPartyId[0] = selectedParty.getId();

                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Selected: " + selectedParty.getName(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Invalid/Header selected
                    if (existingOption == null) {
                        etOptionDescription.setText("");
                        selectedLogoPath[0] = null;
                        selectedPartyId[0] = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Set buttons - Positive is null initially to override later
        builder.setPositiveButton(existingOption == null ? "Add" : "Update", null);
        builder.setNegativeButton("Cancel", (d, which) -> {
            partyManager.removeListener(partyListener);
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> partyManager.removeListener(partyListener));
        dialog.show();

        // Override Positive Button to prevent auto-dismiss on error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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

            // Only enforce strictly selected party if it's not an edit or if logic demands
            // it.
            // For now, if partyName is filled but selectedPartyId is null, it might be
            // manual entry (if enabled)
            // but we disabled manual entry. So valid selection is required.
            if (selectedPartyId[0] == null && existingOption == null) {
                // Try to see if partyName matches any loaded party (in case of re-selection
                // logic weirdness)
                boolean matchFound = false;
                for (Party p : parties) {
                    if (p.getName().equalsIgnoreCase(partyName)) {
                        selectedLogoPath[0] = p.getLogoPath();
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    Toast.makeText(getContext(), "Please select a valid party from the list", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }

            if (existingOption == null) {
                String id = java.util.UUID.randomUUID().toString();
                VotingOption option = new VotingOption(id, election.getId(), candidateName, partyName,
                        selectedLogoPath[0]);
                optionManager.addOption(option);
                Toast.makeText(getContext(), "Voting option added", Toast.LENGTH_SHORT).show();
            } else {
                VotingOption updatedOption = new VotingOption(existingOption.getId(), election.getId(), candidateName,
                        partyName, selectedLogoPath[0]);
                optionManager.updateOption(updatedOption);
                Toast.makeText(getContext(), "Voting option updated", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
    }
}
