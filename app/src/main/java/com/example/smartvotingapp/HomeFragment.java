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

public class HomeFragment extends Fragment implements SearchableFragment, NewsManager.NewsUpdateListener {

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
            // Register listener immediately
            newsManager.addListener(this);

            newsContainer = view.findViewById(R.id.newsContainer);

            // Initial load (might be empty if firebase not ready, but listener will catch
            // it)
            updateNewsUI();

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
    public void onDestroyView() {
        super.onDestroyView();
        if (newsManager != null) {
            newsManager.removeListener(this);
        }
    }

    @Override
    public void onNewsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::updateNewsUI);
        }
    }

    private void updateNewsUI() {
        allNews = newsManager.getAllNews(); // Get cached news
        loadNews(allNews);
    }

    @Override
    public void onSearch(String query) {
        if (allNews == null) {
            allNews = newsManager.getAllNews();
        }

        if (allNews == null || allNews.isEmpty())
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

    private void loadNews(List<News> newsList) {
        if (newsContainer == null)
            return;
        newsContainer.removeAllViews();

        if (newsList.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Loading news or no news available...");
            emptyView.setPadding(20, 20, 20, 20);
            newsContainer.addView(emptyView);
            return;
        }

        for (News news : newsList) {
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
                    String url = news.getImageUrl();
                    if (url.startsWith("data:")) {
                        // Base64
                        String base64 = url.substring(url.indexOf(",") + 1);
                        byte[] decodedString = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory
                                .decodeByteArray(decodedString, 0, decodedString.length);
                        imgNews.setImageBitmap(decodedByte);
                    } else {
                        android.net.Uri uri = android.net.Uri.parse(url);
                        if (uri.getScheme() != null
                                && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                            // This is likely legacy/local only, might not work on other devices,
                            // but keeping it for backward compat if needed (though now we use base64)
                            imgNews.setImageURI(uri);
                        } else {
                            new Thread(() -> {
                                try {
                                    java.io.InputStream in = new java.net.URL(url).openStream();
                                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                                    newsView.post(() -> imgNews.setImageBitmap(bmp));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    newsView.post(() -> imgNews.setImageResource(R.drawable.ic_news_placeholder));
                                }
                            }).start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imgNews.setImageResource(R.drawable.ic_news_placeholder);
                }
            } else {
                imgNews.setImageResource(R.drawable.ic_news_placeholder);
            }

            android.view.animation.Animation animation = android.view.animation.AnimationUtils
                    .loadAnimation(getContext(), R.anim.slide_in_up);
            animation.setStartOffset(newsContainer.getChildCount() * 100);
            newsView.startAnimation(animation);

            newsContainer.addView(newsView);
        }
    }
}
