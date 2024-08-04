package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView restaurantRecyclerView;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> restaurantList;
    private List<Restaurant> filteredList = new ArrayList<>(); // Initialize the list
    private String username;
    private String address;
    private SearchView searchView;
    private View noResultsView; // Add this
    private ImageView noResultsImage; // Add this
    private TextView noResultsText;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        if (getArguments() != null) {
            username = getArguments().getString("username");
            address = getArguments().getString("address");
        } else {
            // Use defaults or retrieve from preferences
            username = "User";
            address = "Not available";
        }

        String firstName = getFirstName(username);
        firstName = capitalizeFirstLetter(firstName);

        TextView greetingTextView = view.findViewById(R.id.username_crave);
        if (greetingTextView != null) {
            greetingTextView.setText("Hello! " + firstName + ", What are you craving today?");
        }

        TextView addressTextView = view.findViewById(R.id.current_location_txt);
        if (addressTextView != null) {
            addressTextView.setText(address);
        }

        searchView = view.findViewById(R.id.rectangle_3);

        noResultsView = view.findViewById(R.id.no_results_view);
        noResultsImage = view.findViewById(R.id.no_results_image);
        noResultsText = view.findViewById(R.id.no_results_text);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchIcon.setColorFilter(Color.BLACK);
        searchEditText.setTextColor(Color.BLACK);
        closeIcon.setColorFilter(Color.BLACK);
        searchEditText.setHint("Enter restaurant name or cuisine");
        searchEditText.setHintTextColor(Color.GRAY);
        searchEditText.setTextSize(15);


        SliderView sliderView;
        int[] images ={
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3,
                R.drawable.image4
        };

        sliderView = view.findViewById(R.id.imageSlider);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRestaurants(newText);
                return true;
            }
        });

        SliderAdapter sliderAdapter = new SliderAdapter(images);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.DROP);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.startAutoCycle();

        restaurantRecyclerView = view.findViewById(R.id.restaurant_recyclerview);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        restaurantList = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(restaurantList, getContext(), restaurant -> {
            String fullAddress = restaurant.getAddress();
            String[] addressParts = fullAddress.split(",");

            String addressPart = "";
            if (addressParts.length > 3) {
                addressPart = addressParts[2].trim() + "," + addressParts[3].trim();
            } else if (addressParts.length > 2) {
                addressPart = addressParts[2].trim();
            }

            Intent intent = new Intent(getActivity(), RestaurantMenuActivity.class);
            intent.putExtra("restaurantName", restaurant.getName());
            intent.putExtra("restaurantCuisine", restaurant.getCuisine());
            intent.putExtra("restaurantAddress", addressPart);
            startActivity(intent);
        });
        restaurantRecyclerView.setAdapter(restaurantAdapter);

        loadRestaurantData();

        return view;
    }
    private void loadRestaurantData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        restaurantList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("Name");
                            String cuisine = document.getString("Cuisine");
                            String imageUrl = document.getString("ImageUrl");
                            String address = document.getString("Address");

                            if (name != null && cuisine != null && imageUrl != null && address != null) {
                                Restaurant restaurant = new Restaurant(name, cuisine, imageUrl, address);
                                restaurantList.add(restaurant);
                            }
                        }
                        restaurantAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Restaurant Not Found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterRestaurants(String query) {
        if (filteredList == null) {
            filteredList = new ArrayList<>();
        } else {
            filteredList.clear();
        }
        if (query.isEmpty()) {
            filteredList.addAll(restaurantList);
        } else {
            for (Restaurant restaurant : restaurantList) {
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase()) ||
                        restaurant.getCuisine().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(restaurant);
                }
            }
        }
        restaurantAdapter.updateList(filteredList);
        checkNoResults();
    }

    private void checkNoResults() {
        if (filteredList.isEmpty()) {
            restaurantRecyclerView.setVisibility(View.GONE);
            noResultsView.setVisibility(View.VISIBLE);
        } else {
            restaurantRecyclerView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
        }
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
