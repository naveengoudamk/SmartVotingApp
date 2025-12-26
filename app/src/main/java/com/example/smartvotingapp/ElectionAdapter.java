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

    public interface OnElectionClickListener {
        void onElectionClick(Election election);
    }

    public ElectionAdapter(List<Election> elections, OnElectionClickListener listener) {
        this.elections = elections;
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
        if (status.equals("running") || status.equals("active") || status.equals("open")) {
            holder.status.setTextColor(0xFF059669); // Green
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            holder.btnVote.setEnabled(true);
            holder.btnVote.setAlpha(1.0f);
        } else {
            holder.status.setTextColor(0xFFDC2626); // Red
            // Create a red background or just reuse/tint
            holder.status.setBackgroundResource(R.drawable.bg_status_active); // Ideally different bg
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEE2E2)); // Light Red
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(0.5f);
            holder.btnVote.setText("Closed");
        }

        holder.btnVote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onElectionClick(election);
            }
        });

        // Also make the whole card clickable
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.btnVote.isEnabled()) {
                listener.onElectionClick(election);
            }
        });
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
