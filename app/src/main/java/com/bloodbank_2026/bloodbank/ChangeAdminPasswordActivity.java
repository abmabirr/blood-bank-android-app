package com.bloodbank_2026.bloodbank;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class ChangeAdminPasswordActivity extends AppCompatActivity {

    private EditText currentPassword, newPassword, confirmPassword;
    private Button updatePasswordBtn;
    private ImageView backBtn;
    private DatabaseReference adminRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_admin_password);

        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        updatePasswordBtn = findViewById(R.id.updatePasswordBtn);
        backBtn = findViewById(R.id.backBtn);

        String dbUrl = "https://blood-bankk-a12ee-default-rtdb.firebaseio.com/";
        adminRef = FirebaseDatabase.getInstance(dbUrl).getReference("AdminSettings");

        backBtn.setOnClickListener(v -> finish());

        updatePasswordBtn.setOnClickListener(v -> {
            String current = currentPassword.getText().toString().trim();
            String newVal = newPassword.getText().toString().trim();
            String confirm = confirmPassword.getText().toString().trim();

            if (current.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newVal.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newVal.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify current password from database first
            adminRef.child("password").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String dbPass = task.getResult().getValue(String.class);
                    // Default if not set yet
                    if (dbPass == null) dbPass = "admin123";

                    if (current.equals(dbPass)) {
                        // Update to new password
                        adminRef.child("password").setValue(newVal).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Current password is incorrect!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Database error!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
