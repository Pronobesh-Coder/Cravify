package com.example.cravify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Cart extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_APP_BACKGROUND = "AppInBackground";

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private TextView restaurantNameView;
    private TextView userAddressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // Initialize Firestore and Views
        db = FirebaseFirestore.getInstance();
        cartRecyclerView = findViewById(R.id.cart_items_recycler_view);
        restaurantNameView = findViewById(R.id.restaurant_name);
        userAddressView = findViewById(R.id.user_address);

        // Initialize cart item list and adapter
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList, this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        // Get restaurant name from intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("restaurantName");

        if (restaurantName != null) {
            restaurantNameView.setText(restaurantName);
            loadCartItems(restaurantName);
            fetchUserAddress();
        } else {
            restaurantNameView.setText("Restaurant not available");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mark app as in background
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_APP_BACKGROUND, true);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark app as not in background
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_APP_BACKGROUND, false);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteCartItems();
    }


    // Load cart items for the given restaurant
    private void loadCartItems(String restaurantName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cart")
                .whereEqualTo("restaurantName", restaurantName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartItemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String foodName = document.getString("name");
                            String foodType = document.getString("type");
                            int quantity = document.getLong("quantity") != null ? document.getLong("quantity").intValue() : 0;
                            double price = document.getDouble("price") != null ? document.getDouble("price") : 0;

                            if (foodName != null && foodType != null) {
                                CartItem cartItem = new CartItem(foodName, foodType, quantity, price);
                                cartItemList.add(cartItem);
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("CartActivity", "Error loading cart items", task.getException());
                        userAddressView.setText("Failed to load cart items");
                    }
                });
    }

    // Fetch user address from Firestore
    private void fetchUserAddress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String address = task.getResult().getString("Address");
                        if (address != null) {
                            userAddressView.setText(address);
                        } else {
                            userAddressView.setText("Address not available");
                        }
                    } else {
                        Log.e("CartActivity", "Error fetching user address", task.getException());
                        userAddressView.setText("Failed to load address");
                    }
                });
    }

    // Delete cart items from Firestore
    private void deleteCartItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cart").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("CartActivity", "DocumentSnapshot successfully deleted!"))
                                    .addOnFailureListener(e -> Log.e("CartActivity", "Error deleting document", e));
                        }
                    } else {
                        Log.e("CartActivity", "Error getting documents.", task.getException());
                    }
                });
    }
}
