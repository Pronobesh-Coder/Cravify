package com.example.cravify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView orderHistoryRecyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<OrderHistoryItem> orderHistoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private RelativeLayout noOrdersLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_history_fragment, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        orderHistoryRecyclerView = view.findViewById(R.id.order_history_recyclerview);
        noOrdersLayout = view.findViewById(R.id.no_orders_layout);

        // Set up RecyclerView
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderHistoryAdapter = new OrderHistoryAdapter(orderHistoryList);
        orderHistoryRecyclerView.setAdapter(orderHistoryAdapter);

        // Load Order History
        loadOrderHistory();

        return view;
    }

    private void loadOrderHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).collection("order_history").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        orderHistoryList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            String type = document.getString("type");
                            double price = document.getDouble("price") != null ? document.getDouble("price") : 0;
                            int quantity = document.getLong("quantity") != null ? document.getLong("quantity").intValue() : 0;

                            OrderHistoryItem orderHistoryItem = new OrderHistoryItem(name, type, price, quantity);
                            orderHistoryList.add(orderHistoryItem);
                        }
                        orderHistoryAdapter.notifyDataSetChanged();

                        // Show/hide the empty state layout
                        if (orderHistoryList.isEmpty()) {
                            noOrdersLayout.setVisibility(View.VISIBLE);
                            orderHistoryRecyclerView.setVisibility(View.GONE);
                        } else {
                            noOrdersLayout.setVisibility(View.GONE);
                            orderHistoryRecyclerView.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HistoryFragment", "Error loading order history", e));
        }
    }
}
