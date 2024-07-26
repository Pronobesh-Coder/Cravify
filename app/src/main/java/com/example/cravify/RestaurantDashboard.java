package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class RestaurantDashboard extends AppCompatActivity {

    Button logout;
    TextView restaurantNameTextView;
    TextView resCuisine;
    TextView resAddress;
    TextView resEmail;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_dashboard);

        logout = findViewById(R.id.logout_button);
        restaurantNameTextView = findViewById(R.id.restaurant_name_text_view);
        resCuisine = findViewById(R.id.cuisine);
        resAddress = findViewById(R.id.current_res_location);
        resEmail = findViewById(R.id.email);
        mAuth = FirebaseAuth.getInstance();

        // Retrieve data from Intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("res_name");
        String cuisine = intent.getStringExtra("cuisine");
        String address = intent.getStringExtra("address");
        String email = intent.getStringExtra("email");

        // Display the retrieved data
        if (restaurantName != null) {
            restaurantNameTextView.setText(capitalizeFirstLetter(restaurantName));
        } else {
            restaurantNameTextView.setText("Restaurant");
        }

        if (cuisine != null) {
            resCuisine.setText(cuisine);
        } else {
            resCuisine.setText("-");
        }

        if (address != null) {
            resAddress.setText(address);
        } else {
            resAddress.setText("-");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutAndRedirect();
            }
        });
    }

    private void signOutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        Intent loginIntent = new Intent(RestaurantDashboard.this, RestaurantLogin.class);
        startActivity(loginIntent);
        finish();
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Sign out the user when the activity is stopped
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Additional sign out to ensure user is signed out in case onStop is not called
        FirebaseAuth.getInstance().signOut();
    }
}
