package com.rebound.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rebound.R;
import com.rebound.utils.LocaleHelper;


public class LanguageSelectorActivity extends AppCompatActivity {

    private ImageView iconEnglishTick, iconVietnameseTick;
    private RelativeLayout rowEnglish, rowVietnamese;
    private TextView txtTitle, txtEnglish, txtVietnamese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load saved locale before UI inflation
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String langCode = prefs.getString("language", "en");
        LocaleHelper.setLocale(this, langCode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selector);

        // Init views
        iconEnglishTick = findViewById(R.id.iconEnglishTick);
        iconVietnameseTick = findViewById(R.id.iconVietnameseTick);
        rowEnglish = findViewById(R.id.rowEnglish);
        rowVietnamese = findViewById(R.id.rowVietnamese);
        ImageView imgBack = findViewById(R.id.imgBack);

        txtTitle = findViewById(R.id.txtTitle);
        txtEnglish = findViewById(R.id.txtEnglish);
        txtVietnamese = findViewById(R.id.txtVietnamese);

        // Back event
        imgBack.setOnClickListener(v -> finish());

        // Tick state
        setLanguageUI(langCode);

        // Click events
        rowEnglish.setOnClickListener(v -> changeLanguage("en"));
        rowVietnamese.setOnClickListener(v -> changeLanguage("vi"));
    }

    private void changeLanguage(String langCode) {
        // Save language
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("language", langCode).apply();

        // Set locale
        LocaleHelper.setLocale(this, langCode);

        // Restart current activity to apply language
        recreate();
    }

    private void setLanguageUI(String langCode) {
        iconEnglishTick.setVisibility("en".equals(langCode) ? View.VISIBLE : View.GONE);
        iconVietnameseTick.setVisibility("vi".equals(langCode) ? View.VISIBLE : View.GONE);
    }
}
