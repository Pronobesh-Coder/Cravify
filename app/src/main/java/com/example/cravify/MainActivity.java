package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private String username;
    private String address; // Add a field for address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Retrieve the username and address from the intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("username")) {
                username = intent.getStringExtra("username");
            }
            if (intent.hasExtra("address")) {
                address = intent.getStringExtra("address");
            }
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                Bundle args = new Bundle();
                args.putString("username", username);
                args.putString("address", address); // Pass the address to HomeFragment
                selectedFragment.setArguments(args);
            } else if (item.getItemId() == R.id.nav_saved) {
                selectedFragment = new SavedFragment();
            } else if (item.getItemId() == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        if (savedInstanceState == null) {
            // Load HomeFragment with username and address arguments
            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString("username", username);
            args.putString("address", address); // Pass the address to HomeFragment
            homeFragment.setArguments(args);
            loadFragment(homeFragment);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void navigateToSavedFragment() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_saved); // Assuming 'saved' is the menu ID for SavedFragment
    }

    public void navigateToHistoryFragment() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_history); // Assuming 'history' is the menu ID for HistoryFragment
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        // Avoid using .addToBackStack() unless you need to keep the previous fragment on the stack
        fragmentTransaction.commit();
    }
}
