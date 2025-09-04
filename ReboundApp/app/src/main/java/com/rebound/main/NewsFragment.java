package com.rebound.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rebound.R;
import com.rebound.adapters.NewsAdapter;
import com.rebound.models.Main.NewsItem;
import com.rebound.utils.FirebaseNewsFetcher;
import com.rebound.callback.FirebaseListCallback;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerViewNews;
    private NewsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        recyclerViewNews = view.findViewById(R.id.recyclerViewNews);
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gọi Firebase để lấy danh sách bài viết
        FirebaseNewsFetcher.getAllNews(new FirebaseListCallback<NewsItem>() {
            @Override
            public void onSuccess(ArrayList<NewsItem> newsItems) {
                adapter = new NewsAdapter(requireContext(), newsItems);
                recyclerViewNews.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), "Không thể tải tin tức: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
