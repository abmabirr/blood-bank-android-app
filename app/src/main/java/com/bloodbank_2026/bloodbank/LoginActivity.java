package com.bloodbank_2026.bloodbank;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView registerBtn, forgotPassword, adminLoginRedirect;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        adminLoginRedirect = findViewById(R.id.adminLoginRedirect);

        auth = FirebaseAuth.getInstance();

        adminLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoginActivity.class));
        });

        forgotPassword.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            if (userEmail.isEmpty()) {
                Toast.makeText(this, "Enter email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                }
            });
        });

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}