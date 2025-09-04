package com.rebound.main;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rebound.R;
import com.rebound.data.TermsData;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dùng đúng layout file bạn đã đặt tên
        setContentView(R.layout.activity_privacy_policy);

        // Lấy ra các thành phần trong layout
        TextView txtCancelPolicy = findViewById(R.id.txtCancelPolicy);
        TextView txtTerms = findViewById(R.id.txtTerms);
        ImageView imgBack = findViewById(R.id.imgBack);

        // Gán sự kiện back
        imgBack.setOnClickListener(v -> finish());

        // Gán nội dung từ dữ liệu tĩnh
        txtCancelPolicy.setText(TermsData.getPolicyCancel(this));
        txtTerms.setText(TermsData.getTermsPlain(this));
    }
}