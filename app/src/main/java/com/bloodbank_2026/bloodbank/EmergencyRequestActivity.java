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

public class EmergencyRequestActivity extends AppCompatActivity {

    EditText reqBlood, reqLocation, reqPhone, reqMessage;
    Button submitReqBtn;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_request);

        reqBlood = findViewById(R.id.reqBlood);
        reqLocation = findViewById(R.id.reqLocation);
        reqPhone = findViewById(R.id.reqPhone);
        reqMessage = findViewById(R.id.reqMessage);
        submitReqBtn = findViewById(R.id.submitReqBtn);
        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        ref = FirebaseDatabase.getInstance(dbUrl).getReference("EmergencyRequests");

        submitReqBtn.setOnClickListener(v -> {
            String blood = reqBlood.getText().toString().trim().toUpperCase();
            String location = reqLocation.getText().toString().trim();
            String phone = reqPhone.getText().toString().trim();
            String message = reqMessage.getText().toString().trim();
            String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

            if (blood.isEmpty() || location.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String requestId = ref.push().getKey();
            EmergencyRequest request = new EmergencyRequest(requestId, blood, location, phone, message, timestamp);

            if (requestId != null) {
                ref.child(requestId).setValue(request).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send Push Notification to all users
                        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(
                                "/topics/all_users",
                                "Urgent Blood Request: " + blood,
                                "Location: " + location + "\nPhone: " + phone,
                                getApplicationContext()
                        );
                        notificationsSender.SendNotifications();

                        Toast.makeText(EmergencyRequestActivity.this, "Request Posted Successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(EmergencyRequestActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
