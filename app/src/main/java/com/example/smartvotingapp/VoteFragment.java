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

public class VoteFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Election> electionList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);

        recyclerView = view.findViewById(R.id.electionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ElectionManager electionManager = new ElectionManager(getContext());
        electionList = electionManager.getAllElections();

        ElectionAdapter adapter = new ElectionAdapter(electionList, this::checkEligibility);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void checkEligibility(Election election) {
        User user = UserUtils.getCurrentUser(getContext());

        if (user == null) {
            Toast.makeText(getContext(), "User data not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = UserUtils.calculateAge(user.getDob());

        // Check if election is active (running, active, or open)
        String status = election.getStatus().toLowerCase();
        if (!status.equals("running") && !status.equals("active") && !status.equals("open")) {
            Toast.makeText(getContext(), "This election is not active (Status: " + election.getStatus() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < election.getMinAge()) {
            Toast.makeText(getContext(),
                    "Not Eligible (Age < " + election.getMinAge() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Trim and compare states (case-insensitive)
        String userState = user.getState().trim();
        String electionState = election.getState().trim();

        android.util.Log.d("VoteFragment",
                "Checking eligibility: User State='" + userState + "', Election State='" + electionState + "'");

        if (!userState.equalsIgnoreCase(electionState)) {
            Toast.makeText(getContext(),
                    "Not Eligible - This election is for " + electionState + " residents only. Your state: "
                            + userState,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Eligible → open VotingActivity
        Intent intent = new Intent(getContext(), VotingActivity.class);
        intent.putExtra("election_id", election.getId());
        intent.putExtra("user_name", user.getName());
        startActivity(intent);
    }
}
