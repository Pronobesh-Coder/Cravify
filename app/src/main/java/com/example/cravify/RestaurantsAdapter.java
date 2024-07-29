package com.example.cravify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.RestaurantViewHolder> {

    private List<Map<String, String>> restaurants;

    public RestaurantsAdapter(List<Map<String, String>> restaurants) {
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Map<String, String> restaurant = restaurants.get(position);
        holder.nameTextView.setText(restaurant.get("name"));
        holder.emailTextView.setText(restaurant.get("email"));
        holder.phoneTextView.setText(restaurant.get("phone"));
        holder.addressTextView.setText(restaurant.get("address"));
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    public void setRestaurants(List<Map<String, String>> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView emailTextView;
        public TextView phoneTextView;
        public TextView addressTextView;

        public RestaurantViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.name_text_view);
            emailTextView = view.findViewById(R.id.email_text_view);
            phoneTextView = view.findViewById(R.id.phone_text_view);
            addressTextView = view.findViewById(R.id.address_text_view);
        }
    }
}
