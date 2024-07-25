package com.example.cravify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RestaurantRegistration extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameEditText;
    private EditText cuisineEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageView imagePreviewImageView;
    private Button uploadImageButton;
    private Button registerButton;
    private ImageView btn_back;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private CountryCodePicker ccp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_registration);

        // Initialize UI elements
        nameEditText = findViewById(R.id.name);
        cuisineEditText = findViewById(R.id.cuisine);
        phoneEditText = findViewById(R.id.phone);
        addressEditText = findViewById(R.id.address);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        imagePreviewImageView = findViewById(R.id.image_preview);
        uploadImageButton = findViewById(R.id.upload_image_button);
        registerButton = findViewById(R.id.register_button);
        btn_back = findViewById(R.id.back);
        ccp = findViewById(R.id.country_code); // Initialize CountryCodePicker
        ccp.registerCarrierNumberEditText(phoneEditText); // Bind phoneEditText to CountryCodePicker

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up image upload button click listener
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Set up register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerRestaurant();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RestaurantLogin.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imagePreviewImageView.setImageBitmap(bitmap);
                imagePreviewImageView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerRestaurant() {
        String name = nameEditText.getText().toString();
        String cuisine = cuisineEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (name.isEmpty() || cuisine.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Restaurant Registered!", Toast.LENGTH_SHORT).show();
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String username = firebaseUser.getEmail().split("@")[0];
                                    Intent intent = new Intent(RestaurantRegistration.this, RestaurantDashboard.class);
                                    intent.putExtra("res_name", username);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "User registration successful, but failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            String fullPhone = ccp.getFullNumberWithPlus();
            Map<String, Object> restaurant = new HashMap<>();
            restaurant.put("Name", name);
            restaurant.put("Cuisine", cuisine);
            restaurant.put("Phone", fullPhone); // Use fullPhone with country code
            restaurant.put("Address", address);
            restaurant.put("Email", email);

            db.collection("restaurants")
                    .add(restaurant)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getApplicationContext(), "Details Added!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Could not add details!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}