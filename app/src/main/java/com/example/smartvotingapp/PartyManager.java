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

public class PartyManager {
    private static final String FILE_NAME = "parties.json";
    private Context context;

    public PartyManager(Context context) {
        this.context = context;
    }

    public List<Party> getAllParties() {
        List<Party> parties = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return parties;

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                parties.add(new Party(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.optString("symbol", ""),
                        obj.optString("description", "")));
            }
        } catch (Exception e) {
            Log.e("PartyManager", "Error reading parties", e);
        }
        return parties;
    }

    public void addParty(Party party) {
        List<Party> parties = getAllParties();
        parties.add(party);
        saveParties(parties);
    }

    public void updateParty(Party party) {
        List<Party> parties = getAllParties();
        for (int i = 0; i < parties.size(); i++) {
            if (parties.get(i).getId().equals(party.getId())) {
                parties.set(i, party);
                break;
            }
        }
        saveParties(parties);
    }

    public void deleteParty(String id) {
        List<Party> parties = getAllParties();
        parties.removeIf(p -> p.getId().equals(id));
        saveParties(parties);
    }

    private void saveParties(List<Party> parties) {
        JSONArray array = new JSONArray();
        for (Party p : parties) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", p.getId());
                obj.put("name", p.getName());
                obj.put("symbol", p.getSymbol());
                obj.put("description", p.getDescription());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("PartyManager", "Error saving parties", e);
        }
    }
}
