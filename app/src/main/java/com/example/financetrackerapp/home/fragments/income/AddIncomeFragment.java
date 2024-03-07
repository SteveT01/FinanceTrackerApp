package com.example.financetrackerapp.home.fragments.income;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide; // For image loading
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.FragmentAddIncomeBinding;
import com.example.financetrackerapp.model.AmountModel; // Model class for income data
import com.github.dhaval2404.imagepicker.ImagePicker; // For picking images
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddIncomeFragment extends Fragment {
    // Binding instance for accessing the views
    FragmentAddIncomeBinding binding;
    // Variables to hold form data
    String title, amount, details, imageUri = "";
    // Dialog to show progress feedback
    ProgressDialog progressDialog;
    // Firebase authentication and database references
    FirebaseAuth auth;
    DatabaseReference dbRefIncome;
    StorageReference storageReference;
    // Model instance to hold data if updating an existing income
    AmountModel previousModel;

    // Default constructor
    public AddIncomeFragment() {
        // Required empty public constructor
    }

    // onCreate method to initialize fragment state
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for any data passed to this fragment
        if (getArguments() != null) {
            // Retrieve and cast the passed data to AmountModel type
            previousModel = (AmountModel) getArguments().getSerializable("data");
        }
    }

    // onCreateView method to inflate the fragment's layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddIncomeBinding.inflate(inflater);
        return binding.getRoot();
    }

    // onViewCreated method to set up the UI elements and handlers after the view is created
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the back button and its click listener
        binding.rlTop.ivBack.setVisibility(View.VISIBLE);
        binding.rlTop.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });

        // Initialize the progress dialog for loading operations
        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Initialize Firebase Auth and database references
        auth = FirebaseAuth.getInstance();
        dbRefIncome = FirebaseDatabase.getInstance().getReference("Incomes");
        storageReference = FirebaseStorage.getInstance().getReference("IncomePictures");

        // If updating an income, pre-fill the form with existing data
        if (previousModel != null) {
            binding.rlTop.tvLabel.setText("Update Income");
            binding.btnAdd.setText("Update");
            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.etTitle.setText(previousModel.getTitle());
            binding.etIncome.setText(previousModel.getAmount());
            binding.etDetails.setText(previousModel.getDetails());
            imageUri = previousModel.getImage();
            if (!previousModel.getImage().equals("")){
                Glide.with(binding.ivImage)
                        .load(imageUri)
                        .centerCrop()
                        .into(binding.ivImage);
            }
        } else {
            // Set default text for adding a new income
            binding.rlTop.tvLabel.setText("Add Income");
        }

        // Setup image picker click listener
        binding.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Income Image"), 123);
            }
        });

        // Setup add button click listener to validate and submit the income data
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidated()) {
                    if (previousModel != null) {
                        updateIncome();
                    } else {
                        addIncome();
                    }
                }
            }
        });

        // Setup delete button click listener to delete an existing income entry
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (previousModel != null){
                    dbRefIncome.child(previousModel.getAmount()).removeValue();
                    showMessage("Deleted Successfully");
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                }
            }
        });

    }

    // Method to add a new income entry to Firebase
    private void addIncome() {
        progressDialog.show();
        if (imageUri.isEmpty()){
            // Create a new income entry without an image
            String incomeId = dbRefIncome.push().getKey();
            AmountModel model = new AmountModel(incomeId, auth.getCurrentUser().getUid(), title, amount, details, "", getCurrentDateTime());
            // Save the new income entry to Firebase
            dbRefIncome.child(incomeId).setValue(model).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Added Successfully");
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getLocalizedMessage());
            });
        }else{
            // Upload the selected image and create a new income entry with the image
            Uri uriImage = Uri.parse(imageUri);
            StorageReference imageRef = storageReference.child(uriImage.getLastPathSegment());
            imageRef.putFile(uriImage).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUri = uri.toString();
                String incomeId = dbRefIncome.push().getKey();
                AmountModel model = new AmountModel(incomeId, auth.getCurrentUser().getUid(), title, amount, details, downloadUri, getCurrentDateTime());
                dbRefIncome.child(incomeId).setValue(model).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    showMessage("Added Successfully");
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showMessage(e.getLocalizedMessage());
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

    // Method to update an existing income entry in Firebase
    private void updateIncome() {
        progressDialog.show();
        if (imageUri.isEmpty() || imageUri.contains(previousModel.getImage())) {
            // Update the income entry without changing the image
            Map<String, Object> update = new HashMap<String, Object>();
            update.put("title", title);
            update.put("amount", amount);
            update.put("details", details);
            // Save the updated income entry to Firebase
            dbRefIncome.child(previousModel.getAmountId()).updateChildren(update).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Successfully Saved");
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getMessage());
            });
        } else {
            // Upload the new image and update the income entry with the new image
            Uri uriImage = Uri.parse(imageUri);
            StorageReference imageRef = storageReference.child(uriImage.getLastPathSegment());
            imageRef.putFile(uriImage).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUri = uri.toString();
                Map<String, Object> update = new HashMap<>();
                update.put("title", title);
                update.put("amount", amount);
                update.put("details", details);
                update.put("image", downloadUri);
                dbRefIncome.child(previousModel.getAmountId()).updateChildren(update).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    showMessage("Successfully Saved");
                    Navigation.findNavController(binding.getRoot()).navigateUp();
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

    // Method to get the current date and time in a specific format
    @SuppressLint("SimpleDateFormat")
    public String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss aa");
        return dateFormat.format(calendar.getTime());
    }

    // Method to validate the input fields before submitting the data
    private Boolean isValidated() {
        // Retrieve the data from input fields
        title = binding.etTitle.getText().toString().trim();
        amount = binding.etIncome.getText().toString().trim();
        details = binding.etDetails.getText().toString().trim();

        // Validate the input data and display errors if necessary
        if (title.isEmpty()) {
            showMessage("Please enter title");
            return false;
        }

        if (amount.isEmpty()) {
            showMessage("Please enter amount");
            return false;
        }

        if (details.isEmpty()) {
            showMessage("Please enter details");
            return false;
        }

        return true; // All inputs are valid
    }

    // Method to display toast messages
    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    // Method to handle the result from the image picker activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            // Get the image URI from the selected image and display it
            imageUri = data.getData().toString();
            binding.ivImage.setImageURI(data.getData());
        }
    }
}