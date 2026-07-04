package com.bloodbank_2026.bloodbank;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.messaging.FirebaseMessaging;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    View searchBtn, donateBtn, profileBtn, emergencyBtn, allRequestsBtn, healthTipsBtn, aboutBtn;
    Button iHaveDonatedBtn;
    ImageView settingsBtn, appNoticeBtn;
    CardView notificationCard;
    TextView notificationText, totalDonorsTxt, totalRequestsTxt, myDonationsTxt, dashboardTitle;
    DatabaseReference ref, donorsRef, requestsRef, noticesRef;
    FirebaseAuth auth;
    private int adminTapCount = 0;
    private long lastTapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        searchBtn = findViewById(R.id.searchBtn);
        donateBtn = findViewById(R.id.donateBtn);
        profileBtn = findViewById(R.id.profileBtn);
        emergencyBtn = findViewById(R.id.emergencyBtn);
        allRequestsBtn = findViewById(R.id.allRequestsBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        appNoticeBtn = findViewById(R.id.appNoticeBtn);
        notificationCard = findViewById(R.id.notificationCard);
        iHaveDonatedBtn = findViewById(R.id.iHaveDonatedBtn);
        notificationText = findViewById(R.id.notificationText);
        totalDonorsTxt = findViewById(R.id.totalDonorsTxt);
        totalRequestsTxt = findViewById(R.id.totalRequestsTxt);
        myDonationsTxt = findViewById(R.id.myDonationsTxt);
        dashboardTitle = findViewById(R.id.dashboardTitle);
        healthTipsBtn = findViewById(R.id.healthTipsBtn);
        aboutBtn = findViewById(R.id.aboutBtn);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
            ref = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId);
            donorsRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users");
            requestsRef = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");
            noticesRef = FirebaseDatabase.getInstance(dbUrl).getReference("AppNotices");

            checkDonationEligibility();
            loadStats();
        }

        // Notification Permission check for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        appNoticeBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AppNoticesActivity.class));
        });

        dashboardTitle.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTapTime < 500) {
                adminTapCount++;
            } else {
                adminTapCount = 1;
            }
            lastTapTime = currentTime;

            if (adminTapCount == 5) {
                adminTapCount = 0; // Reset
                Toast.makeText(this, "Opening Admin Login...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminLoginActivity.class));
            }
        });

        searchBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SearchDonorActivity.class)));

        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        donateBtn.setOnClickListener(v ->
                startActivity(new Intent(this, DonateBloodActivity.class)));

        emergencyBtn.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyRequestActivity.class)));

        allRequestsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        healthTipsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HealthTipsActivity.class)));

        aboutBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        iHaveDonatedBtn.setOnClickListener(v -> updateDonationStatus());

        // Subscribe to global notifications topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users");

        // Start Background Notification Service
        Intent serviceIntent = new Intent(this, NotificationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Setup Automatic Notification Listener (No Server Key Needed)
        setupNotificationListener();
        setupNoticeListener();
    }

    private boolean isInitialDataLoaded = false;
    private boolean isInitialNoticeLoaded = false;

    private void setupNoticeListener() {
        if (noticesRef == null) return;

        // Skip initial data
        noticesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInitialNoticeLoaded = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        noticesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                if (isInitialNoticeLoaded) {
                    try {
                        String title = snapshot.child("title").getValue(String.class);
                        String message = snapshot.child("message").getValue(String.class);
                        if (title != null) {
                            showLocalNotification(title, message);
                        }
                    } catch (Exception e) {}
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupNotificationListener() {
        if (requestsRef == null) return;

        // Check if database connection is active
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInitialDataLoaded = true;
                Toast.makeText(DashboardActivity.this, "Blood Alert Service: Active", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        requestsRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                if (isInitialDataLoaded) {
                    try {
                        String blood = snapshot.child("bloodGroup").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        
                        if (blood != null) {
                            // Debug toast before showing notification
                            Toast.makeText(DashboardActivity.this, "New Request Found: " + blood, Toast.LENGTH_SHORT).show();
                            showLocalNotification("Urgent " + blood + " Needed!", "Location: " + location);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NotifyError", e.getMessage());
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showLocalNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "blood_alert_final_v1"; 

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Blood Requests", NotificationManager.IMPORTANCE_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_blood_drop)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true) // এটি নোটিফিকেশনটিকে স্ক্রিনের উপরে পপ-আপ করাবে
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void updateDonationStatus() {
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    ref.updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DashboardActivity.this, "Donation updated! Thank you.", Toast.LENGTH_SHORT).show();
                            notificationCard.setVisibility(View.GONE);
                            // Refresh stats
                            loadStats();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkDonationEligibility() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lastDate = snapshot.child("lastDonationDate").getValue() != null ? snapshot.child("lastDonationDate").getValue().toString() : "Never";
                    
                    // Update My Donations Count from Database
                    String donationCount = snapshot.child("donationCount").getValue() != null ? 
                            snapshot.child("donationCount").getValue().toString() : "0";
                    myDonationsTxt.setText(donationCount);

                    if (lastDate.equals("Never") || lastDate.isEmpty()) {
                        notificationCard.setVisibility(View.GONE);
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

                        if (daysLeft <= 7 && daysLeft > 0) {
                            notificationCard.setVisibility(View.VISIBLE);
                            notificationText.setText("Reminder: You will be eligible to donate blood in " + daysLeft + " days!");
                        } else if (daysLeft <= 0) {
                            notificationCard.setVisibility(View.VISIBLE);
                            notificationText.setText("Great news! You are now eligible to donate blood again.");
                        } else {
                            notificationCard.setVisibility(View.GONE);
                        }
                    } catch (ParseException e) {
                        notificationCard.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadStats() {
        // Load Total Donors
        donorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalDonorsTxt.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Load Total Emergency Requests
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalRequestsTxt.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
