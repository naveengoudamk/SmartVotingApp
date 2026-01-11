package com.example.smartvotingapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory, rvWinnerSummary;
    private ElectionManager electionManager;
    private VoteManager voteManager;
    private VotingOptionManager optionManager;
    private List<Election> electionList = new ArrayList<>();
    private List<WinnerSummary> winnerList = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private WinnerSummaryAdapter winnerAdapter;
    private String targetId;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_history, container, false);

            if (getArguments() != null) {
                targetId = getArguments().getString("target_id");
            }

            rvHistory = view.findViewById(R.id.rvHistory);
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

            rvWinnerSummary = view.findViewById(R.id.rvWinnerSummary);
            rvWinnerSummary
                    .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            electionManager = new ElectionManager(getContext());
            voteManager = new VoteManager(getContext());
            optionManager = new VotingOptionManager(getContext());

            // Initialize Adapters
            historyAdapter = new HistoryAdapter(electionList, getContext(), voteManager, optionManager);
            rvHistory.setAdapter(historyAdapter);

            winnerAdapter = new WinnerSummaryAdapter(winnerList);
            rvWinnerSummary.setAdapter(winnerAdapter);

            // Listeners to refresh data
            Runnable refreshRunnable = () -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::updateData);
                }
            };

            electionManager.addListener(new ElectionManager.ElectionUpdateListener() {
                @Override
                public void onElectionsUpdated() {
                    refreshRunnable.run();
                }
            });
            voteManager.addListener(() -> refreshRunnable.run());
            optionManager.addListener(new VotingOptionManager.VotingOptionUpdateListener() {
                @Override
                public void onVotingOptionsUpdated() {
                    refreshRunnable.run();
                }
            });

            // Initial load check
            updateData();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(getContext(), "Error: " + e.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
            return new View(getContext());
        }
    }

    private void updateData() {
        if (!isAdded())
            return;
        electionList.clear();
        electionList.addAll(electionManager.getAllElections());
        historyAdapter.notifyDataSetChanged();

        if (targetId != null) {
            for (int i = 0; i < electionList.size(); i++) {
                if (String.valueOf(electionList.get(i).getId()).equals(targetId)) {
                    int pos = i;
                    rvHistory.post(() -> rvHistory.smoothScrollToPosition(pos));
                    targetId = null;
                    break;
                }
            }
        }

        // Calculate Winners for Top Bar
        winnerList.clear();
        for (Election election : electionList) {
            if (isElectionDeclared(election)) {
                WinnerSummary winner = calculateWinner(election);
                if (winner != null) {
                    winnerList.add(winner);
                }
            }
        }
        winnerAdapter.notifyDataSetChanged();

        if (winnerList.isEmpty()) {
            rvWinnerSummary.setVisibility(View.GONE);
        } else {
            rvWinnerSummary.setVisibility(View.VISIBLE);
        }
    }

    private boolean isElectionDeclared(Election election) {
        // Date check logic
        String resultDateStr = election.getResultDate();
        if ("Results Announced".equalsIgnoreCase(election.getStatus()))
            return true;

        if (resultDateStr != null && !resultDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date resultDate = sdf.parse(resultDateStr);

                java.util.Calendar today = java.util.Calendar.getInstance();
                today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                today.set(java.util.Calendar.MINUTE, 0);
                today.set(java.util.Calendar.SECOND, 0);
                today.set(java.util.Calendar.MILLISECOND, 0);

                java.util.Calendar resultCal = java.util.Calendar.getInstance();
                resultCal.setTime(resultDate);
                resultCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                resultCal.set(java.util.Calendar.MINUTE, 0);
                resultCal.set(java.util.Calendar.SECOND, 0);
                resultCal.set(java.util.Calendar.MILLISECOND, 0);

                if (!today.before(resultCal))
                    return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    private WinnerSummary calculateWinner(Election election) {
        Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
        List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

        if (options.isEmpty())
            return null;

        String winnerParty = "";
        String logoPath = null;
        int maxVotes = -1;
        boolean tie = false;

        for (VotingOption option : options) {
            int count = voteCounts.getOrDefault(option.getId(), 0);
            if (count > maxVotes) {
                maxVotes = count;
                winnerParty = option.getDescription();
                logoPath = option.getLogoPath();
                tie = false;
            } else if (count == maxVotes) {
                tie = true;
            }
        }

        if (maxVotes > 0 && !tie) {
            return new WinnerSummary(election.getState(), winnerParty, logoPath);
        }
        return null;
    }

    private static class WinnerSummary {
        String state, party, logo;

        public WinnerSummary(String state, String party, String logo) {
            this.state = state;
            this.party = party;
            this.logo = logo;
        }
    }

    private static class WinnerSummaryAdapter extends RecyclerView.Adapter<WinnerSummaryAdapter.ViewHolder> {
        List<WinnerSummary> list;

        public WinnerSummaryAdapter(List<WinnerSummary> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_winner_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            WinnerSummary w = list.get(position);
            holder.tvState.setText(w.state);
            holder.tvParty.setText(w.party);

            if (w.logo != null) {
                try {
                    File file = new File(holder.itemView.getContext().getFilesDir(), w.logo); // Assume filename
                    if (file.exists()) {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                                .decodeFile(file.getAbsolutePath());
                        holder.imgLogo.setImageBitmap(bitmap);
                    } else {
                        // Try as absolute path
                        file = new File(w.logo);
                        if (file.exists()) {
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                                    .decodeFile(file.getAbsolutePath());
                            holder.imgLogo.setImageBitmap(bitmap);
                        } else {
                            holder.imgLogo.setImageResource(R.drawable.ic_vote);
                        }
                    }
                } catch (Exception e) {
                    holder.imgLogo.setImageResource(R.drawable.ic_vote);
                }
            } else {
                holder.imgLogo.setImageResource(R.drawable.ic_vote);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvState, tvParty;
            android.widget.ImageView imgLogo;

            public ViewHolder(View v) {
                super(v);
                tvState = v.findViewById(R.id.tvState);
                tvParty = v.findViewById(R.id.tvWinnerParty);
                imgLogo = v.findViewById(R.id.imgWinnerLogo);
            }
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<Election> elections;
        private VoteManager voteManager;
        private VotingOptionManager optionManager;

        public HistoryAdapter(List<Election> elections, android.content.Context context, VoteManager vm,
                VotingOptionManager vom) {
            this.elections = elections;
            this.voteManager = vm;
            this.optionManager = vom;
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
            String resultDateStr = election.getResultDate();
            Date resultDate = null;

            if (resultDateStr != null && !resultDateStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    resultDate = sdf.parse(resultDateStr);
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    today.set(java.util.Calendar.MINUTE, 0);
                    today.set(java.util.Calendar.SECOND, 0);
                    today.set(java.util.Calendar.MILLISECOND, 0);

                    java.util.Calendar resultCal = java.util.Calendar.getInstance();
                    resultCal.setTime(resultDate);
                    resultCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    resultCal.set(java.util.Calendar.MINUTE, 0);
                    resultCal.set(java.util.Calendar.SECOND, 0);
                    resultCal.set(java.util.Calendar.MILLISECOND, 0);

                    if (!today.before(resultCal))
                        isDeclared = true;
                } catch (Exception e) {
                }
            }
            if ("Results Announced".equalsIgnoreCase(election.getStatus()))
                isDeclared = true;

            if (isDeclared) {
                holder.tvStatusBadge.setText("Declared");
                holder.tvStatusBadge.setTextColor(0xFF059669);
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
                holder.tvDate.setText("Results declared" + (resultDate != null ? " on: " + resultDateStr : ""));

                holder.layoutResults.setVisibility(View.VISIBLE);
                holder.layoutWaiting.setVisibility(View.GONE);

                holder.confettiView.setVisibility(View.VISIBLE);
                holder.confettiView.startAnimation();

                // Stop animation after 2 seconds
                holder.confettiView.postDelayed(() -> {
                    holder.confettiView.stopAnimation();
                    holder.confettiView.setVisibility(View.GONE);
                }, 2000);

                Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
                List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

                if (options.isEmpty()) {
                    holder.tvResultSummary.setText("No candidates.");
                    holder.imgWinner.setVisibility(View.GONE);
                } else {
                    int max = -1;
                    String winner = "";
                    String logo = null;
                    boolean tie = false;
                    int totalVotes = 0;

                    // First pass: Calculate totals and winner
                    for (VotingOption opt : options) {
                        int c = voteCounts.getOrDefault(opt.getId(), 0);
                        totalVotes += c;
                        if (c > max) {
                            max = c;
                            winner = opt.getOptionName();
                            logo = opt.getLogoPath();
                            tie = false;
                        } else if (c == max) {
                            tie = true;
                        }
                    }

                    // Set Text Summary (Congrats)
                    if (tie) {
                        holder.tvResultSummary.setText("It's a tie! (" + max + " votes each)");
                        holder.imgWinner.setVisibility(View.GONE);
                    } else {
                        holder.tvResultSummary.setText("🎉 Congratulations " + winner + "!\nDeclared Winner");
                        if (logo != null) {
                            try {
                                File file = new File(holder.itemView.getContext().getFilesDir(), logo);
                                if (file.exists()) {
                                    holder.imgWinner.setImageBitmap(
                                            android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath()));
                                } else {
                                    holder.imgWinner.setImageResource(R.drawable.ic_vote);
                                }
                            } catch (Exception e) {
                                holder.imgWinner.setImageResource(R.drawable.ic_vote);
                            }
                            holder.imgWinner.setVisibility(View.VISIBLE);
                        } else {
                            holder.imgWinner.setVisibility(View.GONE);
                        }
                    }

                    // Populate Progress Bars
                    holder.layoutVoteBars.removeAllViews();
                    for (VotingOption opt : options) {
                        int count = voteCounts.getOrDefault(opt.getId(), 0);

                        View row = LayoutInflater.from(holder.itemView.getContext())
                                .inflate(R.layout.item_result_count_row, holder.layoutVoteBars, false);

                        TextView tvName = row.findViewById(R.id.tvOptionName);
                        TextView tvCount = row.findViewById(R.id.tvVoteCount);
                        android.widget.ProgressBar progressBar = row.findViewById(R.id.progressBar);

                        tvName.setText(opt.getOptionName());
                        tvCount.setText(count + " votes");

                        // Fix for 0 total votes division
                        progressBar.setMax(totalVotes > 0 ? totalVotes : 1);
                        progressBar.setProgress(count);

                        holder.layoutVoteBars.addView(row);
                    }

                    // Add Total Label
                    TextView tvTotal = new TextView(holder.itemView.getContext());
                    tvTotal.setText("Total Votes: " + totalVotes);
                    tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvTotal.setPadding(0, 16, 0, 0);
                    tvTotal.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
                    tvTotal.setTextColor(0xFF374151); // Gray-700
                    holder.layoutVoteBars.addView(tvTotal);
                }

            } else {
                holder.tvStatusBadge.setText("Pending");
                holder.tvStatusBadge.setTextColor(0xFFD97706);
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
                holder.tvDate.setText("Result Date: " + (resultDateStr != null ? resultDateStr : "TBA"));

                holder.layoutResults.setVisibility(View.GONE);
                holder.layoutWaiting.setVisibility(View.VISIBLE);
                holder.confettiView.setVisibility(View.GONE);
                holder.confettiView.stopAnimation();
            }
        }

        @Override
        public int getItemCount() {
            return elections.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStatusBadge, tvDate, tvResultSummary;
            LinearLayout layoutResults, layoutWaiting, layoutVoteBars;
            android.widget.ImageView imgWinner;
            SimpleConfettiView confettiView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvElectionTitle);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvResultSummary = itemView.findViewById(R.id.tvResultSummary);
                layoutResults = itemView.findViewById(R.id.layoutResults);
                layoutVoteBars = itemView.findViewById(R.id.layoutVoteBars);
                layoutWaiting = itemView.findViewById(R.id.layoutWaiting);
                imgWinner = itemView.findViewById(R.id.imgWinner);
                confettiView = itemView.findViewById(R.id.confettiView);
            }
        }
    }
}
