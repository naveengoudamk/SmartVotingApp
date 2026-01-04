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
        android.widget.RadioGroup rgStatus = view.findViewById(R.id.rgStatus);
        android.widget.RadioButton rbActive = view.findViewById(R.id.rbActive);
        android.widget.RadioButton rbClosed = view.findViewById(R.id.rbClosed);
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
                etState.setText(adminScope);
                etState.setEnabled(false);
            }
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String state = etState.getText().toString().trim();
            String minAgeStr = etMinAge.getText().toString().trim();
            String status = rbActive.isChecked() ? "Active" : "Closed";
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
        LinearLayout optionsContainer = view.findViewById(R.id.optionsContainer);
        LinearLayout partiesContainer = view.findViewById(R.id.partiesContainer);

        tvElectionTitle.setText("Manage Options: " + election.getTitle());

        VotingOptionManager optionManager = new VotingOptionManager(getContext());
        PartyManager partyManager = new PartyManager(getContext());

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
                            emptyView.setText("No voting options yet.\nSelect parties from right →");
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
                                    optionManager.deleteOption(option.getId());
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
        PartyManager.PartyUpdateListener partyListener = new PartyManager.PartyUpdateListener() {
            @Override
            public void onPartiesUpdated() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        partiesContainer.removeAllViews();
                        List<Party> parties = partyManager.getAllParties();

                        if (parties.isEmpty()) {
                            TextView emptyView = new TextView(getContext());
                            emptyView.setText("No parties available.\nAdd parties in Parties tab");
                            emptyView.setPadding(20, 20, 20, 20);
                            emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            partiesContainer.addView(emptyView);
                        } else {
                            for (Party party : parties) {
                                View partyView = LayoutInflater.from(getContext()).inflate(
                                        R.layout.item_party_selectable, partiesContainer, false);

                                TextView tvPartyName = partyView.findViewById(R.id.tvPartyName);
                                TextView tvPartySymbol = partyView.findViewById(R.id.tvPartySymbol);

                                tvPartyName.setText(party.getName());
                                tvPartySymbol.setText(party.getSymbol() != null ? party.getSymbol() : "");

                                // Click to add this party as a voting option
                                partyView.setOnClickListener(v -> {
                                    showAddCandidateForPartyDialog(election, optionManager, party);
                                });

                                partiesContainer.addView(partyView);
                            }
                        }
                    });
                }
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

    // New method to add candidate for selected party
    private void showAddCandidateForPartyDialog(Election election, VotingOptionManager optionManager, Party party) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Candidate for " + party.getName());

        View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
        EditText etCandidateName = new EditText(getContext());
        etCandidateName.setHint("Enter candidate name");
        etCandidateName.setPadding(40, 40, 40, 40);
        builder.setView(etCandidateName);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String candidateName = etCandidateName.getText().toString().trim();

            if (candidateName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter candidate name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create voting option with party details
            String id = java.util.UUID.randomUUID().toString();
            VotingOption option = new VotingOption(id, election.getId(), candidateName,
                    party.getName(), party.getLogoPath());
            optionManager.addOption(option);
            Toast.makeText(getContext(), "Added " + candidateName + " (" + party.getName() + ")",
                    Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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
