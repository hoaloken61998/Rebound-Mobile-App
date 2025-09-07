package com.rebound.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rebound.R;
import com.rebound.data.OnboardingData;
import com.rebound.models.Onboarding.OnboardingItem;


import java.util.List;

public class OnBoardingActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView descTextView;
    private ImageView leftArrow, rightArrow;
    private TextView txtOnBoardingButtonSkip;

    private List<OnboardingItem> onboardingItems;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        titleTextView = findViewById(R.id.txtOnboardingTitle);
        descTextView = findViewById(R.id.txtOnboardingDescription);
        leftArrow = findViewById(R.id.btnOnboardingBack);
        rightArrow = findViewById(R.id.btnOnboardingNext);
        txtOnBoardingButtonSkip = findViewById(R.id.txtOnBoardingButtonSkip);

        // Lấy dữ liệu onboarding
        onboardingItems = OnboardingData.getItems();

        // Hiển thị nội dung đầu tiên
        updateContent(currentIndex);

        // Xử lý mũi tên phải
        rightArrow.setOnClickListener(view -> {
            if (currentIndex < onboardingItems.size() - 1) {
                currentIndex++;
                updateContent(currentIndex);
            } else {
                startActivity(new Intent(OnBoardingActivity.this, NavBarActivity.class));
                finish();
            }
        });

        // Xử lý mũi tên trái
        leftArrow.setOnClickListener(view -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateContent(currentIndex);
            }
        });

        // Skip
        txtOnBoardingButtonSkip.setOnClickListener(v -> {
            startActivity(new Intent(OnBoardingActivity.this, NavBarActivity.class));
            finish();
        });
    }

    private void updateContent(int index) {
        OnboardingItem item = onboardingItems.get(index);

        titleTextView.setText(item.title);
        descTextView.setText(item.description);

        // Dots
        View dot1 = findViewById(R.id.dot1);
        View dot2 = findViewById(R.id.dot2);
        View dot3 = findViewById(R.id.dot3);

        dot1.setSelected(false);
        dot2.setSelected(false);
        dot3.setSelected(false);

        switch (index) {
            case 0: dot1.setSelected(true); break;
            case 1: dot2.setSelected(true); break;
            case 2: dot3.setSelected(true); break;
        }
    }
}
