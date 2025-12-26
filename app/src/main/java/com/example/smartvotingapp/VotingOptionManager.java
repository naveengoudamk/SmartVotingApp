package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VotingOptionManager {
    private static final String FILE_NAME = "voting_options.json";
    private Context context;

    public VotingOptionManager(Context context) {
        this.context = context;
    }

    public List<VotingOption> getOptionsByElection(int electionId) {
        List<VotingOption> options = new ArrayList<>();
        List<VotingOption> allOptions = getAllOptions();

        for (VotingOption option : allOptions) {
            if (option.getElectionId() == electionId) {
                options.add(option);
            }
        }
        return options;
    }

    public List<VotingOption> getAllOptions() {
        List<VotingOption> options = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return options;

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                options.add(new VotingOption(
                        obj.getString("id"),
                        obj.getInt("electionId"),
                        obj.getString("optionName"),
                        obj.optString("description", ""),
                        obj.optString("logoPath", null)));
            }
        } catch (Exception e) {
            Log.e("VotingOptionManager", "Error reading options", e);
        }
        return options;
    }

    public void addOption(VotingOption option) {
        List<VotingOption> options = getAllOptions();
        options.add(option);
        saveOptions(options);
    }

    public void updateOption(VotingOption option) {
        List<VotingOption> options = getAllOptions();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId().equals(option.getId())) {
                options.set(i, option);
                break;
            }
        }
        saveOptions(options);
    }

    public void deleteOption(String optionId) {
        List<VotingOption> options = getAllOptions();
        options.removeIf(o -> o.getId().equals(optionId));
        saveOptions(options);
    }

    private void saveOptions(List<VotingOption> options) {
        JSONArray array = new JSONArray();
        for (VotingOption option : options) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", option.getId());
                obj.put("electionId", option.getElectionId());
                obj.put("optionName", option.getOptionName());
                obj.put("description", option.getDescription());
                if (option.getLogoPath() != null) {
                    obj.put("logoPath", option.getLogoPath());
                }
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("VotingOptionManager", "Error saving options", e);
        }
    }
}
