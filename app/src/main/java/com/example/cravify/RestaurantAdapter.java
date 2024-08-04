package com.example.cravify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public RestaurantAdapter(List<Restaurant> restaurantList, Context context, OnItemClickListener onItemClickListener) {
        this.restaurantList = restaurantList;
        this.context = context.getApplicationContext(); // Use application context
        this.onItemClickListener = onItemClickListener;
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

        // Load image using Glide with a placeholder and error handling
        Glide.with(context)
                .load(restaurant.getImageUrl())
                //.apply(new RequestOptions().placeholder(R.drawable.placeholder_image).error(R.drawable.error_image))
                .into(holder.imageView);

        // Set click listener for the item view
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(restaurant);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.restaurant_name);
            cuisineTextView = itemView.findViewById(R.id.restaurant_cuisine);
            addressTextView = itemView.findViewById(R.id.restaurant_address);
            imageView = itemView.findViewById(R.id.restaurant_image);
        }
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }
}
