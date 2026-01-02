package com.example.smartvotingapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AdminResultFragment extends Fragment
        implements VoteManager.VoteUpdateListener, ElectionManager.ElectionUpdateListener {

    private LinearLayout resultsContainer;
    private ElectionManager electionManager;
    private VoteManager voteManager;
    private VotingOptionManager optionManager;

    public AdminResultFragment() {
    }

    private String adminScope;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (getArguments() != null) {
                adminScope = getArguments().getString("admin_scope");
            }
            View view = inflater.inflate(R.layout.fragment_admin_result, container, false);

            resultsContainer = view.findViewById(R.id.resultsContainer);
            electionManager = new ElectionManager(getContext());
            electionManager.addListener(this);

            voteManager = new VoteManager(getContext());
            voteManager.addListener(this);

            optionManager = new VotingOptionManager(getContext());

            loadElections();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return new View(getContext()); // Return dummy view to prevent crash
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (voteManager != null) {
            voteManager.removeListener(this);
        }
        if (electionManager != null) {
            electionManager.removeListener(this);
        }
    }

    @Override
    public void onVotesUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadElections);
        }
    }

    @Override
    public void onElectionsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadElections);
        }
    }

    private void loadElections() {
        if (resultsContainer == null)
            return;
        resultsContainer.removeAllViews();
        List<Election> elections = electionManager.getAllElections();

        if (elections.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No elections found.");
            emptyView.setPadding(32, 32, 32, 32);
            resultsContainer.addView(emptyView);
            return;
        }

        for (Election election : elections) {
            // FILTER: Scope Logic
            if (adminScope != null && !adminScope.isEmpty()) {
                if (!election.getState().equalsIgnoreCase(adminScope)) {
                    continue;
                }
            }

            View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_result_card, resultsContainer,
                    false);

            TextView tvTitle = cardView.findViewById(R.id.tvTitle);
            TextView tvStatus = cardView.findViewById(R.id.tvStatus);
            TextView tvResultDate = cardView.findViewById(R.id.tvResultDate);
            Button btnSetDate = cardView.findViewById(R.id.btnSetDate);
            Button btnAnnounce = cardView.findViewById(R.id.btnAnnounce);
            LinearLayout layoutCounts = cardView.findViewById(R.id.layoutCounts);

            tvTitle.setText(election.getTitle());
            tvStatus.setText("Status: " + election.getStatus());

            String date = election.getResultDate();
            tvResultDate.setText(date != null ? "Results Date: " + date : "Results Date: Not Set");

            // Disable announce button if already announced
            if ("Results Announced".equalsIgnoreCase(election.getStatus())) {
                btnAnnounce.setText("Announced");
                btnAnnounce.setEnabled(false);
            } else {
                btnAnnounce.setText("Announce");
                btnAnnounce.setEnabled(true);
            }

            btnSetDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (dp, year, month, day) -> {
                    String newDate = year + "-" + (month + 1) + "-" + day;
                    election.setResultDate(newDate);
                    electionManager.updateElection(election);
                    loadElections(); // Refresh
                    Toast.makeText(getContext(), "Result date updated", Toast.LENGTH_SHORT).show();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });

            btnAnnounce.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Announce Results")
                        .setMessage("Are you sure you want to announce the results for " + election.getTitle()
                                + "? This will make the results visible to all users.")
                        .setPositiveButton("Announce", (dialog, which) -> {
                            election.setStatus("Results Announced");
                            // If no date set, set today
                            if (election.getResultDate() == null || election.getResultDate().isEmpty()) {
                                String today = new java.text.SimpleDateFormat("yyyy-M-d", java.util.Locale.getDefault())
                                        .format(new java.util.Date());
                                election.setResultDate(today);
                            }
                            electionManager.updateElection(election);
                            loadElections();
                            Toast.makeText(getContext(), "Results announced successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // Populate Counts
            Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
            List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

            layoutCounts.removeAllViews();
            if (options.isEmpty()) {
                TextView tvNoOptions = new TextView(getContext());
                tvNoOptions.setText("No candidates configured.");
                layoutCounts.addView(tvNoOptions);
            } else {
                int totalVotes = 0;
                for (VotingOption option : options) {
                    totalVotes += voteCounts.getOrDefault(option.getId(), 0);
                }

                for (VotingOption option : options) {
                    View countView = LayoutInflater.from(getContext()).inflate(R.layout.item_result_count_row,
                            layoutCounts, false);
                    TextView tvName = countView.findViewById(R.id.tvOptionName);
                    TextView tvCount = countView.findViewById(R.id.tvVoteCount);
                    android.widget.ProgressBar progressBar = countView.findViewById(R.id.progressBar);

                    int count = voteCounts.getOrDefault(option.getId(), 0);
                    tvName.setText(option.getOptionName());
                    tvCount.setText(count + " votes");

                    progressBar.setMax(totalVotes > 0 ? totalVotes : 1);
                    progressBar.setProgress(count);

                    layoutCounts.addView(countView);
                }

                TextView tvTotal = new TextView(getContext());
                tvTotal.setText("Total Votes: " + totalVotes);
                tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTotal.setPadding(0, 16, 0, 0);
                layoutCounts.addView(tvTotal);
            }

            resultsContainer.addView(cardView);
        }
    }
}
