package com.example.cravify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        // Retrieve the username and address from the arguments
        String username = getArguments() != null ? getArguments().getString("username") : "User";
        String address = getArguments() != null ? getArguments().getString("address") : "Not available";

        // Process the username to get the first name and capitalize the first letter
        String firstName = getFirstName(username); // Process the username
        firstName = capitalizeFirstLetter(firstName); // Capitalize the first letter

        // Find the TextView and set the greeting message
        TextView greetingTextView = view.findViewById(R.id.username_crave);
        if (greetingTextView != null) {
            greetingTextView.setText("Hello! " + firstName + ", What are you craving today?");
        }

        // Find the TextView for address and set it
        TextView addressTextView = view.findViewById(R.id.current_location_txt);
        if (addressTextView != null) {
            addressTextView.setText(address);
        }

        return view;
    }

    private String getFirstName(String username) {
        if (username.contains(".")) {
            return username.split("\\.")[0]; // Split by dot and take the first part
        }
        return username; // Return as-is if no dot
    }

    private String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
