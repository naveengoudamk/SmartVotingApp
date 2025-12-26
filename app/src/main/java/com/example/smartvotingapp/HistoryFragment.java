package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_history, container, false);

            rvHistory = view.findViewById(R.id.rvHistory);
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

            loadHistory();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(getContext(), "Error loading history: " + e.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
    }

    private void loadHistory() {
        ElectionManager electionManager = new ElectionManager(getContext());
        List<Election> elections = electionManager.getAllElections();

        HistoryAdapter adapter = new HistoryAdapter(elections, getContext());
        rvHistory.setAdapter(adapter);
    }

    private String readFile(String filename) {
        File file = new File(getContext().getFilesDir(), filename);
        if (!file.exists())
            return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    // Inner Adapter Class
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private List<Election> elections;
        private VoteManager voteManager;
        private VotingOptionManager optionManager;

        public HistoryAdapter(List<Election> elections, android.content.Context context) {
            this.elections = elections;
            this.voteManager = new VoteManager(context);
            this.optionManager = new VotingOptionManager(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Election election = elections.get(position);
            holder.tvTitle.setText(election.getTitle());

            boolean isDeclared = false;

            // Check if this election has a result date set
            String resultDateStr = election.getResultDate();
            Date resultDate = null;

            if (resultDateStr != null && !resultDateStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    resultDate = sdf.parse(resultDateStr);

                    // Get today's date at midnight for proper comparison
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    today.set(java.util.Calendar.MINUTE, 0);
                    today.set(java.util.Calendar.SECOND, 0);
                    today.set(java.util.Calendar.MILLISECOND, 0);

                    // Get result date at midnight
                    java.util.Calendar resultCal = java.util.Calendar.getInstance();
                    resultCal.setTime(resultDate);
                    resultCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    resultCal.set(java.util.Calendar.MINUTE, 0);
                    resultCal.set(java.util.Calendar.SECOND, 0);
                    resultCal.set(java.util.Calendar.MILLISECOND, 0);

                    // Results are declared if today is on or after the result date
                    if (!today.before(resultCal)) {
                        isDeclared = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isDeclared && resultDate != null) {
                holder.tvStatusBadge.setText("Declared");
                holder.tvStatusBadge.setTextColor(0xFF059669); // Green
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active); // Reuse green bg
                holder.tvDate.setText("Results declared on: "
                        + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(resultDate));

                holder.layoutResults.setVisibility(View.VISIBLE);
                holder.layoutWaiting.setVisibility(View.GONE);

                // Calculate and show results
                Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
                List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

                if (options.isEmpty()) {
                    holder.tvResultSummary.setText("No candidates/options found.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    String winner = "N/A";
                    int maxVotes = -1;

                    for (VotingOption option : options) {
                        int count = voteCounts.getOrDefault(option.getId(), 0);
                        if (count > maxVotes) {
                            maxVotes = count;
                            winner = option.getOptionName();
                        }
                        sb.append("• ").append(option.getOptionName()).append(": ").append(count).append("\n");
                    }

                    // Prepend Winner with Congratulations
                    String summary = "🎉 Congratulations to " + winner + "!\n\n" +
                            "🏆 Winner: " + winner + " (" + maxVotes + " votes)\n\n" +
                            "Detailed Results:\n" + sb.toString();
                    holder.tvResultSummary.setText(summary.trim());
                }

            } else {
                holder.tvStatusBadge.setText("Pending");
                holder.tvStatusBadge.setTextColor(0xFFD97706); // Amber/Orange
                // We need an amber background, but for now reuse or tint
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
                holder.tvStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEF3C7)); // Light
                                                                                                                    // Amber

                if (resultDate != null) {
                    holder.tvDate.setText("Results on: "
                            + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(resultDate));
                } else {
                    holder.tvDate.setText("Results Date: To be announced");
                }

                holder.layoutResults.setVisibility(View.GONE);
                holder.layoutWaiting.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return elections.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatusBadge, tvDate, tvResultSummary;
            LinearLayout layoutResults, layoutWaiting;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvElectionTitle);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvResultSummary = itemView.findViewById(R.id.tvResultSummary);
                layoutResults = itemView.findViewById(R.id.layoutResults);
                layoutWaiting = itemView.findViewById(R.id.layoutWaiting);
            }
        }
    }
}
