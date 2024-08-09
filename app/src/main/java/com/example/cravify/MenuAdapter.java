package com.example.cravify;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuItemList;
    private Resources resources;
    private CartUpdateListener cartUpdateListener;
    private int totalItemsInCart = 0;
    private FirebaseFirestore db;

    public MenuAdapter(List<MenuItem> menuItemList, Resources resources, CartUpdateListener cartUpdateListener) {
        this.menuItemList = menuItemList;
        this.resources = resources;
        this.cartUpdateListener = cartUpdateListener;
        this.db = FirebaseFirestore.getInstance();
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
            color = resources.getColor(R.color.green, null);
        } else if ("Non-Veg".equalsIgnoreCase(menuItem.getType())) {
            color = resources.getColor(R.color.red, null);
        } else {
            color = resources.getColor(R.color.black, null);
        }

        holder.nameTextView.setText(menuItem.getName());
        holder.typeTextView.setText(menuItem.getType());
        holder.typeTextView.setTextColor(color);
        holder.descriptionTextView.setText(menuItem.getDescription());
        holder.priceTextView.setText(String.format("₹ %.1f", menuItem.getPrice()));
        Glide.with(holder.itemView.getContext()).load(menuItem.getImageUrl()).into(holder.imageView);

        holder.addToCartButton.setOnClickListener(v -> addToCart(menuItem, holder));
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    private void addToCart(MenuItem menuItem, MenuViewHolder holder) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cart")
                .document(menuItem.getName()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            db.collection("users").document(userId).collection("cart")
                                    .document(menuItem.getName()).set(new CartItem(menuItem.getName(), menuItem.getType(), 1, menuItem.getPrice()))
                                    .addOnSuccessListener(aVoid -> {
                                        holder.addToCartButton.setText("Added");
                                        holder.addToCartButton.setEnabled(false);
                                        totalItemsInCart++;
                                        cartUpdateListener.onCartUpdated(totalItemsInCart);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Dish Already Added!", Toast.LENGTH_SHORT).show();
                            holder.addToCartButton.setEnabled(false);
                        }
                    }
                });
    }



    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView typeTextView;
        public TextView descriptionTextView;
        public TextView priceTextView;
        public ImageView imageView;
        public Button addToCartButton, addedToCartButton;

        public MenuViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.menu_item_name);
            typeTextView = view.findViewById(R.id.menu_item_type);
            descriptionTextView = view.findViewById(R.id.menu_item_description);
            priceTextView = view.findViewById(R.id.menu_item_price);
            imageView = view.findViewById(R.id.menu_item_image);
            addToCartButton = view.findViewById(R.id.addToCartButton);
            addedToCartButton = view.findViewById(R.id.addedToCartButton);
        }
    }

    public interface CartUpdateListener {
        void onCartUpdated(int itemCount);
    }
}
