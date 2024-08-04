package com.example.cravify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private Set<String> likedRestaurants = new HashSet<>(); // Track liked restaurants

    public RestaurantAdapter(List<Restaurant> restaurantList, Context context, OnItemClickListener onItemClickListener) {
        this.restaurantList = restaurantList;
        this.context = context.getApplicationContext(); // Use application context
        this.onItemClickListener = onItemClickListener;
        loadLikedRestaurants(); // Load the liked restaurants when the adapter is created
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.res_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.nameTextView.setText(restaurant.getName());
        holder.cuisineTextView.setText(restaurant.getCuisine());
        holder.addressTextView.setText(restaurant.getAddress());

        // Load image using Glide
        Glide.with(context)
                .load(restaurant.getImageUrl())
                .into(holder.imageView);

        // Set heart icon state
        boolean isLiked = likedRestaurants.contains(restaurant.getName()); // Use restaurant name or ID
        holder.heartIcon.setImageResource(isLiked ? R.drawable.heart_filled : R.drawable.heart_outline);

        // Set click listener for the item view
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(restaurant);
            }
        });

        // Set click listener for the heart icon
        holder.heartIcon.setOnClickListener(v -> {
            if (likedRestaurants.contains(restaurant.getName())) { // Use restaurant name or ID
                removeFavouriteRestaurant(restaurant.getName()); // Use restaurant ID or name
                holder.heartIcon.setImageResource(R.drawable.heart_outline);
            } else {
                addToFavourites(restaurant.getName()); // Use restaurant ID or name
                holder.heartIcon.setImageResource(R.drawable.heart_filled);
            }
        });
    }

    public void updateList(List<Restaurant> newList) {
        restaurantList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return restaurantList != null ? restaurantList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView cuisineTextView;
        TextView addressTextView;
        ImageView imageView;
        ImageView heartIcon; // Added heart icon ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            cuisineTextView = itemView.findViewById(R.id.restaurant_cuisine);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            imageView = itemView.findViewById(R.id.restaurant_image);
            heartIcon = itemView.findViewById(R.id.heart_icon); // Initialize heart icon
        }
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    public void addToFavourites(String restaurantName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("favourites")
                .document(restaurantName) // Store the restaurant name as the document ID
                .set(new HashMap<>()) // Set an empty document or some metadata if needed
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddToFavourites", "Failed to add to favourites", e);
                    Toast.makeText(context, "Failed to add to favourites", Toast.LENGTH_SHORT).show();
                });
    }




    private void removeFavouriteRestaurant(String restaurantName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("favourites").document(restaurantName) // Use restaurant name as document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to remove from Favourites", Toast.LENGTH_SHORT).show();
                });
    }


    private void loadLikedRestaurants() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("favourites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        likedRestaurants.add(documentSnapshot.getId()); // Add each document ID to the set
                    }
                    notifyDataSetChanged(); // Refresh the UI with the liked restaurants
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load favourites", Toast.LENGTH_SHORT).show();
                });
    }
}
