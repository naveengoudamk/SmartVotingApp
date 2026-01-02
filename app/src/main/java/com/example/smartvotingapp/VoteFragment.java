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

public class VoteFragment extends Fragment implements ElectionManager.ElectionUpdateListener {

    private RecyclerView recyclerView;
    private List<Election> electionList;
    private ElectionAdapter adapter;
    private ElectionManager electionManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_vote, container, false);

            recyclerView = view.findViewById(R.id.electionRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            electionManager = new ElectionManager(getContext());
            electionManager.addListener(this);

            electionList = electionManager.getAllElections();

            adapter = new ElectionAdapter(electionList, this::checkEligibility);
            recyclerView.setAdapter(adapter);

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading elections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext());
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
            getActivity().runOnUiThread(() -> {
                electionList.clear();
                electionList.addAll(electionManager.getAllElections());
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

        // ✅ Eligible → open VotingActivity
        Intent intent = new Intent(getContext(), VotingActivity.class);
        intent.putExtra("election_id", election.getId());
        intent.putExtra("user_name", user.getName());
        startActivity(intent);
    }
}
