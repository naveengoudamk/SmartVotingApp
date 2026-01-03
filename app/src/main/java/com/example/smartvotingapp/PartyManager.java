
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

        for (Party p : defaults) {
            // Push with custom ID if possible or use the default ID as key
            databaseReference.child(p.getId()).setValue(p);
        }
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
