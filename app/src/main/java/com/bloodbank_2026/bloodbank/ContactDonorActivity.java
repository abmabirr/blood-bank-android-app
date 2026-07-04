package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ContactDonorActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    DatabaseReference ref;
    EmergencyRequestAdapter adapter;
    List<EmergencyRequest> requestList;
    List<String> requestIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_donor);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.contactProgress);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        requestIds = new ArrayList<>();
        adapter = new EmergencyRequestAdapter(this, requestList, requestIds);
        recyclerView.setAdapter(adapter);

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com";
        ref = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");

        loadEmergencyRequests();
    }

    private void loadEmergencyRequests() {
        progressBar.setVisibility(View.VISIBLE);

        ref.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                requestList.clear();
                requestIds.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(ContactDonorActivity.this, "No emergency requests found.", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    EmergencyRequest req = ds.getValue(EmergencyRequest.class);
                    if (req != null) {
                        requestList.add(0, req); // Show latest first
                        requestIds.add(0, ds.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ContactDonorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
