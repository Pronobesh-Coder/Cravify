package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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

public class RestaurantMenuActivity extends AppCompatActivity {

    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private List<MenuItem> menuItemList;

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

        nameTextView.setText(restaurantName);
        cuisineTextView.setText(restaurantCuisine);
        addressTextView.setText(restaurantAddress);

        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuItemList = new ArrayList<>();
        menuAdapter = new MenuAdapter(menuItemList,getResources());
        menuRecyclerView.setAdapter(menuAdapter);

        loadMenuItems(restaurantName);
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
}