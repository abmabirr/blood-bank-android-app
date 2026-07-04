package com.bloodbank_2026.bloodbank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText adminEmail, adminPassword;
    private Button adminLoginBtn;
    private TextView backToUserLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        adminLoginBtn = findViewById(R.id.adminLoginBtn);
        backToUserLogin = findViewById(R.id.backToUserLogin);
        auth = FirebaseAuth.getInstance();

        adminLoginBtn.setOnClickListener(v -> {
            String email = adminEmail.getText().toString().trim();
            String password = adminPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ডাটাবেজ থেকে এডমিন পাসওয়ার্ড চেক করা
            String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
            com.google.firebase.database.DatabaseReference adminRef = com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl).getReference("AdminSettings");

            adminRef.child("password").get().addOnCompleteListener(task -> {
                String dbPass = "admin123"; // ডিফল্ট পাসওয়ার্ড
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    dbPass = task.getResult().getValue(String.class);
                }

                if (email.equals("admin@bloodbank.com") && password.equals(dbPass)) {
                    Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminPanelActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Firebase Auth fallback
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            if (email.endsWith("@admin.com")) {
                                startActivity(new Intent(this, AdminPanelActivity.class));
                                finish();
                            } else {
                                auth.signOut();
                                Toast.makeText(this, "Access Denied: Not an admin account.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Login Failed: Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

        backToUserLogin.setOnClickListener(v -> finish());
    }
}
