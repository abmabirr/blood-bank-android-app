package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView backBtn = findViewById(R.id.aboutBackBtn);
        backBtn.setOnClickListener(v -> finish());
    }
}
