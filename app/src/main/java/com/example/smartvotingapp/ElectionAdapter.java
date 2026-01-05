package com.example.smartvotingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ElectionAdapter extends RecyclerView.Adapter<ElectionAdapter.ViewHolder> {

    private List<Election> elections;
    private OnElectionClickListener listener;
    private VoteManager voteManager;
    private String currentUserId;

    public interface OnElectionClickListener {
        void onElectionClick(Election election);
    }

    public ElectionAdapter(List<Election> elections, VoteManager voteManager, String userId,
            OnElectionClickListener listener) {
        this.elections = elections;
        this.voteManager = voteManager;
        this.currentUserId = userId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_election_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Election election = elections.get(position);
        holder.title.setText(election.getTitle());
        holder.state.setText(election.getState());
        holder.minAge.setText(election.getMinAge() + "+");
        holder.date.setText(election.getStopDate());
        holder.status.setText(election.getStatus());

        // Style status based on value
        String status = election.getStatus().toLowerCase();

        // Default click listener for normal 'Vote Now'
        android.view.View.OnClickListener voteClickListener = v -> {
            if (listener != null) {
                listener.onElectionClick(election);
            }
        };

        if (status.contains("result")) {
            holder.status.setTextColor(0xFF2563EB); // Blue
            holder.status.setBackgroundResource(R.drawable.bg_status_active); // Consider specialized bg
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFDBEAFE)); // Light Blue
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(1.0f);
            holder.btnVote.setText("Results Out");
            holder.btnVote.setOnClickListener(null); // No action
        } else if (status.equals("closed")) {
            holder.status.setTextColor(0xFFDC2626); // Red
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEE2E2)); // Light Red
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(0.5f);
            holder.btnVote.setText("Closed");
            holder.btnVote.setOnClickListener(null);
        } else if (voteManager != null && currentUserId != null
                && voteManager.hasUserVoted(currentUserId, election.getId())) {
            // USER HAS ALREADY VOTED
            holder.status.setTextColor(0xFF059669); // Green (or keep as is)
            holder.status.setBackgroundResource(R.drawable.bg_status_active);

            holder.btnVote.setEnabled(true);
            holder.btnVote.setAlpha(1.0f);
            holder.btnVote.setText("Vote Recorded");
            // holder.btnVote.setBackgroundTintList(...) // Optionally change color to
            // something distinctive

            holder.btnVote.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Election ID: " + election.getId())
                        .setMessage("You have already voted in this election!")
                        .setPositiveButton("OK", null)
                        .show();
            });
        } else {
            // Running/Active and NOT voted
            holder.status.setTextColor(0xFF059669); // Green
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            holder.btnVote.setEnabled(true);
            holder.btnVote.setAlpha(1.0f);
            holder.btnVote.setText("Vote Now");
            holder.btnVote.setOnClickListener(voteClickListener);
        }

        // Also make the whole card clickable acts as 'Vote Now' only if eligible?
        // If voted, card click should generally do nothing or show same popup?
        // Let's stick to btnVote handling specific actions.
        // If we want card click to work:
        holder.itemView.setOnClickListener(v -> holder.btnVote.performClick());
    }

    @Override
    public int getItemCount() {
        return elections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status, state, minAge, date;
        android.widget.Button btnVote;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvElectionTitle);
            status = itemView.findViewById(R.id.tvStatus);
            state = itemView.findViewById(R.id.tvState);
            minAge = itemView.findViewById(R.id.tvMinAge);
            date = itemView.findViewById(R.id.tvDate);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}
