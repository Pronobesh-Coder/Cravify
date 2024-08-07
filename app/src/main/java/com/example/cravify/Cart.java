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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Cart extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_APP_BACKGROUND = "AppInBackground";

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView restaurantNameView;
    private TextView userAddressView;
    private TextView itemTotalView;
    private TextView totalPriceView;
    private TextView toPayPriceView; // Added TextView for to_pay_price
    private String restaurantName;
    private TextView deliveryPrice, platformFee, gstFare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        cartRecyclerView = findViewById(R.id.cart_items_recycler_view);
        restaurantNameView = findViewById(R.id.restaurant_name);
        userAddressView = findViewById(R.id.user_address);
        itemTotalView = findViewById(R.id.item_total_price);
        totalPriceView = findViewById(R.id.total_price);
        toPayPriceView = findViewById(R.id.to_pay_price);
        deliveryPrice = findViewById(R.id.delivery_fee_price);
        platformFee = findViewById(R.id.platform_fee_price);
        gstFare = findViewById(R.id.gst_charges_price);


        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItemList, this);
        cartRecyclerView.setAdapter(cartAdapter);

        // Get restaurant name from intent
        Intent intent = getIntent();
        restaurantName = intent.getStringExtra("restaurantName");

        if (restaurantName != null) {
            restaurantNameView.setText(restaurantName);
            fetchUserAddress();
        } else {
            restaurantNameView.setText("Restaurant not available");
        }
        loadCartItems();
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
        // Check if app is truly closing
        if (isAppClosing()) {
            deleteCartItems();
        }
    }

    // Check if the app is actually closing
    private boolean isAppClosing() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
            return tasks.size() == 0;
        }
        return false;
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error deleting cart items", e));
    }

    public void updateTotalPrice() {
        double itemTotal = 0;
        for (CartItem item : cartItemList) {
            itemTotal += item.getPrice() * item.getQuantity();
        }

        if (itemTotal == 0) {
            itemTotalView.setText("₹0");
            totalPriceView.setText("₹0");
            toPayPriceView.setText("₹0");
            deliveryPrice.setText("₹0");
            platformFee.setText("₹0");
            gstFare.setText("₹0");

        } else {
            double deliveryFee = 40;
            double platformFee = 5;
            double gstAndCharges = 16;
            double totalAmount = itemTotal + deliveryFee + platformFee + gstAndCharges;

            itemTotalView.setText("₹" + itemTotal); // Update item total
            totalPriceView.setText("₹" + totalAmount); // Update total price
            toPayPriceView.setText("₹" + totalAmount); // Update to pay price
        }
    }

    // Load cart items and initialize cartAdapter
    private void loadCartItems() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
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
                    updateTotalPrice(); // Initialize total price
                });
    }
}
