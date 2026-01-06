package com.example.smartvotingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ElectionAdapter extends RecyclerView.Adapter<ElectionAdapter.ViewHolder> {

    private boolean isMultiSelectMode = false;
    private java.util.HashSet<Integer> selectedElectionIds = new java.util.HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    // Valid fields that were missing
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

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setMultiSelectMode(boolean enabled) {
        this.isMultiSelectMode = enabled;
        if (!enabled) {
            selectedElectionIds.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public java.util.ArrayList<Election> getSelectedElections() {
        java.util.ArrayList<Election> selected = new java.util.ArrayList<>();
        for (Election e : elections) {
            if (selectedElectionIds.contains(e.getId())) {
                selected.add(e);
            }
        }
        return selected;
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

        // Multi-Select Logic
        if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedElectionIds.contains(election.getId()));

            // Disable individual vote button in multi-select mode to avoid confusion
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(0.3f);

            holder.checkBox.setOnClickListener(v -> {
                if (selectedElectionIds.contains(election.getId())) {
                    selectedElectionIds.remove(election.getId());
                } else {
                    selectedElectionIds.add(election.getId());
                }
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedElectionIds.size());
                }
            });

            holder.itemView.setOnClickListener(v -> holder.checkBox.performClick());
        } else {
            holder.checkBox.setVisibility(View.GONE);
            // Restore normal click listener
            holder.itemView.setOnClickListener(null);
            holder.itemView.setLongClickable(true);
            holder.itemView.setOnLongClickListener(v -> {
                setMultiSelectMode(true);
                selectedElectionIds.add(election.getId()); // Select the one long-pressed
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedElectionIds.size());
                }
                return true;
            });
        }

        // Style status based on value (Only apply visual styles here, button logic
        // handled above/below)
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
            if (!isMultiSelectMode) {
                holder.btnVote.setEnabled(false);
                holder.btnVote.setAlpha(1.0f);
                holder.btnVote.setText("Results Out");
            }
        } else if (status.equals("closed")) {
            holder.status.setTextColor(0xFFDC2626); // Red
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFEE2E2)); // Light Red
            if (!isMultiSelectMode) {
                holder.btnVote.setEnabled(false);
                holder.btnVote.setAlpha(0.5f);
                holder.btnVote.setText("Closed");
            }
        } else if (voteManager != null && currentUserId != null
                && voteManager.hasUserVoted(currentUserId, election.getId())) {
            // USER HAS ALREADY VOTED
            holder.status.setTextColor(0xFF059669); // Green (or keep as is)
            holder.status.setBackgroundResource(R.drawable.bg_status_active);

            if (!isMultiSelectMode) {
                holder.btnVote.setEnabled(true);
                holder.btnVote.setAlpha(1.0f);
                holder.btnVote.setText("Vote Recorded");

                holder.btnVote.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Election ID: " + election.getId())
                            .setMessage("You have already voted in this election!")
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        } else {
            // Running/Active and NOT voted
            holder.status.setTextColor(0xFF059669); // Green
            holder.status.setBackgroundResource(R.drawable.bg_status_active);
            if (!isMultiSelectMode) {
                holder.btnVote.setEnabled(true);
                holder.btnVote.setAlpha(1.0f);
                holder.btnVote.setText("Vote Now");
                holder.btnVote.setOnClickListener(voteClickListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return elections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, status, state, minAge, date;
        android.widget.Button btnVote;
        android.widget.CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvElectionTitle);
            status = itemView.findViewById(R.id.tvStatus);
            state = itemView.findViewById(R.id.tvState);
            minAge = itemView.findViewById(R.id.tvMinAge);
            date = itemView.findViewById(R.id.tvDate);
            btnVote = itemView.findViewById(R.id.btnVote);
            checkBox = itemView.findViewById(R.id.cbSelect);
        }
    }
}
