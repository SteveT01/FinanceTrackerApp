package com.example.financetrackerapp.home.fragments.expense;

import static android.app.Activity.RESULT_OK;

// Import starements
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide; // For image loading
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.FragmentAddExpenseBinding;
import com.example.financetrackerapp.databinding.FragmentAddIncomeBinding;
import com.example.financetrackerapp.model.AmountModel; // Data model class
import com.github.dhaval2404.imagepicker.ImagePicker; // For picking images
import com.google.firebase.auth.FirebaseAuth; // For Firebase authentication
import com.google.firebase.database.DatabaseReference; // For Firebase database Reference
import com.google.firebase.database.FirebaseDatabase; // For Firebase Database
import com.google.firebase.storage.FirebaseStorage; // For Firebase Storage
import com.google.firebase.storage.StorageReference; // For Firebase Storage Reference

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Fragment class to add or update expense records
public class AddExpenseFragment extends Fragment {
    // Binding and data variables
    FragmentAddExpenseBinding binding;
    String title, amount, details, imageUri = "";
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference dbRefExpense;
    StorageReference storageReference;
    AmountModel previousModel; // Model to hold the previous expense record if any

    // Empty constructor required for Fragment instantiation
    public AddExpenseFragment() {
        // Required empty public constructor
    }

    // onCreate method to initialize fragment state
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve and set the previousModel if arguments were passed to the fragment
        if (getArguments() != null) {
            previousModel = (AmountModel) getArguments().getSerializable("data");
        }
    }

    // onCreateView method to inflate the fragment's layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddExpenseBinding.inflate(inflater);
        return binding.getRoot();
    }

    // onViewCreated method to set up UI interactions after view creation
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup for the progress dialog
        binding.rlTop.ivBack.setVisibility(View.VISIBLE);
        binding.rlTop.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        dbRefExpense = FirebaseDatabase.getInstance().getReference("Expenses");
        storageReference = FirebaseStorage.getInstance().getReference("ExpensePictures");

        if (previousModel != null) {
            binding.rlTop.tvLabel.setText("Update Expense");
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
            binding.rlTop.tvLabel.setText("Add Expense");
        }

        binding.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(intent, "Select Expense Image"), 123);
            }
        });

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidated()) {
                    if (previousModel != null) {
                        updateExpense();
                    } else {
                        addExpense();
                    }
                }
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (previousModel != null){
                    dbRefExpense.child(previousModel.getAmount()).removeValue();
                    showMessage("Deleted Successfully");
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                }
            }
        });

    }

    private void addExpense() {
        progressDialog.show();
        if (imageUri.isEmpty()){
            String expenseId = dbRefExpense.push().getKey();
            AmountModel model = new AmountModel(expenseId, auth.getCurrentUser().getUid(), title, amount, details, "", getCurrentDateTime());
            dbRefExpense.child(expenseId).setValue(model).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Added Successfully");
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getLocalizedMessage());
            });
        }else{
            Uri uriImage = Uri.parse(imageUri);
            StorageReference imageRef = storageReference.child(uriImage.getLastPathSegment());
            imageRef.putFile(uriImage).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUri = uri.toString();
                String expenseId = dbRefExpense.push().getKey();
                AmountModel model = new AmountModel(expenseId, auth.getCurrentUser().getUid(), title, amount, details, downloadUri, getCurrentDateTime());
                dbRefExpense.child(expenseId).setValue(model).addOnCompleteListener(task -> {
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

    private void updateExpense() {
        progressDialog.show();
        if (imageUri.isEmpty() || imageUri.contains(previousModel.getImage())) {
            Map<String, Object> update = new HashMap<String, Object>();
            update.put("title", title);
            update.put("amount", amount);
            update.put("details", details);
            dbRefExpense.child(previousModel.getAmountId()).updateChildren(update).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Successfully Saved");
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getMessage());
            });
        } else {
            Uri uriImage = Uri.parse(imageUri);
            StorageReference imageRef = storageReference.child(uriImage.getLastPathSegment());
            imageRef.putFile(uriImage).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUri = uri.toString();
                Map<String, Object> update = new HashMap<>();
                update.put("title", title);
                update.put("amount", amount);
                update.put("details", details);
                update.put("image", downloadUri);
                dbRefExpense.child(previousModel.getAmountId()).updateChildren(update).addOnCompleteListener(task -> {
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

    @SuppressLint("SimpleDateFormat")
    public String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss aa");
        return dateFormat.format(calendar.getTime());
    }

    private Boolean isValidated() {
        title = binding.etTitle.getText().toString().trim();
        amount = binding.etIncome.getText().toString().trim();
        details = binding.etDetails.getText().toString().trim();

        if (imageUri.isEmpty()) {
            showMessage("Please select image");
            return false;
        }

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

        return true;
    }

    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData().toString();
            binding.ivImage.setImageURI(data.getData());
        }
    }
}

// Method implementations: addExpense, updateExpense, isValidated, showMessage, onActivityResult
// These methods handle adding a new expense, updating an existing one, validating input data,
// showing Toast messages, and handling the result of an image picking activity, respectively.

// Utility methods: getCurrentDateTime to get the current date and time
// setupAddMode to configure the UI for adding a new expense
