package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class SearchDonorActivity extends AppCompatActivity {

    EditText bloodInput, locationInput;
    TextView result;
    ProgressBar progressBar;
    DatabaseReference ref;
    private boolean isDataReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_donor);

        bloodInput = findViewById(R.id.bloodInput);
        locationInput = findViewById(R.id.locationInput);
        result = findViewById(R.id.result);
        progressBar = findViewById(R.id.progressBar);
        result.setMovementMethod(LinkMovementMethod.getInstance());

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com";
        
        try {
            ref = FirebaseDatabase.getInstance(dbUrl).getReference("Users");
        } catch (Exception e) {
            result.setText("Database Config Error: " + e.getMessage());
        }

        findViewById(R.id.searchBtn).setOnClickListener(v -> {
            String searchBlood = bloodInput.getText().toString().trim().toUpperCase().replaceAll("\\s","");
            String searchLoc = locationInput.getText().toString().trim().toLowerCase();

            if (searchBlood.isEmpty() && searchLoc.isEmpty()) {
                Toast.makeText(this, "Please enter blood group or location", Toast.LENGTH_SHORT).show();
                return;
            }

            isDataReceived = false;
            progressBar.setVisibility(View.VISIBLE);
            result.setText("Searching donors...");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    isDataReceived = true;
                    progressBar.setVisibility(View.GONE);
                    
                    if (!snapshot.exists()) {
                        result.setText("❌ No data found. Please register a donor first.");
                        return;
                    }

                    StringBuilder data = new StringBuilder();
                    int count = 0;

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user == null) continue;

                        String userBlood = (user.blood != null) ? user.blood.trim().toUpperCase().replaceAll("\\s","") : "";
                        String userLoc = (user.location != null) ? user.location.trim().toLowerCase() : "";

                        boolean bloodMatch = searchBlood.isEmpty() || userBlood.equals(searchBlood);
                        boolean locMatch = searchLoc.isEmpty() || userLoc.contains(searchLoc);

                        if (bloodMatch && locMatch) {
                            count++;
                            String displayPhone = (user.phone != null && !user.phone.isEmpty()) ? user.phone : "Not provided";
                            String phoneLink = (user.phone != null && !user.phone.isEmpty()) ? 
                                    "<a href=\"tel:" + user.phone + "\">" + user.phone + "</a>" : "<i>Not provided</i>";

                            data.append("<b>Donor #").append(count).append("</b><br>")
                                .append("Blood: ").append(user.blood).append("<br>")
                                .append("Location: ").append(user.location != null && !user.location.isEmpty() ? user.location : "Not specified").append("<br>")
                                .append("Phone: ").append(phoneLink).append("<br>")
                                .append("-----------------------<br><br>");
                        }
                    }

                    if(count == 0){
                        result.setText("No donors found for your criteria.");
                    } else {
                        String header = "<b>Found " + count + " donors:</b><br><br>";
                        result.setText(Html.fromHtml(header + data.toString()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    isDataReceived = true;
                    progressBar.setVisibility(View.GONE);
                    result.setText("❌ Firebase Error: " + error.getMessage());
                }
            });
        });
    }
}
