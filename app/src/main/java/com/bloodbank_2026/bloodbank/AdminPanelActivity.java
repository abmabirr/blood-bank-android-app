package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminPanelActivity extends AppCompatActivity {

    private ImageView adminBackBtn;
    private Button manageRequestsBtn, manageDonorsBtn, sendNoticePanelBtn, manageNoticesBtn;
    private TextView adminTotalDonors, adminTotalRequests;
    private DatabaseReference donorsRef, requestsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        adminBackBtn = findViewById(R.id.adminBackBtn);
        manageRequestsBtn = findViewById(R.id.manageRequestsBtn);
        manageDonorsBtn = findViewById(R.id.manageDonorsBtn);
        sendNoticePanelBtn = findViewById(R.id.sendNoticePanelBtn);
        manageNoticesBtn = findViewById(R.id.manageNoticesBtn);
        adminTotalDonors = findViewById(R.id.adminTotalDonors);
        adminTotalRequests = findViewById(R.id.adminTotalRequests);

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        donorsRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users");
        requestsRef = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");

        adminBackBtn.setOnClickListener(v -> finish());

        sendNoticePanelBtn.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AdminNoticeActivity.class));
        });

        manageNoticesBtn.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AppNoticesActivity.class);
            intent.putExtra("isAdmin", true);
            startActivity(intent);
        });

        manageRequestsBtn.setOnClickListener(v -> {
            // Re-using NotificationsActivity logic or creating a specialized admin view
            // For now, let's just show the list. In a full system, you'd add a "Delete" button there.
            android.content.Intent intent = new android.content.Intent(this, NotificationsActivity.class);
            intent.putExtra("isAdmin", true);
            startActivity(intent);
        });

        manageDonorsBtn.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, SearchDonorActivity.class);
            intent.putExtra("isAdmin", true);
            startActivity(intent);
        });

        findViewById(R.id.changeAdminPassBtn).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ChangeAdminPasswordActivity.class));
        });

        loadStats();
    }

    private void loadStats() {
        donorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminTotalDonors.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminTotalRequests.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
