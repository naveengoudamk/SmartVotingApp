package com.example.smartvotingapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminHomeFragment extends Fragment implements NewsManager.NewsUpdateListener {

    private LinearLayout newsContainer;
    private NewsManager newsManager;

    public AdminHomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

            newsManager = new NewsManager(getContext());
            newsManager.addListener(this); // Add real-time listener

            newsContainer = view.findViewById(R.id.newsContainer);
            Button btnAddNews = view.findViewById(R.id.btnAddNews);
            Button btnViewFeedback = view.findViewById(R.id.btnViewFeedback);

            btnAddNews.setOnClickListener(v -> showAddEditDialog(null));

            btnViewFeedback.setOnClickListener(v -> {
                try {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new AdminFeedbackFragment())
                            .addToBackStack(null)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            loadNews();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading home: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            getActivity().runOnUiThread(this::loadNews);
        }
    }

    private void loadNews() {
        if (newsContainer == null)
            return;
        newsContainer.removeAllViews();
        List<News> newsList = newsManager.getAllNews();

        if (newsList.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("No news added yet. Click 'Add News' to create one.");
            emptyView.setPadding(32, 32, 32, 32);
            emptyView.setTextSize(16);
            newsContainer.addView(emptyView);
            return;
        }

        for (News news : newsList) {
            View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_admin, newsContainer, false);

            TextView title = newsView.findViewById(R.id.tvTitle);
            TextView date = newsView.findViewById(R.id.tvDate);
            TextView desc = newsView.findViewById(R.id.tvDescription);
            android.widget.ImageView imgNews = newsView.findViewById(R.id.imgNews);
            Button btnEdit = newsView.findViewById(R.id.btnEdit);
            Button btnDelete = newsView.findViewById(R.id.btnDelete);

            title.setText(news.getTitle());
            date.setText(news.getDate());
            desc.setText(news.getDescription());

            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                imgNews.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        java.io.InputStream in = new java.net.URL(news.getImageUrl()).openStream();
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                        newsView.post(() -> imgNews.setImageBitmap(bmp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                imgNews.setVisibility(View.GONE);
            }

            btnEdit.setOnClickListener(v -> showAddEditDialog(news));
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete News")
                        .setMessage("Are you sure you want to delete this news?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            newsManager.deleteNews(news.getId());
                            Toast.makeText(getContext(), "News deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            newsContainer.addView(newsView);
        }
    }

    private androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher;
    private EditText currentImageUrlInput;
    private android.widget.ImageView currentImagePreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Persist permission so we can read it later even after restart
                        try {
                            getContext().getContentResolver().takePersistableUriPermission(uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (currentImageUrlInput != null) {
                            currentImageUrlInput.setText(uri.toString());
                        }
                        if (currentImagePreview != null) {
                            currentImagePreview.setVisibility(View.VISIBLE);
                            currentImagePreview.setImageURI(uri);
                        }
                    }
                });
    }

    private void showAddEditDialog(News news) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_news, null);
        builder.setView(view);
        builder.setTitle(news == null ? "Add News" : "Edit News");

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etImageUrl = view.findViewById(R.id.etImageUrl);
        Button btnPickImage = view.findViewById(R.id.btnPickImage);
        android.widget.ImageView imgPreview = view.findViewById(R.id.imgPreview);

        // Save references for the callback
        currentImageUrlInput = etImageUrl;
        currentImagePreview = imgPreview;

        if (news != null) {
            etTitle.setText(news.getTitle());
            etDescription.setText(news.getDescription());
            etImageUrl.setText(news.getImageUrl());
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(news.getImageUrl());
                    if (uri.getScheme() != null
                            && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                        imgPreview.setVisibility(View.VISIBLE);
                        imgPreview.setImageURI(uri);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getContext(), "Please fill title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            long timestamp = System.currentTimeMillis();

            if (news == null) {
                // Add new
                News newNews = new News(UUID.randomUUID().toString(), title, desc, date, timestamp, imageUrl);
                newsManager.addNews(newNews);
                Toast.makeText(getContext(), "News added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Update existing
                News updatedNews = new News(news.getId(), title, desc, date, timestamp, imageUrl);
                newsManager.updateNews(updatedNews);
                Toast.makeText(getContext(), "News updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
