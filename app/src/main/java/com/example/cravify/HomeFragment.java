package com.example.cravify;

import android.content.SharedPreferences;
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

        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        String username = getArguments() != null ? getArguments().getString("username") : getUsernameFromPreferences();
        String address = getArguments() != null ? getArguments().getString("address") : "Not available";

        String firstName = getFirstName(username);
        firstName = capitalizeFirstLetter(firstName);

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

    private String getUsernameFromPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE);
        return preferences.getString("full_name", "User");
    }

    private String getFirstName(String username) {
        if (username != null && username.contains(".")) {
            return username.split("\\.")[0];
        }
        return username != null ? username : "User";
    }

    private String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
