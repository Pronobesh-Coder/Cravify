package com.example.cravify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private ImageView profileImageView, changePhoto;
    private TextView userNameTextView, userAgeTextView, userPhoneTextView, userEmailTextView, userAddressTextView;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        profileImageView = view.findViewById(R.id.profile_image);
        logoutButton = view.findViewById(R.id.logout_button);
        userNameTextView = view.findViewById(R.id.user_name);
        userAgeTextView = view.findViewById(R.id.user_age);
        userPhoneTextView = view.findViewById(R.id.user_phone);
        userEmailTextView = view.findViewById(R.id.user_email);
        userAddressTextView = view.findViewById(R.id.user_address);
        changePhoto = view.findViewById(R.id.change_photo_button);

        logoutButton.setOnClickListener(v -> signOut());
        changePhoto.setOnClickListener(v -> pickImage());

        // Fetch user data from Firestore
        fetchUserProfile();

        return view;
    }

    private void fetchUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Fetching profile for userId: " + userId);

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document exists");

                            String name = document.getString("Name");
                            String age = document.getString("Age");
                            String phone = document.getString("Phone");
                            String email = document.getString("Email");
                            String address = document.getString("Address");
                            String profileImageUrl = document.getString("profileImageUrl");

                            if (name != null) userNameTextView.setText(name);
                            if (age != null) userAgeTextView.setText("Age : " + age);
                            if (phone != null) userPhoneTextView.setText("Phone : " + phone);
                            if (email != null) userEmailTextView.setText("Email : " + email);
                            if (address != null) userAddressTextView.setText("Address : " + address);

                            // Check if profileImageUrl is empty or null
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadProfileImage(profileImageUrl);
                            } else {
                                // Use placeholder image if profileImageUrl is empty or null
                                profileImageView.setImageResource(R.drawable.ic_placeholder);
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "Fetch failed with ", task.getException());
                    }
                });
    }

    private void loadProfileImage(String imageUrl) {
        // Load the image and apply circle crop
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                //.placeholder(R.drawable.ic_placeholder)  // Placeholder image
                .into(profileImageView);
    }


    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        Bitmap croppedBitmap = cropToCircle(bitmap);
                        profileImageView.setImageBitmap(croppedBitmap);
                        uploadImageToFirebase(croppedBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap cropToCircle(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final RectF rectF = new RectF(0, 0, size, size);
        final Rect rect = new Rect(0, 0, size, size);
        final Path path = new Path();
        path.addCircle(size / 2.0f, size / 2.0f, size / 2.0f, Path.Direction.CCW);

        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawPath(path, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference imageRef = storageReference.child("profileImages/" + userId + ".jpg");

        imageRef.putBytes(bitmapToByteArray(bitmap))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateProfileImageUrl(imageUrl);
                            loadProfileImage(imageUrl);
                            Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.d(TAG, "Upload failed: ", task.getException());
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private void updateProfileImageUrl(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).update("profileImageUrl", imageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profile image URL updated successfully");
                    } else {
                        Log.d(TAG, "Update failed: ", task.getException());
                    }
                });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), UserLogin.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
