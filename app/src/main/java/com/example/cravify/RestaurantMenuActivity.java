package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RestaurantMenuActivity extends AppCompatActivity implements MenuAdapter.CartUpdateListener {

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private List<MenuItem> menuItemList;
    private RelativeLayout cartButton;
    private boolean isFirstDishAdded = false;


    @Override
    protected void onResume() {
        super.onResume();
        // Get restaurant name from intent
        String restaurantName = getIntent().getStringExtra("restaurantName");

        // Refresh menu items
        loadMenuItems(restaurantName);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_menu);
        EdgeToEdge.enable(this);

        String restaurantName = getIntent().getStringExtra("restaurantName");
        String restaurantCuisine = getIntent().getStringExtra("restaurantCuisine");
        String restaurantAddress = getIntent().getStringExtra("restaurantAddress");

        TextView nameTextView = findViewById(R.id.restaurant_name);
        TextView cuisineTextView = findViewById(R.id.restaurant_cuisine);
        TextView addressTextView = findViewById(R.id.restaurant_address);
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        cartButton = findViewById(R.id.custom_cart_button);

        nameTextView.setText(restaurantName);
        cuisineTextView.setText(restaurantCuisine);
        addressTextView.setText(restaurantAddress);

        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuItemList = new ArrayList<>();
        menuAdapter = new MenuAdapter(menuItemList, getResources(), this);
        menuRecyclerView.setAdapter(menuAdapter);

        loadMenuItems(restaurantName);

        // Set up OnClickListener for the cart button
        cartButton.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantMenuActivity.this, Cart.class);
            intent.putExtra("restaurantName", restaurantName);
            startActivity(intent);
        });
    }

    private void loadMenuItems(String restaurantName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .document(restaurantName)
                .collection("menuItems").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        menuItemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String type = document.getString("type");
                            double price = document.getDouble("price") != null ? document.getDouble("price") : 0;
                            String imageUrl = document.getString("imageUrl");

                            if (name != null && description != null && type != null && imageUrl != null) {
                                MenuItem menuItem = new MenuItem(name, description, type, price, imageUrl);
                                menuItemList.add(menuItem);
                            }
                        }
                        menuAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(RestaurantMenuActivity.this, "Failed to load menu items!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCartUpdated(int itemCount) {
        if (itemCount > 0) {
            cartButton.setVisibility(View.VISIBLE);
            if (itemCount == 1 && !isFirstDishAdded) {
                Animation fadeInSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
                cartButton.startAnimation(fadeInSlideUp);
                isFirstDishAdded = true;
            }
        } else {
            Animation fadeOutSlideDown = AnimationUtils.loadAnimation(this, R.anim.fade_out_slide_down);
            fadeOutSlideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cartButton.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            cartButton.startAnimation(fadeOutSlideDown);
        }
    }
}