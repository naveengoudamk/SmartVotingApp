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
import java.util.Collections;
import java.util.List;

public class NewsManager {
    private static final String TAG = "NewsManager";
    private Context context;
    private DatabaseReference databaseReference;
    private List<News> cachedNews = new ArrayList<>();
    private List<NewsUpdateListener> listeners = new ArrayList<>();
    private boolean isDataLoaded = false;

    public interface NewsUpdateListener {
        void onNewsUpdated();
    }

    public NewsManager(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference("news");

        // Start listening
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedNews.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        News news = child.getValue(News.class);
                        if (news != null) {
                            cachedNews.add(news);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing news: " + e.getMessage());
                    }
                }
                // Sort by new to old (assuming timestamp helps or just order)
                // If timestamp exists: (n1, n2) -> Long.compare(n2.getTimestamp(),
                // n1.getTimestamp())
                cachedNews.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                isDataLoaded = true;
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    public void addListener(NewsUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        if (isDataLoaded) {
            listener.onNewsUpdated();
        }
    }

    public void removeListener(NewsUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (NewsUpdateListener listener : listeners) {
            listener.onNewsUpdated();
        }
    }

    public List<News> getAllNews() {
        return new ArrayList<>(cachedNews);
    }

    public void addNews(News news) {
        // Push to Firebase
        // Use news ID as key or generate new one
        if (news.getId() == null || news.getId().isEmpty()) {
            String key = databaseReference.push().getKey();
            news = new News(key, news.getTitle(), news.getDescription(), news.getDate(), news.getTimestamp(),
                    news.getImageUrl());
        }
        databaseReference.child(news.getId()).setValue(news);
    }

    public void updateNews(News news) {
        if (news.getId() != null) {
            databaseReference.child(news.getId()).setValue(news);
        }
    }

    public void deleteNews(String id) {
        if (id != null) {
            databaseReference.child(id).removeValue();
        }
    }
}
