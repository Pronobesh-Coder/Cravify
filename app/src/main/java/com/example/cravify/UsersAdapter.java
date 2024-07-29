package com.example.cravify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<Map<String, String>> users;

    public UsersAdapter(List<Map<String, String>> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, String> user = users.get(position);
        holder.nameTextView.setText(user.get("name"));
        holder.ageTextView.setText(user.get("age"));
        holder.emailTextView.setText(user.get("email"));
        holder.phoneTextView.setText(user.get("phone"));
        holder.addressTextView.setText(user.get("address"));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<Map<String, String>> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView ageTextView;
        public TextView emailTextView;
        public TextView phoneTextView;
        public TextView addressTextView;

        public UserViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.name_text_view);
            ageTextView = view.findViewById(R.id.age_text_view);
            emailTextView = view.findViewById(R.id.email_text_view);
            phoneTextView = view.findViewById(R.id.phone_text_view);
            addressTextView = view.findViewById(R.id.address_text_view);
        }
    }
}
