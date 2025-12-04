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

public class NewsManager {
    private static final String FILE_NAME = "news.json";
    private Context context;

    public NewsManager(Context context) {
        this.context = context;
    }

    public List<News> getAllNews() {
        List<News> newsList = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists())
            return newsList;

        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8.name());
            String json = scanner.useDelimiter("\\A").next();
            scanner.close();

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                newsList.add(new News(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("date"),
                        obj.getLong("timestamp"),
                        obj.optString("imageUrl", "")));
            }
        } catch (Exception e) {
            Log.e("NewsManager", "Error reading news", e);
        }
        return newsList;
    }

    public void addNews(News news) {
        List<News> list = getAllNews();
        list.add(0, news); // Add to top
        saveNews(list);
    }

    public void updateNews(News news) {
        List<News> list = getAllNews();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(news.getId())) {
                list.set(i, news);
                break;
            }
        }
        saveNews(list);
    }

    public void deleteNews(String id) {
        List<News> list = getAllNews();
        list.removeIf(n -> n.getId().equals(id));
        saveNews(list);
    }

    private void saveNews(List<News> list) {
        JSONArray array = new JSONArray();
        for (News n : list) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", n.getId());
                obj.put("title", n.getTitle());
                obj.put("description", n.getDescription());
                obj.put("date", n.getDate());
                obj.put("timestamp", n.getTimestamp());
                obj.put("imageUrl", n.getImageUrl());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(array.toString().getBytes());
        } catch (IOException e) {
            Log.e("NewsManager", "Error saving news", e);
        }
    }
}
