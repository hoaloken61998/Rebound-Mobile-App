package com.rebound.main;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.rebound.R;

public class NewsDetailActivity extends AppCompatActivity {

    private TextView txtDetailNewsTitle, txtDescriptionDetailNews, txtDateDetailNews;
    private ImageView imgDetailNews, imgBackNewsDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        txtDetailNewsTitle = findViewById(R.id.txtDetailNewsTitle);
        txtDescriptionDetailNews = findViewById(R.id.txtDescriptionDetailNews);
        txtDateDetailNews = findViewById(R.id.txtDateDetailNews);
        imgDetailNews = findViewById(R.id.imgDetailNews);
        imgBackNewsDetail = findViewById(R.id.imgBackNewsDetail);

        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        String date = getIntent().getStringExtra("date");
        String imageUrl = getIntent().getStringExtra("imageUrl"); // ✅ nhận URL từ Firebase

        txtDetailNewsTitle.setText(title);
        txtDescriptionDetailNews.setText(desc);
        txtDateDetailNews.setText(date);
        imgBackNewsDetail.setOnClickListener(v -> finish());

        // ✅ Load ảnh từ URL (Firebase)
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher) // fallback nếu lỗi hoặc chưa load xong
                .into(imgDetailNews);
    }
}
