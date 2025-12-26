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
        seedDefaultParties();
    }

    private void seedDefaultParties() {
        List<Party> existingParties = getAllParties();
        boolean changed = false;

        List<Party> defaults = new ArrayList<>();
        defaults.add(new Party("1", "Bharatiya Janata Party (BJP)", "Lotus",
                "The Bharatiya Janata Party is one of two major political parties in India.", "res:ic_bjp"));
        defaults.add(new Party("2", "Indian National Congress (INC)", "Hand",
                "The Indian National Congress is a political party in India with widespread roots.", "res:img_inc"));
        defaults.add(new Party("3", "Aam Aadmi Party (AAP)", "Broom",
                "The Aam Aadmi Party is a political party in India that was founded in November 2012.", "res:ic_aap"));
        defaults.add(new Party("4", "Trinamool Congress (TMC)", "Flowers & Grass",
                "The All India Trinamool Congress is an Indian political party which is predominantly active in West Bengal.",
                "res:ic_tmc"));
        defaults.add(new Party("5", "Dravida Munnetra Kazhagam (DMK)", "Rising Sun",
                "Dravida Munnetra Kazhagam is a political party in India, particularly in the state of Tamil Nadu and Puducherry.",
                "res:ic_dmk"));
        defaults.add(new Party("6", "AIADMK", "Two Leaves",
                "All India Anna Dravida Munnetra Kazhagam is an Indian regional political party with great influence in the state of Tamil Nadu.",
                "res:ic_aiadmk"));
        defaults.add(new Party("7", "Samajwadi Party (SP)", "Bicycle",
                "The Samajwadi Party is a socialist political party in India, headquartered in New Delhi.",
                "res:ic_sp"));
        defaults.add(new Party("8", "Bahujan Samaj Party (BSP)", "Elephant",
                "The Bahujan Samaj Party is a national level political party in India that was formed to represent the Bahujans.",
                "res:ic_bsp"));

        for (Party def : defaults) {
            boolean exists = false;
            for (Party p : existingParties) {
                if (p.getName().equalsIgnoreCase(def.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                Party partyToAdd = def;
                final String defId = def.getId();
                boolean idExists = existingParties.stream().anyMatch(p -> p.getId().equals(defId));
                if (idExists) {
                    // If ID exists but name doesn't, it's a conflict. Let's give a new ID to the
                    // default party being added.
                    partyToAdd = new Party(java.util.UUID.randomUUID().toString(), def.getName(), def.getSymbol(),
                            def.getDescription(), def.getLogoPath());
                }
                existingParties.add(partyToAdd);
                changed = true;
            }
        }

        if (changed) {
            saveParties(existingParties);
        }
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
                        obj.optString("description", ""),
                        obj.optString("logoPath", null)));
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
                if (p.getLogoPath() != null) {
                    obj.put("logoPath", p.getLogoPath());
                }
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
