package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView restaurantRecyclerView;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> restaurantList;
    private String username;
    private String address;
    private SearchView searchView;


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

        SliderView sliderView;
        int[] images ={
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3,
                R.drawable.image4
        };

        sliderView = view.findViewById(R.id.imageSlider);

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
