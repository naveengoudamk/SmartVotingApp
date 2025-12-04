package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private TextView tvResultStatus, tvResults;
    private final String RELEASE_DATE_FILE = "release_date.txt";

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        tvResultStatus = view.findViewById(R.id.tvResultStatus);
        tvResults = view.findViewById(R.id.tvResults);

        checkAndLoadResults();

        return view;
    }

    private void checkAndLoadResults() {
        String releaseDateStr = readFile(RELEASE_DATE_FILE);

        if (releaseDateStr == null || releaseDateStr.isEmpty()) {
            tvResultStatus.setText("Results have not been declared yet.");
            tvResults.setText("");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date releaseDate = sdf.parse(releaseDateStr);
            Date currentDate = new Date();

            if (currentDate.after(releaseDate) || currentDate.equals(releaseDate)) {
                tvResultStatus.setText("Results published on: " + releaseDateStr);
                loadResults();
            } else {
                tvResultStatus.setText("Results will be published on: " + releaseDateStr);
                tvResults.setText("Please wait for the announcement.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvResultStatus.setText("Error checking result date.");
        }
    }

    private void loadResults() {
        ElectionManager electionManager = new ElectionManager(getContext());
        VoteManager voteManager = new VoteManager(getContext());
        VotingOptionManager optionManager = new VotingOptionManager(getContext());

        List<Election> elections = electionManager.getAllElections();

        if (elections.isEmpty()) {
            tvResults.setText("No elections found.");
            return;
        }

        StringBuilder results = new StringBuilder();

        for (Election election : elections) {
            results.append("━━━━━━━━━━━━━━━━━━━━\n");
            results.append("📊 ").append(election.getTitle()).append("\n");
            results.append("━━━━━━━━━━━━━━━━━━━━\n\n");

            Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
            List<VotingOption> options = optionManager.getOptionsByElection(election.getId());

            if (options.isEmpty()) {
                results.append("  No voting options available.\n\n");
                continue;
            }

            int totalVotes = 0;
            for (VotingOption option : options) {
                int count = voteCounts.getOrDefault(option.getId(), 0);
                totalVotes += count;
                results.append("  • ").append(option.getOptionName())
                        .append(": ").append(count).append(" votes\n");
            }

            results.append("\n  Total Votes: ").append(totalVotes).append("\n\n");
        }

        tvResults.setText(results.toString());
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
}
