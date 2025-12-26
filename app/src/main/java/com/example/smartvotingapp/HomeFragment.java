package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.List;

public class HomeFragment extends Fragment implements SearchableFragment {

    private LinearLayout newsContainer;
    private NewsManager newsManager;
    private List<News> allNews;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);

            newsManager = new NewsManager(getContext());
            newsContainer = view.findViewById(R.id.newsContainer);

            loadSampleNewsIfNeeded();

            allNews = newsManager.getAllNews();
            loadNews(allNews);

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast
                    .makeText(getContext(), "Error loading home: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT)
                    .show();
            return new View(getContext());
        }
    }

    @Override
    public void onSearch(String query) {
        if (allNews == null)
            return;

        if (query == null || query.isEmpty()) {
            loadNews(allNews);
            return;
        }

        List<News> filteredList = new java.util.ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (News news : allNews) {
            if (news.getTitle().toLowerCase().contains(lowerQuery) ||
                    news.getDescription().toLowerCase().contains(lowerQuery)) {
                filteredList.add(news);
            }
        }
        loadNews(filteredList);
    }

    private void loadSampleNewsIfNeeded() {
        // Load sample news from assets if no news exists
        if (newsManager.getAllNews().isEmpty()) {
            try {
                java.io.InputStream is = getContext().getAssets().open("sample_news.json");
                java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8");
                String json = scanner.useDelimiter("\\A").next();
                scanner.close();

                org.json.JSONArray array = new org.json.JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);
                    News news = new News(
                            obj.getString("id"),
                            obj.getString("title"),
                            obj.getString("description"),
                            obj.getString("date"),
                            obj.getLong("timestamp"),
                            obj.optString("imageUrl", ""));
                    newsManager.addNews(news);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNews(List<News> newsList) {
        newsContainer.removeAllViews();

        if (newsList.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No news available.");
            emptyView.setPadding(20, 20, 20, 20);
            newsContainer.addView(emptyView);
            return;
        }

        for (News news : newsList) {
            // Use the new modern card layout
            View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_card, newsContainer, false);

            TextView title = newsView.findViewById(R.id.tvTitle);
            TextView date = newsView.findViewById(R.id.tvDate);
            TextView desc = newsView.findViewById(R.id.tvDescription);
            android.widget.ImageView imgNews = newsView.findViewById(R.id.imgNews);

            title.setText(news.getTitle());
            date.setText(news.getDate());
            desc.setText(news.getDescription());

            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                imgNews.setVisibility(View.VISIBLE);
                try {
                    android.net.Uri uri = android.net.Uri.parse(news.getImageUrl());
                    if (uri.getScheme() != null
                            && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                        // Local URI, load directly
                        imgNews.setImageURI(uri);
                    } else {
                        // Web URL, load in background
                        new Thread(() -> {
                            try {
                                java.io.InputStream in = new java.net.URL(news.getImageUrl()).openStream();
                                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                                newsView.post(() -> imgNews.setImageBitmap(bmp));
                            } catch (Exception e) {
                                e.printStackTrace();
                                newsView.post(() -> imgNews.setImageResource(R.drawable.ic_news_placeholder));
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imgNews.setImageResource(R.drawable.ic_news_placeholder);
                }
            } else {
                imgNews.setImageResource(R.drawable.ic_news_placeholder);
            }

            // Add animation
            android.view.animation.Animation animation = android.view.animation.AnimationUtils
                    .loadAnimation(getContext(), R.anim.slide_in_up);
            animation.setStartOffset(newsContainer.getChildCount() * 100); // Staggered effect
            newsView.startAnimation(animation);

            newsContainer.addView(newsView);
        }
    }
}
