package com.example.cravify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart extends AppCompatActivity implements PaymentResultListener {

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
    private TextView toPayPriceView;
    private String restaurantName;
    private TextView deliveryPrice, platformFee, gstFare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        initializeViews();

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItemList, this);
        cartRecyclerView.setAdapter(cartAdapter);

        Intent intent = getIntent();
        restaurantName = intent.getStringExtra("restaurantName");

        if (restaurantName != null) {
            restaurantNameView.setText(restaurantName);
            fetchUserAddress();
        } else {
            restaurantNameView.setText("Restaurant not available");
        }

        loadCartItems();

        Checkout.preload(getApplicationContext());

        findViewById(R.id.proceed_to_pay_button).setOnClickListener(v -> startPayment());
    }

    private void initializeViews() {
        cartRecyclerView = findViewById(R.id.cart_items_recycler_view);
        restaurantNameView = findViewById(R.id.restaurant_name);
        userAddressView = findViewById(R.id.user_address);
        itemTotalView = findViewById(R.id.item_total_price);
        totalPriceView = findViewById(R.id.total_price);
        toPayPriceView = findViewById(R.id.to_pay_price);
        deliveryPrice = findViewById(R.id.delivery_fee_price);
        platformFee = findViewById(R.id.platform_fee_price);
        gstFare = findViewById(R.id.gst_charges_price);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setAppInBackground(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAppInBackground(false);
    }

    private void setAppInBackground(boolean inBackground) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_APP_BACKGROUND, inBackground);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isAppClosing()) {
            deleteCartItems();
        }
    }

    private boolean isAppClosing() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
            return tasks.isEmpty();
        }
        return false;
    }

    private void fetchUserAddress() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String address = task.getResult().getString("Address");
                            userAddressView.setText(address != null ? address : "Address not available");
                        } else {
                            Log.e("CartActivity", "Error fetching user address", task.getException());
                            userAddressView.setText("Failed to load address");
                        }
                    });
        } else {
            userAddressView.setText("User not authenticated");
        }
    }

    private void deleteCartItems() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("cart").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("CartActivity", "Error deleting cart items", e));
        }
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
            double platformFeeAmount = 5;
            double gstAndCharges = 16;
            double totalAmount = itemTotal + deliveryFee + platformFeeAmount + gstAndCharges;

            itemTotalView.setText("₹" + itemTotal);
            totalPriceView.setText("₹" + totalAmount);
            toPayPriceView.setText("₹" + totalAmount);
            deliveryPrice.setText("₹" + deliveryFee);
            platformFee.setText("₹" + platformFeeAmount);
            gstFare.setText("₹" + gstAndCharges);
        }
    }

    private void loadCartItems() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
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
                        updateTotalPrice();
                    })
                    .addOnFailureListener(e -> Log.e("CartActivity", "Error loading cart items", e));
        }
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_NxWtUm8DUtS0U9");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            try {
                JSONObject options = new JSONObject();

                options.put("name", "Cravify");
                options.put("description", "Payment for your order");
                options.put("currency", "INR");

                double totalAmount = Double.parseDouble(toPayPriceView.getText().toString().replace("₹", "")) * 100;
                options.put("amount", totalAmount);

                options.put("prefill.email", currentUser.getEmail());
                options.put("prefill.contact", "");  // Add user contact if available

                checkout.open(this, options);

            } catch (Exception e) {
                Log.e("Razorpay", "Error in starting payment", e);
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

        storeOrderHistory();

        clearCartAndNavigateHome();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_SHORT).show();
    }


    private void clearCartAndNavigateHome() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("cart").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        navigateToHome();
                    })
                    .addOnFailureListener(e -> Log.e("CartActivity", "Error deleting cart items", e));
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("payment_successful", true); // Pass the flag to MainActivity
        startActivity(intent);
    }

    private void storeOrderHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            for (CartItem item : cartItemList) {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("name", item.getName());
                orderData.put("type", item.getType());
                orderData.put("price", item.getPrice());
                orderData.put("quantity", item.getQuantity());

                db.collection("users").document(userId)
                        .collection("order_history")
                        .add(orderData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("CartActivity", "Order added to history");
                        })
                        .addOnFailureListener(e -> Log.e("CartActivity", "Error adding order to history", e));
            }
        }
    }
}