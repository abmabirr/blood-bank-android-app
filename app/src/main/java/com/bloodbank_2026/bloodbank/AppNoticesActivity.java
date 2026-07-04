package com.bloodbank_2026.bloodbank;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

public class AppNoticesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoticesAdapter adapter;
    private List<Notice> noticeList;
    private DatabaseReference noticeRef;
    private TextView noNoticesTxt;
    private ImageView noticesBackBtn;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notices);

        // Check if user is admin (passed from Dashboard)
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        recyclerView = findViewById(R.id.noticesRecyclerView);
        noNoticesTxt = findViewById(R.id.noNoticesTxt);
        noticesBackBtn = findViewById(R.id.noticesBackBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noticeList = new ArrayList<>();

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        noticeRef = FirebaseDatabase.getInstance(dbUrl).getReference("AppNotices");

        adapter = new NoticesAdapter(noticeList, notice -> {
            if (isAdmin) {
                showDeleteDialog(notice);
            }
        });
        
        recyclerView.setAdapter(adapter);
        noticesBackBtn.setOnClickListener(v -> finish());

        loadNotices();
    }

    private void showDeleteDialog(Notice notice) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notice")
                .setMessage("Are you sure you want to delete this notice?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (notice.noticeId != null) {
                        noticeRef.child(notice.noticeId).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(AppNoticesActivity.this, "Notice deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AppNoticesActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadNotices() {
        noticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticeList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notice notice = data.getValue(Notice.class);
                    if (notice != null) {
                        // Ensure noticeId is set from key if missing
                        if (notice.noticeId == null) notice.noticeId = data.getKey();
                        noticeList.add(notice);
                    }
                }
                Collections.reverse(noticeList);
                adapter.notifyDataSetChanged();

                if (noticeList.isEmpty()) {
                    noNoticesTxt.setVisibility(View.VISIBLE);
                } else {
                    noNoticesTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppNoticesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
