package com.rebound.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.rebound.models.Main.NewsItem;
import com.rebound.callback.FirebaseListCallback;

import java.util.ArrayList;

public class FirebaseNewsFetcher {

    public static void getAllNews(FirebaseListCallback<NewsItem> callback) {
        FirebaseDatabase.getInstance()
                .getReference("News")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    ArrayList<NewsItem> newsList = new ArrayList<>();
                    for (DataSnapshot newsSnap : dataSnapshot.getChildren()) {
                        NewsItem item = newsSnap.getValue(NewsItem.class);
                        if (item != null) {
                            newsList.add(item);
                        }
                    }
                    callback.onSuccess(newsList);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage() != null ? e.getMessage() : "Failed to fetch news.");
                });
    }
}
