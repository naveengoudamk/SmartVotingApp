package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VoteManager {
    private static final String FILE_NAME = "votes.json";
    private Context context;

    public VoteManager(Context context) {
        this.context = context;
    }

    public boolean hasUserVoted(String aadhaarId, int electionId) {
        List<VoteRecord> votes = getAllVotes();
        for (VoteRecord vote : votes) {
            if (vote.getAadhaarId().equals(aadhaarId) && vote.getElectionId() == electionId) {
                return true;
            }
        }
        return false;
    }

    public void recordVote(VoteRecord vote) {
        List<VoteRecord> votes = getAllVotes();
        votes.add(vote);
        saveVotes(votes);
    }

    public List<VoteRecord> getVotesByElection(int electionId) {
        List<VoteRecord> result = new ArrayList<>();
        List<VoteRecord> allVotes = getAllVotes();

        for (VoteRecord vote : allVotes) {
            if (vote.getElectionId() == electionId) {
                result.add(vote);
            }
        }
        return result;
    }

    public Map<String, Integer> getVoteCountsByElection(int electionId) {
        Map<String, Integer> counts = new HashMap<>();
        List<VoteRecord> votes = getVotesByElection(electionId);

        for (VoteRecord vote : votes) {
            String optionId = vote.getOptionId();
            counts.put(optionId, counts.getOrDefault(optionId, 0) + 1);
        }
        return counts;
    }

    public List<VoteRecord> getAllVotes() {
        List<VoteRecord> votes = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return votes;

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                votes.add(new VoteRecord(
                        obj.getString("aadhaarId"),
                        obj.getInt("electionId"),
                        obj.getString("optionId"),
                        obj.getLong("timestamp")));
            }
        } catch (Exception e) {
            Log.e("VoteManager", "Error reading votes", e);
        }
        return votes;
    }

    private void saveVotes(List<VoteRecord> votes) {
        JSONArray array = new JSONArray();
        for (VoteRecord vote : votes) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("aadhaarId", vote.getAadhaarId());
                obj.put("electionId", vote.getElectionId());
                obj.put("optionId", vote.getOptionId());
                obj.put("timestamp", vote.getTimestamp());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("VoteManager", "Error saving votes", e);
        }
    }
}
