package com.example.cravify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private List<OrderHistoryItem> orderHistoryList;

    public OrderHistoryAdapter(List<OrderHistoryItem> orderHistoryList) {
        this.orderHistoryList = orderHistoryList;
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_history_item, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        OrderHistoryItem orderItem = orderHistoryList.get(position);

        holder.nameTextView.setText(orderItem.getName());
        holder.typeTextView.setText(orderItem.getType());
        holder.priceTextView.setText("₹" + orderItem.getPrice());
        holder.quantityTextView.setText("Quantity: " + orderItem.getQuantity());
    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    public static class OrderHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView typeTextView;
        TextView priceTextView;
        TextView quantityTextView;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.order_item_name);
            typeTextView = itemView.findViewById(R.id.order_item_type);
            priceTextView = itemView.findViewById(R.id.order_item_price);
            quantityTextView = itemView.findViewById(R.id.order_item_quantity);
        }
    }
}
