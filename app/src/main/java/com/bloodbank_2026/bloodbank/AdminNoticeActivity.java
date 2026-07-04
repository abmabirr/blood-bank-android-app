package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminNoticeActivity extends AppCompatActivity {

    private EditText noticeTitleEdt, noticeMessageEdt;
    private Button sendNoticeBtn;
    private ImageView noticeBackBtn;
    private DatabaseReference noticeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notice);

        noticeTitleEdt = findViewById(R.id.noticeTitleEdt);
        noticeMessageEdt = findViewById(R.id.noticeMessageEdt);
        sendNoticeBtn = findViewById(R.id.sendNoticeBtn);
        noticeBackBtn = findViewById(R.id.noticeBackBtn);

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        noticeRef = FirebaseDatabase.getInstance(dbUrl).getReference("AppNotices");

        noticeBackBtn.setOnClickListener(v -> finish());

        sendNoticeBtn.setOnClickListener(v -> {
            String title = noticeTitleEdt.getText().toString().trim();
            String message = noticeMessageEdt.getText().toString().trim();
            String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String noticeId = noticeRef.push().getKey();
            Notice notice = new Notice(noticeId, title, message, timestamp);

            if (noticeId != null) {
                noticeRef.child(noticeId).setValue(notice).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Notice sent to all users via Background Service!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
