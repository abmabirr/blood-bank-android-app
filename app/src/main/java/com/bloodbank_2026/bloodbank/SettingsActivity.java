package com.bloodbank_2026.bloodbank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    ImageView backBtn;
    CardView settingsProfileBtn;
    SwitchMaterial darkModeSwitch, notifSwitch;
    LinearLayout aboutBtn, logoutSettingsBtn;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backBtn = findViewById(R.id.backBtn);
        settingsProfileBtn = findViewById(R.id.settingsProfileBtn);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notifSwitch = findViewById(R.id.notifSwitch);
        aboutBtn = findViewById(R.id.aboutBtn);
        logoutSettingsBtn = findViewById(R.id.logoutSettingsBtn);

        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load Dark Mode state
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        darkModeSwitch.setChecked(isDarkMode);

        backBtn.setOnClickListener(v -> finish());

        settingsProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("DarkMode", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("DarkMode", false);
            }
            editor.apply();
        });

        notifSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        aboutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
        });

        logoutSettingsBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
