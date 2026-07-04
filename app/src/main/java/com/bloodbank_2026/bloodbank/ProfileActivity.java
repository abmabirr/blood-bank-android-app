package com.bloodbank_2026.bloodbank;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    EditText profileName, profileBlood, profilePhone, profileLastDonation;
    TextView nextDonationStatus, profileDonationCount;
    Button logoutBtn, updateBtn;
    FirebaseAuth auth;
    DatabaseReference ref;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileBlood = findViewById(R.id.profileBlood);
        profilePhone = findViewById(R.id.profilePhone);
        profileLastDonation = findViewById(R.id.profileLastDonation);
        profileDonationCount = findViewById(R.id.profileDonationCount);
        nextDonationStatus = findViewById(R.id.nextDonationStatus);
        logoutBtn = findViewById(R.id.logoutBtn);
        updateBtn = findViewById(R.id.updateBtn);

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        profileLastDonation.setOnClickListener(v -> new DatePickerDialog(ProfileActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Using the database URL directly
            ref = FirebaseDatabase.getInstance("https://blood-bankk-a12ee-default-rtdb.firebaseio.com/").getReference("Users").child(userId);

            // Data load
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // trying to use POJO directly
                        User user = snapshot.getValue(User.class);
                        
                        if (user != null && user.name != null) {
                            profileName.setText(user.name);
                            profileBlood.setText(user.blood != null ? user.blood : "");
                            profilePhone.setText(user.phone != null ? user.phone : "");
                            profileLastDonation.setText(user.lastDonationDate != null ? user.lastDonationDate : "Never");
                            profileDonationCount.setText(String.valueOf(user.donationCount));
                            calculateNextDonation(user.lastDonationDate);
                        } else {
                            // if POJO fail...take data manually
                            String name = snapshot.child("name").getValue() != null ? snapshot.child("name").getValue().toString() : "";
                            String blood = snapshot.child("blood").getValue() != null ? snapshot.child("blood").getValue().toString() : "";
                            String phone = snapshot.child("phone").getValue() != null ? snapshot.child("phone").getValue().toString() : "";
                            String lastDate = snapshot.child("lastDonationDate").getValue() != null ? snapshot.child("lastDonationDate").getValue().toString() : "Never";

                            profileName.setText(name);
                            profileBlood.setText(blood);
                            profilePhone.setText(phone);
                            profileLastDonation.setText(lastDate);
                            String count = snapshot.child("donationCount").getValue() != null ? snapshot.child("donationCount").getValue().toString() : "0";
                            profileDonationCount.setText(count);
                            calculateNextDonation(lastDate);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Profile not found in Database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Read Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // update profile
            updateBtn.setOnClickListener(v -> {
                String newName = profileName.getText().toString().trim();
                String newBlood = profileBlood.getText().toString().trim();
                String newPhone = profilePhone.getText().toString().trim();
                String newLastDate = profileLastDonation.getText().toString().trim();

                HashMap<String, Object> map = new HashMap<>();
                map.put("name", newName);
                map.put("blood", newBlood);
                map.put("phone", newPhone);
                map.put("lastDonationDate", newLastDate);

                ref.updateChildren(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        profileLastDonation.setText(sdf.format(myCalendar.getTime()));
    }

    private void calculateNextDonation(String lastDate) {
        if (lastDate == null || lastDate.equals("Never") || lastDate.isEmpty()) {
            nextDonationStatus.setText("Next Eligibility: You are eligible to donate now!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date date = sdf.parse(lastDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DAY_OF_YEAR, 120);
            Date nextDate = c.getTime();

            long diff = nextDate.getTime() - new Date().getTime();
            long daysLeft = diff / (24 * 60 * 60 * 1000);

            if (daysLeft <= 0) {
                nextDonationStatus.setText("Next Eligibility: You are eligible to donate now!");
            } else {
                nextDonationStatus.setText("Next Eligibility: " + sdf.format(nextDate) + " (In " + daysLeft + " days)");
            }
        } catch (ParseException e) {
            nextDonationStatus.setText("Next Eligibility: Invalid Date");
        }
    }
}