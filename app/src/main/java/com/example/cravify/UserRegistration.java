package com.example.cravify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.HashMap;
import java.util.Map;

public class UserRegistration extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "UserRegistration";

    ImageView btn_back;
    EditText Name, Age, Phone, Email, Password;
    TextView AddressTextView;
    Button Regis;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore db;
    CountryCodePicker ccp;
    GoogleMap mMap;
    LatLng selectedLocation;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getEmail().split("@")[0];
            Intent intent = new Intent(UserRegistration.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        EdgeToEdge.enable(this);
        btn_back = findViewById(R.id.back);
        Name = findViewById(R.id.name);
        Age = findViewById(R.id.age);
        Phone = findViewById(R.id.phone);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        AddressTextView = findViewById(R.id.address);
        Regis = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progBar);
        ccp = findViewById(R.id.country_code);
        ccp.registerCarrierNumberEditText(Phone);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserLogin.class);
            startActivity(intent);
            finish();
        });

        Regis.setOnClickListener(view -> {
            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();
            String name = Name.getText().toString().trim();
            String stringAge = Age.getText().toString().trim();
            String phone = Phone.getText().toString().trim();
            String address = AddressTextView.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(name) || TextUtils.isEmpty(stringAge) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(address) || selectedLocation == null) {
                Toast.makeText(getApplicationContext(), "Enter the Required Fields!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    progressBar.setVisibility(View.VISIBLE);

                    String fullPhone = ccp.getFullNumberWithPlus();
                    int age = Integer.parseInt(stringAge);
                    Map<String, Object> user = new HashMap<>();
                    user.put("Name", name);
                    user.put("Age", age);
                    user.put("Email", email);
                    user.put("Phone", fullPhone);
                    user.put("Address", address);
                    //user.put("Location", selectedLocation);  // Store the selected location

                    db.collection("users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getApplicationContext(), "Details Added!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Failed to add details!", Toast.LENGTH_SHORT).show();
                                }
                            });

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UserRegistration.this, "Account Created!",
                                                Toast.LENGTH_SHORT).show();
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        String username = firebaseUser.getEmail().split("@")[0];
                                        Intent intent = new Intent(UserRegistration.this, MainActivity.class);
                                        intent.putExtra("username", username);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UserRegistration.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Failed to register user!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error during registration", e);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(latLng -> {
            if (mMap != null) {
                mMap.clear(); // Clear existing markers
                selectedLocation = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // Convert latitude and longitude to address and set to TextView
                String address = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
                AddressTextView.setText(address);
            }
        });
    }
}

