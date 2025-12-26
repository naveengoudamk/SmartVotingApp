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

public class VotingActivity extends AppCompatActivity {

    private TextView electionInfo, userInfo, tvSelectedOption;
    private Button voteButton;
    private RecyclerView rvVotingOptions;
    private LinearLayout bottomBar;
    private int electionId;
    private String aadhaarId;
    private VotingOptionAdapter adapter;
    private String selectedOptionId = null;
    private String selectedOptionName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        electionInfo = findViewById(R.id.textViewElectionId); // Note: ID might be missing in new layout, check XML
        userInfo = findViewById(R.id.textViewUserInfo);
        voteButton = findViewById(R.id.voteButton);
        rvVotingOptions = findViewById(R.id.rvVotingOptions);
        bottomBar = findViewById(R.id.bottomBar);
        tvSelectedOption = findViewById(R.id.tvSelectedOption);

        electionId = getIntent().getIntExtra("election_id", -1);
        String userName = getIntent().getStringExtra("user_name");
        aadhaarId = MainActivity.aadhaarId;

        // electionInfo might be null if I removed it from XML, let's check or handle
        // gracefully
        if (electionInfo != null)
            electionInfo.setText("Election ID: " + electionId);
        userInfo.setText("Welcome " + userName + ", you are eligible to vote!");

        rvVotingOptions.setLayoutManager(new LinearLayoutManager(this));

        VoteManager voteManager = new VoteManager(this);

        // Check if user has already voted
        if (voteManager.hasUserVoted(aadhaarId, electionId)) {
            userInfo.setText("You have already voted in this election!");
            userInfo.setTextColor(android.graphics.Color.RED);
            rvVotingOptions.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            return;
        }

        loadVotingOptions();

        voteButton.setOnClickListener(v -> {
            if (selectedOptionId == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            // Record vote
            VoteRecord vote = new VoteRecord(aadhaarId, electionId, selectedOptionId, System.currentTimeMillis());
            voteManager.recordVote(vote);

            Toast.makeText(VotingActivity.this,
                    "✅ Vote recorded for " + selectedOptionName,
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void loadVotingOptions() {
        VotingOptionManager optionManager = new VotingOptionManager(this);
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
                    if (option.getLogoPath().startsWith("res:")) {
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
