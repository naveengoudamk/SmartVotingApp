package com.example.smartvotingapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.io.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AdminResultFragment extends Fragment {

    private TextView tvReleaseDate, tvResults;
    private LinearLayout resultsContainer;
    private final String RELEASE_DATE_FILE = "release_date.txt";

    public AdminResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_result, container, false);

        tvReleaseDate = view.findViewById(R.id.tvReleaseDate);
        tvResults = view.findViewById(R.id.tvResults);
        Button btnSetDate = view.findViewById(R.id.btnSetDate);

        btnSetDate.setOnClickListener(v -> openDatePicker());

        loadReleaseDate();
        loadResults();

        return view;
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + (month + 1) + "-" + dayOfMonth;
            saveToFile(RELEASE_DATE_FILE, date);
            tvReleaseDate.setText("Release Date: " + date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadReleaseDate() {
        String date = readFile(RELEASE_DATE_FILE);
        tvReleaseDate.setText("Release Date: " + (date != null ? date : "Not set"));
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
                results.append("  No voting options configured.\n\n");
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

    private void saveToFile(String filename, String content) {
        try (FileOutputStream fos = getContext().openFileOutput(filename, android.content.Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
