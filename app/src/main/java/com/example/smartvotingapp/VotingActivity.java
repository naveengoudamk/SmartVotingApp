package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class VotingActivity extends AppCompatActivity
        implements VoteManager.VoteUpdateListener, VotingOptionManager.VotingOptionUpdateListener {

    private TextView electionInfo, userInfo, tvSelectedOption;
    private Button voteButton;
    private RecyclerView rvVotingOptions;
    private LinearLayout bottomBar;
    private int electionId;
    private String aadhaarId;
    private VotingOptionAdapter adapter;
    private String selectedOptionId = null;
    private String selectedOptionName = null;
    private VoteManager voteManager;
    private VotingOptionManager optionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        electionInfo = findViewById(R.id.textViewElectionId);
        userInfo = findViewById(R.id.textViewUserInfo);
        voteButton = findViewById(R.id.voteButton);
        rvVotingOptions = findViewById(R.id.rvVotingOptions);
        bottomBar = findViewById(R.id.bottomBar);
        tvSelectedOption = findViewById(R.id.tvSelectedOption);

        electionId = getIntent().getIntExtra("election_id", -1);
        String userName = getIntent().getStringExtra("user_name");
        aadhaarId = MainActivity.aadhaarId;

        if (electionInfo != null)
            electionInfo.setText("Election ID: " + electionId);
        userInfo.setText("Checking eligibility...");

        rvVotingOptions.setLayoutManager(new LinearLayoutManager(this));

        voteManager = new VoteManager(this);
        voteManager.addListener(this);

        optionManager = new VotingOptionManager(this);
        optionManager.addListener(this);

        checkVoteStatus();

        // Initial setup for click listener (won't work if hidden by checkVoteStatus)
        voteButton.setOnClickListener(v -> {
            if (selectedOptionId == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            // Record vote
            VoteRecord vote = new VoteRecord(aadhaarId, electionId, selectedOptionId, System.currentTimeMillis());
            voteManager.recordVote(vote);
            // Result of this async call will come back via onVotesUpdated (or simple
            // success log)
            // But we should optimistically allow finish or wait for feedback

            Toast.makeText(VotingActivity.this,
                    "✅ Vote Submitted! updating...",
                    Toast.LENGTH_SHORT).show();
            // We can finish here, acts as optimistic UI
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voteManager != null) {
            voteManager.removeListener(this);
        }
        if (optionManager != null) {
            optionManager.removeListener(this);
        }
    }

    @Override
    public void onVotesUpdated() {
        runOnUiThread(this::checkVoteStatus);
    }

    @Override
    public void onVotingOptionsUpdated() {
        runOnUiThread(() -> {
            if (adapter == null) {
                loadVotingOptions();
            } else {
                // Refresh the adapter with new data
                List<VotingOption> options = optionManager.getOptionsByElection(electionId);
                adapter.updateOptions(options);
            }
        });
    }

    private void checkVoteStatus() {
        if (voteManager.hasUserVoted(aadhaarId, electionId)) {
            userInfo.setText("You have already voted in this election!");
            userInfo.setTextColor(android.graphics.Color.RED);
            rvVotingOptions.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            // Not voted yet, show options
            userInfo.setText("You are eligible to vote!");
            userInfo.setTextColor(getResources().getColor(android.R.color.black)); // Reset color
            if (adapter == null) {
                loadVotingOptions();
            } else {
                rvVotingOptions.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadVotingOptions() {
        List<VotingOption> options = optionManager.getOptionsByElection(electionId);

        if (options.isEmpty()) {
            userInfo.setText("No voting options available for this election.");
            return;
        }

        adapter = new VotingOptionAdapter(options);
        rvVotingOptions.setAdapter(adapter);
    }

    private class VotingOptionAdapter extends RecyclerView.Adapter<VotingOptionAdapter.ViewHolder> {

        private List<VotingOption> options;
        private int selectedPosition = -1;

        public VotingOptionAdapter(List<VotingOption> options) {
            this.options = options;
        }

        public void updateOptions(List<VotingOption> newOptions) {
            this.options = newOptions;
            this.selectedPosition = -1; // Reset selection
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_voting_option_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VotingOption option = options.get(position);
            holder.tvCandidateName.setText(option.getOptionName());
            holder.tvPartyName.setText(option.getDescription());

            // Load Logo
            if (option.getLogoPath() != null) {
                try {
                    if (option.getLogoPath().startsWith("data:")) {
                        String base64 = option.getLogoPath().substring(option.getLogoPath().indexOf(",") + 1);
                        byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory
                                .decodeByteArray(decodedString, 0, decodedString.length);
                        holder.imgPartyLogo.setImageBitmap(decodedByte);
                    } else if (option.getLogoPath().startsWith("res:")) {
                        String resName = option.getLogoPath().substring(4);
                        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                        if (resId != 0) {
                            holder.imgPartyLogo.setImageResource(resId);
                        }
                    } else {
                        java.io.File imgFile = new java.io.File(getFilesDir(), option.getLogoPath());
                        if (imgFile.exists()) {
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                                    .decodeFile(imgFile.getAbsolutePath());
                            holder.imgPartyLogo.setImageBitmap(bitmap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.imgPartyLogo.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Selection State
            boolean isSelected = selectedPosition == position;
            holder.rbSelect.setChecked(isSelected);

            if (isSelected) {
                holder.cardView.setStrokeColor(0xFF1E3A8A); // Dark Blue
                holder.cardView.setStrokeWidth(4);
                holder.cardView.setCardElevation(8);
            } else {
                holder.cardView.setStrokeColor(0xFFE5E7EB); // Gray
                holder.cardView.setStrokeWidth(2);
                holder.cardView.setCardElevation(4);
            }

            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getAdapterPosition();

                // Update Logic
                selectedOptionId = option.getId();
                selectedOptionName = option.getOptionName();

                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                // Show Bottom Bar
                bottomBar.setVisibility(View.VISIBLE);
                tvSelectedOption.setText("Selected: " + selectedOptionName);
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            ImageView imgPartyLogo;
            TextView tvCandidateName, tvPartyName;
            RadioButton rbSelect;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardVotingOption);
                imgPartyLogo = itemView.findViewById(R.id.imgPartyLogo);
                tvCandidateName = itemView.findViewById(R.id.tvCandidateName);
                tvPartyName = itemView.findViewById(R.id.tvPartyName);
                rbSelect = itemView.findViewById(R.id.rbSelect);
            }
        }
    }
}
