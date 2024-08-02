package com.example.cravify;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserRegistration extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "UserRegistration";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String DEFAULT_PROFILE_IMAGE_URL = "";

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
    FusedLocationProviderClient fusedLocationClient;
    AutocompleteSupportFragment autocompleteFragment;
    Location currentLocation;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getEmail().split("@")[0];
            Intent intent = new Intent(UserRegistration.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("address", AddressTextView.getText().toString());
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        EdgeToEdge.enable(this);

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize the AutocompleteSupportFragment
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

            // Change the text color and size programmatically
            autocompleteFragment.getView().post(() -> {
                View view = autocompleteFragment.getView();
                if (view != null) {
                    LinearLayout linearLayout = (LinearLayout) view;
                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                        View child = linearLayout.getChildAt(i);
                        if (child instanceof LinearLayout) {
                            LinearLayout innerLinearLayout = (LinearLayout) child;
                            for (int j = 0; j < innerLinearLayout.getChildCount(); j++) {
                                View innerChild = innerLinearLayout.getChildAt(j);
                                if (innerChild instanceof EditText) {
                                    EditText autocompleteTextView = (EditText) innerChild;
                                    autocompleteTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                                    autocompleteTextView.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                                    autocompleteTextView.setTextSize(13); // Set text size to 13sp
                                }
                            }
                        }
                    }
                }
            });

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Get the place's details and update the map
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        selectedLocation = latLng;
                        if (mMap != null) {
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            AddressTextView.setText(place.getAddress());
                        }
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(UserRegistration.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize Views
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Handle back button click
        btn_back.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserLogin.class);
            startActivity(intent);
            finish();
        });

        // Handle register button click
        Regis.setOnClickListener(view -> {
            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();
            String name = Name.getText().toString().trim();
            String stringAge = Age.getText().toString().trim();
            String phone = Phone.getText().toString().trim();
            String address = AddressTextView.getText().toString().trim();

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("address", address);
            editor.apply();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(name) || TextUtils.isEmpty(stringAge) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
                Toast.makeText(getApplicationContext(), "Enter the Required Fields!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            String userId = firebaseUser.getUid();
                                            String fullPhone = ccp.getFullNumberWithPlus();
                                            //int age = Integer.parseInt(stringAge);

                                            Map<String, Object> user = new HashMap<>();
                                            user.put("Name", name);
                                            user.put("Age", stringAge);
                                            user.put("Email", email);
                                            user.put("Phone", fullPhone);
                                            user.put("Address", address);
                                            user.put("profileImageUrl", DEFAULT_PROFILE_IMAGE_URL);

                                            LatLng locationToStore = selectedLocation != null ? selectedLocation : (currentLocation != null ? new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()) : null);
                                            if (locationToStore != null) {
                                                user.put("Location", locationToStore);
                                            }

                                            // Use userId as the document ID
                                            db.collection("users").document(userId).set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getApplicationContext(), "Details Added!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(UserRegistration.this, MainActivity.class);
                                                            intent.putExtra("username", firebaseUser.getEmail().split("@")[0]);
                                                            intent.putExtra("address", address); // Pass the address
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(UserRegistration.this, "Failed to add details!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(UserRegistration.this, "Failed to create account!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch (NumberFormatException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Invalid age format!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Request location permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    // Request location permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get current location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (mMap != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                            Geocoder geocoder = new Geocoder(UserRegistration.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    String address = addresses.get(0).getAddressLine(0);
                                    AddressTextView.setText(address);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    // Initialize map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
    }
}
