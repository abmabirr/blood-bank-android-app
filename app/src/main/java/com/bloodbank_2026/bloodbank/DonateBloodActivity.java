package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DonateBloodActivity extends AppCompatActivity {

    EditText locationInput, bloodInput, phoneInput;
    Button submitBtn;
    DatabaseReference ref;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_blood);

        locationInput = findViewById(R.id.locationInput);
        bloodInput = findViewById(R.id.bloodInput);
        phoneInput = findViewById(R.id.phoneInput);
        submitBtn = findViewById(R.id.submitBtn);

        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance("https://blood-bankk-a12ee-default-rtdb.firebaseio.com/").getReference("Donors");

        submitBtn.setOnClickListener(v -> {
            String location = locationInput.getText().toString().trim();
            String blood = bloodInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String userId = auth.getCurrentUser().getUid();

            if (location.isEmpty() || blood.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ইউজারের নাম প্রোফাইল থেকে নিয়ে ডোনার লিস্টে সেভ করা
            FirebaseDatabase.getInstance("https://blood-bankk-a12ee-default-rtdb.firebaseio.com/").getReference("Users").child(userId)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue() != null ? snapshot.child("name").getValue().toString() : "Unknown";

                        HashMap<String, String> donorMap = new HashMap<>();
                        donorMap.put("name", name);
                        donorMap.put("phone", phone);
                        donorMap.put("blood", blood);
                        donorMap.put("location", location);
                        donorMap.put("uid", userId);

                        ref.child(userId).setValue(donorMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // ডোনেশন কাউন্ট বাড়ানো এবং তারিখ আপডেট করা
                                int currentCount = 0;
                                Object countObj = snapshot.child("donationCount").getValue();
                                if (countObj != null) {
                                    if (countObj instanceof Long) {
                                        currentCount = ((Long) countObj).intValue();
                                    } else if (countObj instanceof String) {
                                        try {
                                            currentCount = Integer.parseInt((String) countObj);
                                        } catch (NumberFormatException e) {
                                            currentCount = 0;
                                        }
                                    }
                                }

                                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                                
                                HashMap<String, Object> updates = new HashMap<>();
                                updates.put("donationCount", currentCount + 1);
                                updates.put("lastDonationDate", currentDate);
                                // Also update phone and location in user profile for consistency
                                updates.put("phone", phone);
                                updates.put("location", location);

                                FirebaseDatabase.getInstance("https://blood-bankk-a12ee-default-rtdb.firebaseio.com/")
                                    .getReference("Users").child(userId)
                                    .updateChildren(updates);

                                Toast.makeText(this, "Success! Your blood donation has been logged.", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "User profile not found! Please update profile first.", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
}