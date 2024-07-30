package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RestaurantDashboard extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView dishImageView;
    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_dashboard);
        EdgeToEdge.enable(this);

        // Initialize views
        Button logout = findViewById(R.id.logout_button);
        TextView restaurantNameTextView = findViewById(R.id.restaurant_name_text_view);
        TextView resCuisine = findViewById(R.id.cuisine);
        TextView resAddress = findViewById(R.id.current_res_location);
        TextView resEmail = findViewById(R.id.email);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        dishImageView = findViewById(R.id.dish_image_view);
        Button selectImageButton = findViewById(R.id.uploadDishImageButton);
        Button clear = findViewById(R.id.clear);

        // Set up select image button
        selectImageButton.setOnClickListener(v -> openFileChooser());

        // Retrieve data from Intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("res_name");
        String cuisine = intent.getStringExtra("cuisine");
        String address = intent.getStringExtra("address");
        String email = intent.getStringExtra("email");

        // Display the retrieved data
        if (restaurantName != null) {
            restaurantNameTextView.setText(capitalizeFirstLetter(restaurantName));
        } else {
            restaurantNameTextView.setText("Restaurant");
        }

        if (cuisine != null) {
            resCuisine.setText(cuisine);
        } else {
            resCuisine.setText("-");
        }

        if (address != null) {
            resAddress.setText(address);
        } else {
            resAddress.setText("-");
        }

        // Set up add dish button
        Button addDishButton = findViewById(R.id.addDishButton);
        EditText dishNameEditText = findViewById(R.id.dishNameEditText);
        EditText dishDescriptionEditText = findViewById(R.id.dishDescriptionEditText);
        RadioGroup dishTypeRadioGroup = findViewById(R.id.dishTypeRadioGroup);
        EditText dishPriceEditText = findViewById(R.id.dishPriceEditText);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dishNameEditText.setText("");
                dishDescriptionEditText.setText("");
                dishPriceEditText.setText("");
                Toast.makeText(getApplicationContext(),"Cleared!",Toast.LENGTH_SHORT).show();
            }
        });

        addDishButton.setOnClickListener(v -> {
            String dishName = dishNameEditText.getText().toString().trim();
            String dishDescription = dishDescriptionEditText.getText().toString().trim();
            String dishType = ((RadioButton) findViewById(dishTypeRadioGroup.getCheckedRadioButtonId())).getText().toString();
            double dishPrice = Double.parseDouble(dishPriceEditText.getText().toString().trim());

            if (dishName.isEmpty() || dishDescription.isEmpty() || dishPriceEditText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri == null) {
                Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("menu_images/" + dishName + ".jpg");

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Map<String, Object> menuItem = new HashMap<>();
                                menuItem.put("name", dishName);
                                menuItem.put("description", dishDescription);
                                menuItem.put("type", dishType);
                                menuItem.put("price", dishPrice);
                                menuItem.put("imageUrl", uri.toString());

                                db.collection("restaurants")
                                        .document(restaurantName)
                                        .collection("menuItems")
                                        .add(menuItem)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(getApplicationContext(), "Dish Added!", Toast.LENGTH_SHORT).show();
                                            // Clear inputs and hide image view
                                            dishNameEditText.setText("");
                                            dishDescriptionEditText.setText("");
                                            dishPriceEditText.setText("");
                                            dishTypeRadioGroup.clearCheck();
                                            dishImageView.setVisibility(View.GONE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getApplicationContext(), "Could not add dish!", Toast.LENGTH_SHORT).show();
                                        });
                            }))
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Image upload failed!", Toast.LENGTH_SHORT).show());
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logout.setOnClickListener(view -> signOutAndRedirect());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            dishImageView.setImageURI(imageUri);
            dishImageView.setVisibility(View.VISIBLE);
        }
    }

    private void signOutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        Intent loginIntent = new Intent(RestaurantDashboard.this, RestaurantLogin.class);
        startActivity(loginIntent);
        finish();
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Sign out the user when the activity is stopped
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Additional sign out to ensure user is signed out in case onStop is not called
        FirebaseAuth.getInstance().signOut();
    }
}
