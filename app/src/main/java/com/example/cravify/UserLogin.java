package com.example.cravify;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserLogin extends AppCompatActivity {

    private static final String TAG = "UserLogin";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText emailField, passwordField;
    private Button loginButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progBar);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"Logging you in...",Toast.LENGTH_SHORT).show();
            // User is logged in, fetch user details and navigate to MainActivity
            fetchUserDetails(currentUser.getEmail());
            return; // Exit onCreate early
        }

        // Location Permission Check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up click listeners
        findViewById(R.id.go_regis_login).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), UserRegistration.class));
            finish();
        });

        findViewById(R.id.admin_portal_login).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), AdminLogin.class));
            finish();
        });

        findViewById(R.id.partner_login).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RestaurantLogin.class));
            finish();
        });

        loginButton.setOnClickListener(view -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter the Required Fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Fetch user address and navigate to MainActivity
                        fetchUserDetails(email);
                    } else {
                        Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDetails(String email) {
        db.collection("users")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String address = document.getString("Address");
                            String username = email.split("@")[0];
                            navigateToMainActivity(username, address);
                        } else {
                            Toast.makeText(getApplicationContext(), "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to fetch user details!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserDetails(String username, String address) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("address", address);
        editor.apply();
    }


    private void navigateToMainActivity(String username, String address) {
        Intent intent = new Intent(UserLogin.this, MainActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("address", address);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for some features", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
