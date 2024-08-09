package com.example.cravify;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private ImageView editName, editAge, editPhone;
    private Button logoutButton, favouritesButton, orderHistoryButton;
    private Button deleteAccountButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        profileImageView = view.findViewById(R.id.profile_image);
        logoutButton = view.findViewById(R.id.logout_button);
        favouritesButton = view.findViewById(R.id.my_favorites_item);
        orderHistoryButton = view.findViewById(R.id.order_history_item);
        userNameTextView = view.findViewById(R.id.user_name);
        userAgeTextView = view.findViewById(R.id.user_age);
        userPhoneTextView = view.findViewById(R.id.user_phone);
        userEmailTextView = view.findViewById(R.id.user_email);
        userAddressTextView = view.findViewById(R.id.user_address);
        changePhoto = view.findViewById(R.id.change_photo_button);
        editName = view.findViewById(R.id.edit_name);
        editAge = view.findViewById(R.id.edit_age);
        editPhone = view.findViewById(R.id.edit_phone);
        deleteAccountButton = view.findViewById(R.id.delete_account_item);

        logoutButton.setOnClickListener(v -> signOut());
        changePhoto.setOnClickListener(v -> pickImage());

        editName.setOnClickListener(v -> showEditDialog("Name", userNameTextView));
        editAge.setOnClickListener(v -> showEditDialog("Age", userAgeTextView));
        editPhone.setOnClickListener(v -> showEditDialog("Phone", userPhoneTextView));

        favouritesButton.setOnClickListener(v -> navigateToSavedFragment());
        orderHistoryButton.setOnClickListener(v -> navigateToHistoryFragment());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

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
                            if (age != null) userAgeTextView.setText(age);
                            if (phone != null) userPhoneTextView.setText(phone);
                            if (email != null) userEmailTextView.setText(email);
                            if (address != null) userAddressTextView.setText(address);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                loadProfileImage(profileImageUrl);
                            } else {
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
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
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

    private void showEditDialog(String field, TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        int currentMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        View customLayout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
        EditText editText = customLayout.findViewById(R.id.editText);
        editText.setText(textView.getText().toString());

        builder.setTitle("Edit " + field)
                .setView(customLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = editText.getText().toString();
                    textView.setText(newValue);

                    updateUserProfileField(field, newValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserProfileField(String field, String newValue) {
        String userId = mAuth.getCurrentUser().getUid();
        String firestoreField;

        switch (field) {
            case "Name":
                firestoreField = "Name";
                break;
            case "Age":
                firestoreField = "Age";
                break;
            case "Phone":
                firestoreField = "Phone";
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + field);
        }

        db.collection("users").document(userId)
                .update(firestoreField, newValue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, field + " updated successfully");
                    } else {
                        Log.d(TAG, "Update failed: ", task.getException());
                    }
                });
    }

    private void navigateToSavedFragment() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).navigateToSavedFragment();
        }
    }

    private void navigateToHistoryFragment() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).navigateToHistoryFragment();
        }
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action is irreversible!");

        builder.setPositiveButton("Yes, I am sure", (dialog, which) -> deleteAccount());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            int currentMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            TextView titleView = ((TextView) dialog.findViewById(android.R.id.title));
            TextView messageView = ((TextView) dialog.findViewById(android.R.id.message));

            if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
                positiveButton.setTextColor(Color.RED);
                negativeButton.setTextColor(Color.WHITE);
                if (titleView != null) titleView.setTextColor(Color.WHITE);
                if (messageView != null) messageView.setTextColor(Color.WHITE);
            } else {
                positiveButton.setTextColor(Color.RED);
                negativeButton.setTextColor(Color.BLACK);
                if (titleView != null) titleView.setTextColor(Color.BLACK);
                if (messageView != null) messageView.setTextColor(Color.BLACK);
            }
        });
        dialog.show();
    }

    private void deleteAccount() {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Deleting account for userId: " + userId);

        StorageReference profileImageRef = storageReference.child("profileImages/" + userId + ".jpg");
        profileImageRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Profile image deleted from storage");

                db.collection("users").document(userId).delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Log.d(TAG, "User document deleted from Firestore");

                        mAuth.getCurrentUser().delete().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d(TAG, "User deleted from Firebase Authentication");
                                Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getContext(), UserLogin.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Log.e(TAG, "Failed to delete user from Firebase Authentication", task2.getException());
                                Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to delete user document from Firestore", task1.getException());
                        Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Failed to delete profile image from storage", task.getException());
                Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            mAuth.signOut();
            Intent intent = new Intent(getContext(), UserLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            int currentMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
            } else {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        dialog.show();
    }
}
