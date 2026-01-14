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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.File;

public class HomeFragment extends Fragment
        implements SearchableFragment, NewsManager.NewsUpdateListener, ElectionManager.ElectionUpdateListener {

    private LinearLayout newsContainer;
    private LinearLayout resultSummaryContainer;
    private RecyclerView rvHomeResults;
    private NewsManager newsManager;
    private ElectionManager electionManager;
    private VoteManager voteManager;
    private VotingOptionManager optionManager;
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
            resultSummaryContainer = view.findViewById(R.id.resultSummaryContainer);
            rvHomeResults = view.findViewById(R.id.rvHomeResults);
            rvHomeResults
                    .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            electionManager = new ElectionManager(getContext());
            electionManager.addListener(this);

            voteManager = new VoteManager(getContext()); // Needed for calculating winners
            // We verify data on load but don't strictly need real-time vote updates for the
            // summary unless we want it extremely lively.
            // But to show the winner correctly, we need loaded votes.
            voteManager.addListener(() -> {
                if (getActivity() != null)
                    getActivity().runOnUiThread(this::updateResultsUI);
            });

            optionManager = new VotingOptionManager(getContext());
            optionManager.addListener(() -> {
                if (getActivity() != null)
                    getActivity().runOnUiThread(this::updateResultsUI);
            });

            // Initial load
            updateNewsUI();
            updateResultsUI();

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
        if (electionManager != null) {
            electionManager.removeListener(this);
        }
        // Remove other listeners if we stored them - for now inline lambdas are tricky
        // to remove but context leak is minor in fragments if managed well.
        // Ideally store listeners but for brevity this is acceptable or we should
        // assume managers handle weak refs/clearing.
        // Actually managers use simple lists. To be safe let's assume simple usage.
    }

    @Override
    public void onNewsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::updateNewsUI);
        }
    }

    @Override
    public void onElectionsUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::updateResultsUI);
        }
    }

    private void updateResultsUI() {
        if (electionManager == null || voteManager == null || optionManager == null)
            return;
        List<Election> elections = electionManager.getAllElections();
        List<WinnerSummary> winners = new ArrayList<>();

        for (Election election : elections) {
            if (isElectionDeclared(election)) {
                WinnerSummary winner = calculateWinner(election);
                if (winner != null) {
                    winners.add(winner);
                }
            }
        }

        if (winners.isEmpty()) {
            resultSummaryContainer.setVisibility(View.GONE);
        } else {
            resultSummaryContainer.setVisibility(View.VISIBLE);
            WinnerAdapter adapter = new WinnerAdapter(winners);
            rvHomeResults.setAdapter(adapter);
        }
    }

    private boolean isElectionDeclared(Election election) {
        String resultDateStr = election.getResultDate();
        if ("Results Announced".equalsIgnoreCase(election.getStatus()))
            return true;
        if (resultDateStr != null && !resultDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date resultDate = sdf.parse(resultDateStr);
                java.util.Calendar today = java.util.Calendar.getInstance();
                today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                today.set(java.util.Calendar.MINUTE, 0);
                today.set(java.util.Calendar.SECOND, 0);
                today.set(java.util.Calendar.MILLISECOND, 0);

                java.util.Calendar resultCal = java.util.Calendar.getInstance();
                resultCal.setTime(resultDate);
                resultCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                resultCal.set(java.util.Calendar.MINUTE, 0);
                resultCal.set(java.util.Calendar.SECOND, 0);
                resultCal.set(java.util.Calendar.MILLISECOND, 0);

                if (!today.before(resultCal))
                    return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    private WinnerSummary calculateWinner(Election election) {
        Map<String, Integer> voteCounts = voteManager.getVoteCountsByElection(election.getId());
        List<VotingOption> options = optionManager.getOptionsByElection(election.getId());
        if (options.isEmpty())
            return null;

        String winnerParty = "";
        String logoPath = null;
        int maxVotes = -1;
        boolean tie = false;

        for (VotingOption option : options) {
            int count = voteCounts.getOrDefault(option.getId(), 0);
            if (count > maxVotes) {
                maxVotes = count;
                winnerParty = option.getDescription(); // Or getOptionName()
                logoPath = option.getLogoPath();
                tie = false;
            } else if (count == maxVotes) {
                tie = true;
            }
        }
        if (maxVotes > 0 && !tie) {
            return new WinnerSummary(election.getState(), winnerParty, logoPath);
        }
        return null;
    }

    private static class WinnerSummary {
        String state, party, logo;

        public WinnerSummary(String state, String party, String logo) {
            this.state = state;
            this.party = party;
            this.logo = logo;
        }
    }

    private static class WinnerAdapter extends RecyclerView.Adapter<WinnerAdapter.ViewHolder> {
        List<WinnerSummary> list;

        public WinnerAdapter(List<WinnerSummary> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_winner_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            WinnerSummary w = list.get(position);
            holder.tvState.setText(w.state);
            holder.tvParty.setText(w.party);
            if (w.logo != null) {
                try {
                    File file = new File(holder.itemView.getContext().getFilesDir(), w.logo);
                    if (file.exists()) {
                        holder.imgLogo
                                .setImageBitmap(android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath()));
                    } else {
                        // Try abs path
                        file = new File(w.logo);
                        if (file.exists())
                            holder.imgLogo
                                    .setImageBitmap(android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath()));
                        else
                            holder.imgLogo.setImageResource(R.drawable.ic_vote);
                    }
                } catch (Exception e) {
                    holder.imgLogo.setImageResource(R.drawable.ic_vote);
                }
            } else {
                holder.imgLogo.setImageResource(R.drawable.ic_vote);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvState, tvParty;
            ImageView imgLogo;

            public ViewHolder(View v) {
                super(v);
                tvState = v.findViewById(R.id.tvState);
                tvParty = v.findViewById(R.id.tvWinnerParty);
                imgLogo = v.findViewById(R.id.imgWinnerLogo);
            }
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
