package com.rebound.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rebound.R;



public class NoNotificationActivity extends AppCompatActivity {

    Button btnGoStore;
    ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_no_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnGoStore = findViewById(R.id.btnNoNotificationGoStore);
        imgBack = findViewById(R.id.imgBack);

        btnGoStore.setOnClickListener(v -> {
            Intent intent = new Intent(NoNotificationActivity.this, NavBarActivity.class);
            intent.putExtra("fragmentToShow", "main");
            startActivity(intent);
            finish();
        });

        imgBack.setOnClickListener(v -> {
            finish();
        });
    }
}