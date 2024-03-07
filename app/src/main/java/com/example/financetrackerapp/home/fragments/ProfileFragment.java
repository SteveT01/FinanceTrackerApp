package com.example.financetrackerapp.home.fragments;

// Importing necessary Android, Firebase and other library classes
import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide; // Library for image loading
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.auth.LoginActivity;
import com.example.financetrackerapp.databinding.FragmentProfileBinding; // Binding class generated for the profile fragment layout
import com.example.financetrackerapp.model.UserModel; // Custom model class for user data
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

// CLass declaration for ProfileFragment extending Fragment
public class ProfileFragment extends Fragment {
    // Declaring variables for UI binding, user information, progress dialog, Firebase auth, database reference, and storage reference
    FragmentProfileBinding binding;
    String name, imageUri = "";
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference dbRefUsers;
    StorageReference storageReference;

    // Default constructor
    public ProfileFragment() {
        // Required empty public constructor
    }

    // onCreate method for fragment initialization
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    // onCreateView method to inflate the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater);
        return binding.getRoot(); // Returning the root view of the inflated layout
    }

    // onViewCreated method to initialize UI components and set up listeners
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setting the toolbar label
        binding.rlTop.tvLabel.setText("Profile");

        // Initializing progress dialog for loading indication
        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Firebase initialization for auth, database reference, and storage reference
        auth = FirebaseAuth.getInstance();
        dbRefUsers = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");

        // Setting onClickListener to launch image selection intent
        binding.ivTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), 123);
            }
        });

        // Listener for saving changes
        binding.btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidated()){
                    saveChanges();
                }
            }
        });

        // Listener for logout functionality
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finishAffinity(); // Clears the activity stack for the logout process
            }
        });

    }

    // Method to save changes to user profile
    private void saveChanges() {
        progressDialog.show();
        if (imageUri.isEmpty()){
            // Update only name if no new image is selecte
            Map<String, Object> update = new HashMap<String, Object>();
            update.put("name", name);
            dbRefUsers.child(auth.getCurrentUser().getUid()).updateChildren(update).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Successfully Saved");
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getMessage());
            });
        }else{
            // If a new image is selected, upload it to Firebase Storage and update user profile with new image URL and name
            Uri uriImage = Uri.parse(imageUri);
            StorageReference imageRef = storageReference.child(uriImage.getLastPathSegment());
            imageRef.putFile(uriImage).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUri = uri.toString();
                Map<String, Object> update = new HashMap<>();
                update.put("name", name);
                update.put("image", downloadUri);
                dbRefUsers.child(auth.getCurrentUser().getUid()).updateChildren(update).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    showMessage("Successfully Saved");
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showMessage(e.getMessage());
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getLocalizedMessage());
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getLocalizedMessage());
            });
        }

    }

    // Handling result from image selection intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData().toString();
            binding.ivProfile.setImageURI(data.getData());
        }
    }

    // onResume method to refresh user data
    @Override
    public void onResume() {
        super.onResume();
        getUserData(); // Fetches user data from Firebase and updates UI
    }

    // Fetches and displays the current user's data from Firebase
    private void getUserData() {

        progressDialog.show();
        dbRefUsers.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    binding.etUsername.setText(model.getName());
                    binding.etEmail.setText(model.getEmail());
                    binding.etPassword.setText(model.getPassword());
                    if (!model.getImage().isEmpty()){
                        Glide.with(binding.getRoot()).load(model.getImage()).into(binding.ivProfile);
                    }
                    // Update local imageUri variable if it's empty and the model has an image URL
                    if (imageUri.isEmpty()){
                        if (model.getImage() == null){
                            imageUri = "";
                        }else{
                            imageUri = model.getImage();
                        }
                    }
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Validates user input before saving changes
    private Boolean isValidated() {
        name = binding.etUsername.getText().toString().trim();

        if (name.isEmpty()) {
            showMessage("Please enter userName");
            return false;
        }

        return true;
    }

    // Utility method to show toast messages
    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}