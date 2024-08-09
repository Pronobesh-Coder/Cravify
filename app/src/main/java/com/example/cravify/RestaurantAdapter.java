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
    private Set<String> likedRestaurants = new HashSet<>();

    public RestaurantAdapter(List<Restaurant> restaurantList, Context context, OnItemClickListener onItemClickListener) {
        this.restaurantList = restaurantList;
        this.context = context.getApplicationContext();
        this.onItemClickListener = onItemClickListener;
        loadLikedRestaurants();
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

        Glide.with(context)
                .load(restaurant.getImageUrl())
                .into(holder.imageView);

        boolean isLiked = likedRestaurants.contains(restaurant.getName());
        holder.heartIcon.setImageResource(isLiked ? R.drawable.heart_filled : R.drawable.heart_outline);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(restaurant);
            }
        });

        holder.heartIcon.setOnClickListener(v -> {
            if (likedRestaurants.contains(restaurant.getName())) {
                removeFavouriteRestaurant(restaurant.getName());
                holder.heartIcon.setImageResource(R.drawable.heart_outline);
            } else {
                addToFavourites(restaurant.getName());
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
        ImageView heartIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            cuisineTextView = itemView.findViewById(R.id.restaurant_cuisine);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            imageView = itemView.findViewById(R.id.restaurant_image);
            heartIcon = itemView.findViewById(R.id.heart_icon);
        }
    }

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
                .document(restaurantName)
                .set(new HashMap<>())
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
                .collection("favourites").document(restaurantName)
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
                        likedRestaurants.add(documentSnapshot.getId());
                    }
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load favourites", Toast.LENGTH_SHORT).show();
                });
    }
}
