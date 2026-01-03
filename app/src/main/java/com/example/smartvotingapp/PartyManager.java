
package com.example.smartvotingapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PartyManager {
    private static final String TAG = "PartyManager";
    private Context context;
    private DatabaseReference databaseReference;
    private List<Party> cachedParties = new ArrayList<>();
    private List<PartyUpdateListener> listeners = new ArrayList<>();

    public interface PartyUpdateListener {
        void onPartiesUpdated();
    }

    public PartyManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("parties");

        // Listen for updates
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedParties.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Party party = child.getValue(Party.class);
                        if (party != null) {
                            cachedParties.add(party);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing party", e);
                    }
                }
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        // Seed if empty (one-time check usually, but safely ignored if data exists)
        // We will do a single check to seed defaults if absolutely nothing exists
        seedDefaultsIfNeeded();
    }

    private void seedDefaultsIfNeeded() {
        // Minimal check to avoid overwriting or duplicating on every restart
        // Real logic is handled by onDataChange, but for initial seed:
        databaseReference.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                seedDefaultParties();
            }
        });
    }

    private void seedDefaultParties() {
        List<Party> defaults = new ArrayList<>();
        defaults.add(new Party("1", "Bharatiya Janata Party (BJP)", "Lotus",
                "The Bharatiya Janata Party is one of two major political parties in India.", "res:ic_bjp"));
        defaults.add(new Party("2", "Indian National Congress (INC)", "Hand",
                "The Indian National Congress is a political party in India with widespread roots.", "res:ic_inc"));
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
        defaults.add(new Party("9", "Communist Party of India (CPI)", "Ears of Corn and Sickle",
                "The Communist Party of India is the oldest communist party in India.", "res:ic_cpi"));
        defaults.add(new Party("10", "CPI(M)", "Hammer, Sickle and Star",
                "The Communist Party of India (Marxist) is a communist political party in India.", "res:ic_cpim"));
        defaults.add(new Party("11", "Nationalist Congress Party (NCP)", "Clock",
                "The Nationalist Congress Party is one of the eight national parties in India.", "res:ic_ncp"));
        defaults.add(new Party("12", "Rashtriya Janata Dal (RJD)", "Hurricane Lamp",
                "The Rashtriya Janata Dal is an Indian political party, based in the state of Bihar.", "res:ic_rjd"));
        defaults.add(new Party("13", "Janata Dal (United) (JDU)", "Arrow",
                "Janata Dal (United) is an Indian political party with political presence mainly in Bihar.",
                "res:ic_jdu"));
        defaults.add(new Party("14", "Telugu Desam Party (TDP)", "Bicycle",
                "The Telugu Desam Party is a regional political party active in the southern states of Andhra Pradesh and Telangana.",
                "res:ic_tdp"));
        defaults.add(new Party("15", "YSR Congress Party (YSRCP)", "Ceiling Fan",
                "Yuvajana Sramika Rythu Congress Party is an Indian regional political party in the state of Andhra Pradesh.",
                "res:ic_ysrcp"));
        defaults.add(new Party("16", "Biju Janata Dal (BJD)", "Conch",
                "Biju Janata Dal is an Indian regional political party with significant influence in the state of Odisha.",
                "res:ic_bjd"));
        defaults.add(new Party("17", "Jharkhand Mukti Morcha (JMM)", "Bow and Arrow",
                "Jharkhand Mukti Morcha is a state political party in the Indian state of Jharkhand.", "res:ic_jmm"));
        defaults.add(new Party("18", "Shiromani Akali Dal (SAD)", "Scales",
                "Shiromani Akali Dal is a centre-right Sikh-centric state political party in Punjab, India.",
                "res:ic_sad"));
        defaults.add(new Party("19", "Janata Dal (Secular) (JDS)", "Lady Farmer",
                "Janata Dal (Secular) is an Indian political party led by former Prime Minister H. D. Deve Gowda.",
                "res:ic_jds"));

        for (Party p : defaults) {
            // Push with custom ID if possible or use the default ID as key
            databaseReference.child(p.getId()).setValue(p);
        }
    }

    /**
     * Erases all existing parties and resets to the default list.
     */
    public void resetToDefaults() {
        databaseReference.removeValue((error, ref) -> {
            if (error == null) {
                seedDefaultParties();
            } else {
                Log.e(TAG, "Failed to reset parties: " + error.getMessage());
            }
        });
    }

    public void addListener(PartyUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listener.onPartiesUpdated();
        }
    }

    public void removeListener(PartyUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (PartyUpdateListener listener : listeners) {
            listener.onPartiesUpdated();
        }
    }

    public List<Party> getAllParties() {
        return new ArrayList<>(cachedParties);
    }

    public void addParty(Party party) {
        if (party.getId() == null || party.getId().isEmpty()) {
            String key = databaseReference.push().getKey();
            party = new Party(key, party.getName(), party.getSymbol(), party.getDescription(), party.getLogoPath());
        }
        databaseReference.child(party.getId()).setValue(party);
    }

    public void updateParty(Party party) {
        if (party.getId() != null) {
            databaseReference.child(party.getId()).setValue(party);
        }
    }

    public void deleteParty(String id) {
        if (id != null) {
            databaseReference.child(id).removeValue();
        }
    }
}
