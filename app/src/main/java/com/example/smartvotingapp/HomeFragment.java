package com.example.smartvotingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.List;

public class HomeFragment extends Fragment implements SearchableFragment, NewsManager.NewsUpdateListener {

    private LinearLayout newsContainer;
    private NewsManager newsManager;
    private List<News> allNews;

    private String targetId;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home, container, false);

            if (getArguments() != null) {
                targetId = getArguments().getString("target_id");
            }

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
            // Create a nice empty state view
            LinearLayout emptyState = new LinearLayout(getContext());
            emptyState.setOrientation(LinearLayout.VERTICAL);
            emptyState.setGravity(android.view.Gravity.CENTER);
            emptyState.setPadding(40, 80, 40, 80);

            ImageView emptyIcon = new ImageView(getContext());
            emptyIcon.setImageResource(R.drawable.ic_news_placeholder);
            emptyIcon.setAlpha(0.3f);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(200, 200);
            iconParams.gravity = android.view.Gravity.CENTER;
            emptyIcon.setLayoutParams(iconParams);

            TextView emptyTitle = new TextView(getContext());
            emptyTitle.setText("No News Yet");
            emptyTitle.setTextSize(20);
            emptyTitle.setTextColor(0xFF374151);
            emptyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            emptyTitle.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(0, 24, 0, 8);
            emptyTitle.setLayoutParams(titleParams);

            TextView emptyDesc = new TextView(getContext());
            emptyDesc.setText("Check back later for election updates and news");
            emptyDesc.setTextSize(14);
            emptyDesc.setTextColor(0xFF6B7280);
            emptyDesc.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            emptyDesc.setLayoutParams(descParams);

            emptyState.addView(emptyIcon);
            emptyState.addView(emptyTitle);
            emptyState.addView(emptyDesc);
            newsContainer.addView(emptyState);
            return;
        }

        for (News news : newsList) {
            View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_card, newsContainer, false);
            newsView.setTag(news.getId()); // Set ID as tag for scrolling

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

        // Scroll to target if present
        if (targetId != null) {
            newsContainer.post(() -> {
                for (int i = 0; i < newsContainer.getChildCount(); i++) {
                    View child = newsContainer.getChildAt(i);
                    if (child.getTag() != null && child.getTag().equals(targetId)) {
                        int y = child.getTop();
                        if (getView() != null && getView().findViewById(R.id.newsContainer)
                                .getParent() instanceof androidx.core.widget.NestedScrollView) {
                            ((androidx.core.widget.NestedScrollView) getView().findViewById(R.id.newsContainer)
                                    .getParent()).smoothScrollTo(0, y);
                        } else {
                            // Fallback if structure is different
                            child.getParent().requestChildFocus(child, child);
                        }
                        // Highlight effect?
                        child.setBackgroundColor(0x33FFC107); // Subtle highlight
                        child.postDelayed(() -> child.setBackgroundColor(0x00000000), 2000);
                        targetId = null;
                        break;
                    }
                }
            });
        }
    }
}
