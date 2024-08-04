package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> favouriteRestaurants = new ArrayList<>();
    private View noFavouritesLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_saved_fragment, container, false);

        recyclerView = view.findViewById(R.id.saved_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RestaurantAdapter(favouriteRestaurants, getContext(), restaurant -> {
            String fullAddress = restaurant.getAddress();
            String[] addressParts = fullAddress.split(",");

            String addressPart = "";
            if (addressParts.length > 3) {
                addressPart = addressParts[2].trim() + "," + addressParts[3].trim();
            } else if (addressParts.length > 2) {
                addressPart = addressParts[2].trim();
            }

            Intent intent = new Intent(getActivity(), RestaurantMenuActivity.class);
            intent.putExtra("restaurantName", restaurant.getName());
            intent.putExtra("restaurantCuisine", restaurant.getCuisine());
            intent.putExtra("restaurantAddress", addressPart);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        noFavouritesLayout = view.findViewById(R.id.no_favourites_layout);

        loadFavouriteRestaurants();

        return view;
    }

    private void loadFavouriteRestaurants() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("favourites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("SavedFragment", "Fetched favourites: " + queryDocumentSnapshots.size());
                    favouriteRestaurants.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        toggleNoFavouritesMessage(); // No favourites, update UI
                        return;
                    }

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String restaurantName = documentSnapshot.getId();
                        Log.d("SavedFragment", "Fetching restaurant with name: " + restaurantName);

                        db.collection("restaurants")
                                .whereEqualTo("Name", restaurantName) // Query by restaurant name
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        for (DocumentSnapshot restaurantSnapshot : querySnapshot) {
                                            Restaurant restaurant = restaurantSnapshot.toObject(Restaurant.class);
                                            if (restaurant != null) {
                                                favouriteRestaurants.add(restaurant);
                                                Log.d("SavedFragment", "Restaurant added: " + restaurant.getName());
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.d("SavedFragment", "Restaurant not found: " + restaurantName);
                                    }
                                    toggleNoFavouritesMessage(); // Update UI after adding restaurants
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SavedFragment", "Failed to load restaurant details for name: " + restaurantName, e);
                                    Toast.makeText(getContext(), "Failed to load restaurant details", Toast.LENGTH_SHORT).show();
                                    toggleNoFavouritesMessage(); // Update UI on failure
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SavedFragment", "Failed to load favourites", e);
                    Toast.makeText(getContext(), "Failed to load favourites", Toast.LENGTH_SHORT).show();
                    toggleNoFavouritesMessage(); // Update UI on failure
                });
    }

    private void toggleNoFavouritesMessage() {
        if (favouriteRestaurants.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noFavouritesLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noFavouritesLayout.setVisibility(View.GONE);
        }
    }
}
