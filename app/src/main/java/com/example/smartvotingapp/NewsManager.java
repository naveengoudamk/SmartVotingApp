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
        Log.d(TAG, "NewsManager initialized, Firebase reference: " + databaseReference.toString());

        // Start listening
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered, exists: " + snapshot.exists() + ", children: "
                        + snapshot.getChildrenCount());
                cachedNews.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        News news = child.getValue(News.class);
                        if (news != null) {
                            cachedNews.add(news);
                            Log.d(TAG, "Loaded news: " + news.getTitle());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing news: " + e.getMessage(), e);
                    }
                }
                // Sort by new to old
                cachedNews.sort((n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                isDataLoaded = true;
                Log.d(TAG, "Total news loaded: " + cachedNews.size());
                notifyListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage() + ", code: " + error.getCode());
            }
        });
    }

    public void addListener(NewsUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "Listener added, total listeners: " + listeners.size());
        }
        if (isDataLoaded) {
            listener.onNewsUpdated();
        }
    }

    public void removeListener(NewsUpdateListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Listener removed, total listeners: " + listeners.size());
    }

    private void notifyListeners() {
        Log.d(TAG, "Notifying " + listeners.size() + " listeners");
        for (NewsUpdateListener listener : listeners) {
            listener.onNewsUpdated();
        }
    }

    public List<News> getAllNews() {
        return new ArrayList<>(cachedNews);
    }

    public void addNews(News news) {
        // Push to Firebase
        if (news.getId() == null || news.getId().isEmpty()) {
            String key = databaseReference.push().getKey();
            news = new News(key, news.getTitle(), news.getDescription(), news.getDate(), news.getTimestamp(),
                    news.getImageUrl());
        }

        final String newsId = news.getId();
        Log.d(TAG, "Adding news with ID: " + newsId + ", title: " + news.getTitle());

        databaseReference.child(news.getId()).setValue(news)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ News added successfully: " + newsId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to add news: " + e.getMessage(), e);
                });
    }

    public void updateNews(News news) {
        if (news.getId() != null) {
            Log.d(TAG, "Updating news with ID: " + news.getId());
            databaseReference.child(news.getId()).setValue(news)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ News updated successfully: " + news.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to update news: " + e.getMessage(), e);
                    });
        }
    }

    public void deleteNews(String id) {
        if (id != null) {
            Log.d(TAG, "Deleting news with ID: " + id);
            databaseReference.child(id).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ News deleted successfully: " + id);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to delete news: " + e.getMessage(), e);
                    });
        }
    }
}
