package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmergencyDetailActivity extends AppCompatActivity {

    TextView detailBlood, detailLocation, detailPhone, detailMessage, detailTime;
    Button detailDoneBtn, detailDeleteBtn, detailCallBtn;
    ImageView detailBackBtn;
    String requestId, phone;
    boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);

        detailBlood = findViewById(R.id.detailBlood);
        detailLocation = findViewById(R.id.detailLocation);
        detailPhone = findViewById(R.id.detailPhone);
        detailMessage = findViewById(R.id.detailMessage);
        detailTime = findViewById(R.id.detailTime);
        detailDoneBtn = findViewById(R.id.detailDoneBtn);
        detailDeleteBtn = findViewById(R.id.detailDeleteBtn);
        detailBackBtn = findViewById(R.id.detailBackBtn);
        detailCallBtn = findViewById(R.id.detailCallBtn);

        requestId = getIntent().getStringExtra("requestId");
        String blood = getIntent().getStringExtra("blood");
        String location = getIntent().getStringExtra("location");
        phone = getIntent().getStringExtra("phone");
        String message = getIntent().getStringExtra("message");
        String time = getIntent().getStringExtra("time");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (isAdmin) {
            detailDeleteBtn.setVisibility(android.view.View.VISIBLE);
        }

        detailBlood.setText("Blood Required: " + blood);
        detailLocation.setText("📍 Location: " + location);
        detailPhone.setText("📞 Phone: " + phone);
        detailMessage.setText(message == null || message.isEmpty() ? "Urgent help needed!" : message);
        detailTime.setText("Posted: " + time);

        detailBackBtn.setOnClickListener(v -> finish());

        detailCallBtn.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        detailDoneBtn.setOnClickListener(v -> {
            markAsDonated();
        });

        detailDeleteBtn.setOnClickListener(v -> {
            deleteRequest();
        });
    }

    private void deleteRequest() {
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Error: Request ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this emergency request?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
                    FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests").child(requestId)
                            .removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Request deleted successfully.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Toast.makeText(this, "Failed to delete: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markAsDonated() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://blood-bankk-a12ee-default-rtdb.firebaseio.com/").getReference("Users").child(userId);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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
                    java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                    updates.put("donationCount", currentCount + 1);
                    updates.put("lastDonationDate", currentDate);

                    userRef.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Optionally remove the request if you want it to disappear after someone donates
                            // String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
                            // FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests").child(requestId).removeValue();

                            new MaterialAlertDialogBuilder(EmergencyDetailActivity.this)
                                    .setView(R.layout.dialog_thanks)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            Toast.makeText(EmergencyDetailActivity.this, "Update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
