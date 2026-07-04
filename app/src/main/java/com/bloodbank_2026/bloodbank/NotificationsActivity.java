package com.bloodbank_2026.bloodbank;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Object> notificationList; 
    private DatabaseReference requestsRef;
    private View emptyStateTxt;
    private ImageView backBtn;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateTxt = findViewById(R.id.emptyStateTxt);
        backBtn = findViewById(R.id.backBtn);

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        
        adapter = new NotificationsAdapter(notificationList, item -> {
            if (item instanceof EmergencyRequest) {
                EmergencyRequest request = (EmergencyRequest) item;
                Intent intent = new Intent(this, EmergencyDetailActivity.class);
                intent.putExtra("requestId", request.requestId);
                intent.putExtra("blood", request.bloodGroup);
                intent.putExtra("location", request.location);
                intent.putExtra("phone", request.phone);
                intent.putExtra("message", request.message);
                intent.putExtra("time", request.timestamp);
                intent.putExtra("isAdmin", isAdmin);
                startActivity(intent);
            }
        }, item -> {
            if (isAdmin && item instanceof EmergencyRequest) {
                showDeleteDialog((EmergencyRequest) item);
            }
        });
        
        recyclerView.setAdapter(adapter);
        backBtn.setOnClickListener(v -> finish());

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        requestsRef = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");

        loadRequests();
    }

    private void showDeleteDialog(EmergencyRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this blood request?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (request.requestId != null) {
                        requestsRef.child(request.requestId).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(NotificationsActivity.this, "Request deleted successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(NotificationsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadRequests() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    EmergencyRequest req = ds.getValue(EmergencyRequest.class);
                    if (req != null) {
                        if (req.requestId == null) req.requestId = ds.getKey();
                        notificationList.add(req);
                    }
                }
                
                Collections.reverse(notificationList);
                adapter.notifyDataSetChanged();

                if (notificationList.isEmpty()) {
                    emptyStateTxt.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateTxt.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
