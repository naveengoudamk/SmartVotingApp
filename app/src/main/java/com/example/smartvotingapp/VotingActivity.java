package com.example.smartvotingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class VotingActivity extends AppCompatActivity {

    private TextView electionInfo, userInfo;
    private Button voteButton;
    private RadioGroup radioGroupCandidates;
    private int electionId;
    private String aadhaarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        electionInfo = findViewById(R.id.textViewElectionId);
        userInfo = findViewById(R.id.textViewUserInfo);
        voteButton = findViewById(R.id.voteButton);
        radioGroupCandidates = findViewById(R.id.radioGroupCandidates);

        electionId = getIntent().getIntExtra("election_id", -1);
        String userName = getIntent().getStringExtra("user_name");
        aadhaarId = MainActivity.aadhaarId;

        electionInfo.setText("Election ID: " + electionId);
        userInfo.setText("Welcome " + userName + ", you are eligible to vote!");

        VoteManager voteManager = new VoteManager(this);

        // Check if user has already voted
        if (voteManager.hasUserVoted(aadhaarId, electionId)) {
            userInfo.setText("You have already voted in this election!");
            voteButton.setEnabled(false);
            voteButton.setAlpha(0.5f);
            radioGroupCandidates.setEnabled(false);
            Toast.makeText(this, "You cannot vote twice in the same election", Toast.LENGTH_LONG).show();
            return;
        }

        loadVotingOptions();

        voteButton.setOnClickListener(v -> {
            int selectedId = radioGroupCandidates.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = findViewById(selectedId);
            String optionId = (String) selectedRadioButton.getTag();

            // Record vote
            VoteRecord vote = new VoteRecord(aadhaarId, electionId, optionId, System.currentTimeMillis());
            voteManager.recordVote(vote);

            Toast.makeText(VotingActivity.this,
                    "✅ Vote recorded successfully!",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void loadVotingOptions() {
        VotingOptionManager optionManager = new VotingOptionManager(this);
        List<VotingOption> options = optionManager.getOptionsByElection(electionId);

        if (options.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No voting options available for this election.");
            emptyView.setTextSize(16);
            emptyView.setPadding(20, 20, 20, 20);
            radioGroupCandidates.addView(emptyView);
            voteButton.setEnabled(false);
            return;
        }

        for (VotingOption option : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option.getOptionName());
            rb.setTag(option.getId());
            rb.setTextSize(18);
            rb.setPadding(0, 10, 0, 10);
            radioGroupCandidates.addView(rb);
        }
    }
}
