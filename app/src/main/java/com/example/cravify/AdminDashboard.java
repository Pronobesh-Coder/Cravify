package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity {

    private Button logout;
    private RecyclerView usersRecyclerView;
    private RecyclerView restaurantsRecyclerView;
    private UsersAdapter usersAdapter;
    private RestaurantsAdapter restaurantsAdapter;
    private FirebaseFirestore db;
    ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        progressBar = findViewById(R.id.progBar);
        progressBar.setVisibility(View.VISIBLE);
        logout = findViewById(R.id.logout_button);
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        restaurantsRecyclerView = findViewById(R.id.restaurants_recycler_view);

        db = FirebaseFirestore.getInstance();

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersAdapter = new UsersAdapter(new ArrayList<>());
        restaurantsAdapter = new RestaurantsAdapter(new ArrayList<>());

        usersRecyclerView.setAdapter(usersAdapter);
        restaurantsRecyclerView.setAdapter(restaurantsAdapter);

        loadUsers();
        loadRestaurants();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserLogin.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUsers() {
        CollectionReference usersRef = db.collection("users");
        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Map<String, String>> users = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        Map<String, String> user = new HashMap<>();
                        user.put("name", document.getString("Name"));

                        // Handle the age field conversion
                        Object ageObj = document.get("Age");
                        if (ageObj != null) {
                            user.put("age", ageObj.toString());
                        } else {
                            user.put("age", "N/A");
                        }

                        user.put("email", document.getString("Email"));
                        user.put("phone", document.getString("Phone"));
                        user.put("address", document.getString("Address"));
                        users.add(user);
                    }
                    usersAdapter.setUsers(users);
                } else {
                    Toast.makeText(getApplicationContext(),"Could not load users!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void loadRestaurants() {
        CollectionReference restaurantsRef = db.collection("restaurants");
        restaurantsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Map<String, String>> restaurants = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        Map<String, String> restaurant = new HashMap<>();
                        restaurant.put("name", document.getString("Name"));
                        restaurant.put("email", document.getString("Email"));
                        restaurant.put("phone", document.getString("Phone"));
                        restaurant.put("address", document.getString("Address"));
                        restaurants.add(restaurant);
                    }
                    restaurantsAdapter.setRestaurants(restaurants);
                } else {
                    Toast.makeText(getApplicationContext(),"Could not load restaurants!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }
}
