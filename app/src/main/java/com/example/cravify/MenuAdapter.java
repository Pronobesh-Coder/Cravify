package com.example.cravify;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        holder.addToCartButton.setOnClickListener(v -> {
            addToCart(menuItem, holder);
        });

        holder.incrementButton.setOnClickListener(v -> {
            updateQuantity(menuItem, holder, 1);
        });

        holder.decrementButton.setOnClickListener(v -> {
            updateQuantity(menuItem, holder, -1);
        });
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
                                        holder.addToCartButton.setVisibility(View.GONE);
                                        holder.capsuleLayout.setVisibility(View.VISIBLE);
                                        holder.quantityTextView.setText("1");
                                        totalItemsInCart++;
                                        cartUpdateListener.onCartUpdated(totalItemsInCart);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle error
                                    });
                        } else {
                            holder.addToCartButton.setVisibility(View.GONE);
                            holder.capsuleLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void updateQuantity(MenuItem menuItem, MenuViewHolder holder, int change) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cart")
                .document(menuItem.getName()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            CartItem cartItem = task.getResult().toObject(CartItem.class);
                            int newQuantity = cartItem.getQuantity() + change;

                            if (newQuantity > 0) {
                                db.collection("users").document(userId).collection("cart")
                                        .document(menuItem.getName()).update("quantity", newQuantity)
                                        .addOnSuccessListener(aVoid -> {
                                            holder.quantityTextView.setText(String.valueOf(newQuantity));
                                            totalItemsInCart += change;
                                            cartUpdateListener.onCartUpdated(totalItemsInCart);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle error
                                        });
                            } else {
                                db.collection("users").document(userId).collection("cart")
                                        .document(menuItem.getName()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            holder.addToCartButton.setVisibility(View.VISIBLE);
                                            holder.capsuleLayout.setVisibility(View.GONE);
                                            holder.quantityTextView.setText("0");
                                            totalItemsInCart -= cartItem.getQuantity(); // Adjust totalItemsInCart based on removed quantity
                                            cartUpdateListener.onCartUpdated(totalItemsInCart);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle error
                                        });
                            }
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
        public Button addToCartButton;
        public LinearLayout capsuleLayout;
        public TextView incrementButton, decrementButton, quantityTextView;

        public MenuViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.menu_item_name);
            typeTextView = view.findViewById(R.id.menu_item_type);
            descriptionTextView = view.findViewById(R.id.menu_item_description);
            priceTextView = view.findViewById(R.id.menu_item_price);
            imageView = view.findViewById(R.id.menu_item_image);
            addToCartButton = view.findViewById(R.id.addToCartButton);
            capsuleLayout = view.findViewById(R.id.capsule_background);
            incrementButton = view.findViewById(R.id.increase_quantity);
            decrementButton = view.findViewById(R.id.decrease_quantity);
            quantityTextView = view.findViewById(R.id.quantity);
        }
    }

    public interface CartUpdateListener {
        void onCartUpdated(int itemCount);
    }
}
