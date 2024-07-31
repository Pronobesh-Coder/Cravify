package com.example.cravify;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuItemList;
    private Resources resources;
    public MenuAdapter(List<MenuItem> menuItemList,Resources resources) {
        this.menuItemList = menuItemList;
        this.resources = resources;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menuItem = menuItemList.get(position);

        int color;
        if ("Veg".equalsIgnoreCase(menuItem.getType())) {
            color = resources.getColor(R.color.green, null); // Dark green color
        } else if ("Non-Veg".equalsIgnoreCase(menuItem.getType())) {
            color = resources.getColor(R.color.red, null); // Red color
        } else {
            color = resources.getColor(R.color.black, null); // Default color
        }
        holder.nameTextView.setText(menuItem.getName());
        holder.typeTextView.setText(menuItem.getType());
        holder.typeTextView.setTextColor(color);
        holder.descriptionTextView.setText(menuItem.getDescription());
        holder.priceTextView.setText(String.format("₹ %.1f", menuItem.getPrice()));
        Glide.with(holder.itemView.getContext()).load(menuItem.getImageUrl()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView typeTextView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public ImageView imageView;

        public MenuViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.menu_item_name);
            typeTextView = view.findViewById(R.id.menu_item_type);
            descriptionTextView = view.findViewById(R.id.menu_item_description);
            priceTextView = view.findViewById(R.id.menu_item_price);
            imageView = view.findViewById(R.id.menu_item_image);
        }
    }
}
