package com.bloodbank_2026.bloodbank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {

    EditText name, email, password, blood, phone, location;
    Button registerBtn;
    FirebaseAuth mAuth;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        blood = findViewById(R.id.blood);
        phone = findViewById(R.id.phone);
        location = findViewById(R.id.location);
        registerBtn = findViewById(R.id.registerBtn);

        mAuth = FirebaseAuth.getInstance();
        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com";
        ref = FirebaseDatabase.getInstance(dbUrl).getReference("Users");

        registerBtn.setOnClickListener(v -> {
            String userName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();
            String userBlood = blood.getText().toString().trim().toUpperCase();
            String userPhone = phone.getText().toString().trim();
            String userLoc = location.getText().toString().trim();

            if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty() || userBlood.isEmpty() || userPhone.isEmpty() || userLoc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();

                    // Fetch FCM Token for notifications
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                        String token = tokenTask.isSuccessful() ? tokenTask.getResult() : "";
                        
                        // Default lat/lng (0,0) - can be updated via profile
                        User user = new User(userName, userBlood, userPhone, userEmail, userLoc, 0.0, 0.0, token);
                        
                        ref.child(userId).setValue(user).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Database Error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
