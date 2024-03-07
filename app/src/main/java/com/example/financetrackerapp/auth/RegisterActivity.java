// Package declaration of the RegisterActivity within the finance tracker app's authentication module.
package com.example.financetrackerapp.auth;

// Importing necessary classes and packages for the activity.
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.ActivityRegisterBinding;
import com.example.financetrackerapp.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Definition of the RegisterActivity class extending AppCompatActivity for activity behaviour.
public class RegisterActivity extends AppCompatActivity {
    // Using ActivityRegisterBinding for data binding to access UI components directly.
    ActivityRegisterBinding binding;
    // Variables to store user input.
    String name, email, password;
    // ProgressDialog to inform users that registration is in progress.
    ProgressDialog progressDialog;
    // FirebaseAuth instance for handling authentication
    FirebaseAuth auth;
    //DatabaseReference for interatcting with Firebase Database, specifically the Users node.
    DatabaseReference dbRefUsers;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using data binding.
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Set the colour of the status bar to match the app theme.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_color));

        // Setting up the UI componenets and listeners for the activity.
        binding.rlTop.tvLabel.setText("Register");
        binding.rlTop.ivBack.setVisibility(View.VISIBLE);
        binding.rlTop.ivBack.setOnClickListener(v -> finish());
        binding.llBottom.setOnClickListener(v -> finish());

        // Initializing the ProgressDialog with context.
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Initializing FirebaseAuth and DatabaseReference instances.
        auth = FirebaseAuth.getInstance();
        dbRefUsers = FirebaseDatabase.getInstance().getReference("Users");

        // OnClickListener for the register button to trigger the registeration process.
        binding.btnRegister.setOnClickListener(view -> {
            if (isValidated()){
                registerUser();
            }
        });

    }

    // Method to handle the user registration using Firebase Authentication and Realtime Database.
    private void registerUser() {
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            // Creating a new UserModel instance with user input.
            UserModel model = new UserModel(name, email, password, "");
            // Saving the UserModel instance to Firebase Database under the Users node with the UID as the key.
            dbRefUsers.child(auth.getCurrentUser().getUid()).setValue(model).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                showMessage("Registered Successfully");
                finish();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                showMessage(e.getLocalizedMessage());
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            showMessage(e.getLocalizedMessage());
        });
    }

    // Method to validate user input before proceeding with registeration.
    private Boolean isValidated(){
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passET.getText().toString().trim();

        // Input validation checks for name, email, and password
        if (name.isEmpty()){
            showMessage("Please enter name");
            return false; // Added return statement for consistency
        }
        if (email.isEmpty()){
            showMessage("Please enter email");
            return false;
        }
        if (!(Patterns.EMAIL_ADDRESS).matcher(email).matches()) {
            showMessage("Please enter email in correct format");
        }

        if (password.isEmpty()){
            showMessage("Please enter password");
            return false;
        }

        return true; // Returns true if all validations pass.
    }

    // Helper method to show toast messages.
    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}