package com.bloodbank_2026.bloodbank;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {

    private DatabaseReference requestsRef, noticesRef;
    private boolean isInitialRequestsLoaded = false;
    private boolean isInitialNoticesLoaded = false;
    private static final String CHANNEL_ID = "BloodBankBackgroundService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        // Start as foreground service to prevent being killed
        NotificationCompat.Builder serviceNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blood Bank Active")
                .setContentText("Listening for emergency requests...")
                .setSmallIcon(R.drawable.ic_blood_drop)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, serviceNotificationBuilder.build(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, serviceNotificationBuilder.build());
        }

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        requestsRef = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");
        noticesRef = FirebaseDatabase.getInstance(dbUrl).getReference("AppNotices");

        setupListeners();
    }

    private void setupListeners() {
        // Emergency Requests Listener
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInitialRequestsLoaded = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        requestsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isInitialRequestsLoaded) {
                    String blood = snapshot.child("bloodGroup").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    if (blood != null) {
                        showNotification("Urgent " + blood + " Needed!", "Location: " + location, 101);
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Notices Listener
        noticesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isInitialNoticesLoaded = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        noticesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isInitialNoticesLoaded) {
                    String title = snapshot.child("title").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    if (title != null) {
                        showNotification(title, message, 102);
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showNotification(String title, String body, int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "blood_alert_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Blood Requests", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_blood_drop)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(id + (int)System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Restart service if it gets killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
