package com.example.smartvotingapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ElectionUtils {

    public static List<Election> loadElectionsFromAssets(Context context) {
        List<Election> elections = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("elections_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                elections.add(new Election(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("state"),
                        obj.getInt("min_age"),
                        obj.getString("status"),
                        obj.optString("stop_date", "")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elections;
    }
}
