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

public class ElectionManager {
    private static final String FILE_NAME = "elections.json";
    private Context context;

    public ElectionManager(Context context) {
        this.context = context;
    }

    public List<Election> getAllElections() {
        List<Election> list = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return loadDefaultElections();
        }

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("minAge"),
                        obj.getString("status"),
                        obj.optString("stopDate", "")));
            }
        } catch (Exception e) {
            Log.e("ElectionManager", "Error reading elections", e);
        }
        return list;
    }

    public void addElection(Election election) {
        List<Election> list = getAllElections();
        list.add(election);
        saveElections(list);
    }

    public void updateElection(Election election) {
        List<Election> list = getAllElections();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == election.getId()) {
                list.set(i, election);
                break;
            }
        }
        saveElections(list);
    }

    public void deleteElection(int id) {
        List<Election> list = getAllElections();
        list.removeIf(e -> e.getId() == id);
        saveElections(list);
    }

    private void saveElections(List<Election> list) {
        JSONArray array = new JSONArray();
        for (Election e : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", e.getId());
                obj.put("title", e.getTitle());
                obj.put("state", e.getState());
                obj.put("minAge", e.getMinAge());
                obj.put("status", e.getStatus());
                obj.put("stopDate", e.getStopDate());
                array.put(obj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("ElectionManager", "Error saving elections", e);
        }
    }

    private List<Election> loadDefaultElections() {
        List<Election> list = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("elections_data.json");
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                list.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("min_age"), // Note: JSON uses min_age, verify this matches
                        obj.getString("status"),
                        obj.optString("stopDate", "")));
            }
            // Save these defaults to local storage so edits persist
            saveElections(list);
        } catch (Exception e) {
            Log.e("ElectionManager", "Error reading default elections", e);
        }
        return list;
    }
}
