
package com.example.cravify;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItemList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public CartAdapter(List<CartItem> cartItemList, Context context) {
        this.cartItemList = cartItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);

        Resources resources = context.getResources();
        int color;
        if ("Veg".equalsIgnoreCase(cartItem.getType())) {
            color = resources.getColor(R.color.green, null);
        } else if ("Non-Veg".equalsIgnoreCase(cartItem.getType())) {
            color = resources.getColor(R.color.red, null);
        } else {
            color = resources.getColor(R.color.black, null);
        }

        holder.foodNameTextView.setText(cartItem.getName());
        holder.foodTypeTextView.setText(cartItem.getType());
        holder.quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
        holder.priceTextView.setText("₹ " + cartItem.getPrice() * cartItem.getQuantity());

        holder.foodTypeTextView.setTextColor(color);

        holder.increaseQuantityTextView.setOnClickListener(v -> updateQuantity(holder.getAdapterPosition(), cartItem.getQuantity() + 1));
        holder.decreaseQuantityTextView.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                updateQuantity(holder.getAdapterPosition(), cartItem.getQuantity() - 1);
            } else {
                removeCartItem(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    private void updateQuantity(int position, int newQuantity) {
        CartItem cartItem = cartItemList.get(position);
        cartItem.setQuantity(newQuantity);
        notifyItemChanged(position);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("cart").document(cartItem.getName())
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    if (context instanceof Cart) {
                        ((Cart) context).updateTotalPrice();
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void removeCartItem(int position) {
        CartItem cartItem = cartItemList.get(position);
        cartItemList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cartItemList.size());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("cart").document(cartItem.getName())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (context instanceof Cart) {
                        ((Cart) context).updateTotalPrice();
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameTextView;
        TextView foodTypeTextView;
        TextView quantityTextView;
        TextView priceTextView;
        TextView increaseQuantityTextView;
        TextView decreaseQuantityTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            foodNameTextView = itemView.findViewById(R.id.food_name);
            foodTypeTextView = itemView.findViewById(R.id.food_type);
            quantityTextView = itemView.findViewById(R.id.quantity);
            priceTextView = itemView.findViewById(R.id.food_price);
            increaseQuantityTextView = itemView.findViewById(R.id.increase_quantity);
            decreaseQuantityTextView = itemView.findViewById(R.id.decrease_quantity);
        }
    }
}
