package com.example.smartvotingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VoteFragment extends Fragment
        implements ElectionManager.ElectionUpdateListener, VoteManager.VoteUpdateListener {

    private RecyclerView recyclerView;
    private List<Election> electionList;
    private ElectionAdapter adapter;
    private ElectionManager electionManager;
    private VoteManager voteManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_vote, container, false);

            View guestOverlay = view.findViewById(R.id.guestOverlay);
            View voteContentContainer = view.findViewById(R.id.voteContentContainer);
            com.google.android.material.button.MaterialButton btnLogin = view.findViewById(R.id.btnLoginFromVote);

            User user = UserUtils.getCurrentUser(getContext());
            String userId = user != null ? user.getAadhaarId() : MainActivity.aadhaarId;

            if (userId == null) {
                // GUEST MODE
                guestOverlay.setVisibility(View.VISIBLE);
                voteContentContainer.setVisibility(View.GONE);

                btnLogin.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    // Clear back stack so they can't go back to guest mode easily after login?
                    // Or just normal open. Normal open is fine.
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });

                // Return early or just don't setup recycler view?
                // Ideally we don't setup recycler if hidden.
                return view;
            } else {
                // LOGGED IN
                guestOverlay.setVisibility(View.GONE);
                voteContentContainer.setVisibility(View.VISIBLE);
            }

            recyclerView = view.findViewById(R.id.electionRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            electionManager = new ElectionManager(getContext());
            electionManager.addListener(this);

            voteManager = new VoteManager(getContext());
            voteManager.addListener(this);

            electionList = electionManager.getAllElections();

            adapter = new ElectionAdapter(electionList, voteManager, userId, this::checkEligibility);
            adapter.setOnSelectionChangeListener(count -> {
                if (count > 0) {
                    btnBatchVote.setVisibility(View.VISIBLE);
                    btnBatchVote.setText("Vote (" + count + ")");
                } else {
                    btnBatchVote.setVisibility(View.GONE);
                }
            });
            recyclerView.setAdapter(adapter);

            btnBatchVote = view.findViewById(R.id.btnBatchVote);
            btnBatchVote.setOnClickListener(v -> startBatchVoting());

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading elections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
    }

    private com.google.android.material.button.MaterialButton btnBatchVote;

    private void startBatchVoting() {
        List<Election> selected = adapter.getSelectedElections();
        java.util.ArrayList<Integer> eligibleIds = new java.util.ArrayList<>();
        User user = UserUtils.getCurrentUser(getContext());

        if (user == null)
            return;

        for (Election election : selected) {
            // Re-use logic or duplicate simple checks for batch
            int age = UserUtils.calculateAge(user.getDob());
            String status = election.getStatus().toLowerCase();

            // Check basic eligibility
            if ((status.equals("running") || status.equals("active") || status.equals("open"))
                    && age >= election.getMinAge()
                    && user.getState().trim().equalsIgnoreCase(election.getState().trim())
                    && !voteManager.hasUserVoted(user.getAadhaarId(), election.getId())) {

                eligibleIds.add(election.getId());
            }
        }

        if (eligibleIds.isEmpty()) {
            CustomAlert.showWarning(getContext(), "No Eligible Elections",
                    "None of the selected elections are currently open for you to vote in.");
            return;
        }

        // Start chain
        int firstId = eligibleIds.remove(0);
        Intent intent = new Intent(getContext(), VotingActivity.class);
        intent.putExtra("election_id", firstId);
        intent.putExtra("user_name", user.getName());
        intent.putExtra("next_election_ids", eligibleIds); // Pass ArrayList
        startActivity(intent);

        // Reset selection
        adapter.setMultiSelectMode(false);
    }

    // Maintain existing methods...

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (electionManager != null) {
            electionManager.removeListener(this);
        }
        if (voteManager != null) {
            voteManager.removeListener(this);
        }
    }

    @Override
    public void onElectionsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                electionList.clear();
                electionList.addAll(electionManager.getAllElections());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onVotesUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void checkEligibility(Election election) {
        User user = UserUtils.getCurrentUser(getContext());

        if (user == null) {
            CustomAlert.showError(getContext(), "Error", "User data not found!");
            return;
        }

        if (adapter.isMultiSelectMode()) {
            // In multi-select mode, clicking an item should just toggle it
            // This requires modifying the Adapter's bind logic to call this listener?
            // Actually Adapter handles click for selection internally if isMultiSelectMode
            // is true.
            // So this listener is only called for regular "Vote Now" clicks.
            // But we should double check Adapter logic.
            // Adapter says: if isMultiSelectMode ->
            // holder.itemView.setOnClickListener(toggle)
            // else -> holder.btnVote.setOnClickListener(listener)
            // So this method is only called by the Button in single mode.
        }

        int age = UserUtils.calculateAge(user.getDob());

        // Check if election is active (running, active, or open)
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

        // Trim and compare states (case-insensitive)
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

        // Double check if voted (just in case)
        if (voteManager.hasUserVoted(user.getAadhaarId(), election.getId())) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Election ID: " + election.getId())
                    .setMessage("You have already voted in this election!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // ✅ Eligible → open VotingActivity
        Intent intent = new Intent(getContext(), VotingActivity.class);
        intent.putExtra("election_id", election.getId());
        intent.putExtra("user_name", user.getName());
        startActivity(intent);
    }
}
