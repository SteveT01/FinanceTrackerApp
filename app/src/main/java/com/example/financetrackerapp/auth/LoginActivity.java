package com.example.financetrackerapp.auth;

// Import statements for required classes and libraries
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.ActivityLoginBinding;
import com.example.financetrackerapp.home.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

//LoginActivity class definition extending AppCompatActivity for activity behaviour.
public class LoginActivity extends AppCompatActivity {
    // Data binding for ActivityLogin layout to access its views directly.
    ActivityLoginBinding binding;
    // Variables to store user input for email and password.
    String email, password;
    // ProgressDialog to inform users that a process is ongoing.
    ProgressDialog progressDialog;
    // FirebaseAuth instance to handle login with Firebase
    FirebaseAuth auth;

    // SuppressLint annotation to ignore specific lint warnings. Here, it's used for setting text directly in setText method.
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using data binding.
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Set the color of the status bar to match the app theme.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_color));

        // Setting the label of the top RelativeLayout to "Login".
        binding.rlTop.tvLabel.setText("Login");
        // OnClickListener for the register TextView to navigate to the RegisterActivity.
        binding.llRegister.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Initializing the ProgressDialog with context.
        progressDialog = new ProgressDialog(this);
        // Initializing FirebaseAuth instance.
        auth = FirebaseAuth.getInstance();
        // Configuring ProgressDialog properties.
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Setting OnClickListener for the login button.
        binding.btnLogin.setOnClickListener(v -> {
            // Validate input fields before attempting login.
            if (isValidated()){
                // Show ProgressDialog when validation passes and login attempt begins.
                progressDialog.show();
                // Attempt to sign in with email and password.
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Dismiss ProgressDialog and navigate to HomeActivity on successful login.
                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }else{
                            // Show error message if login fails.
                            showMessage(String.valueOf(task.getException()));
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show failure message on login attempt failure.
                        showMessage(e.getMessage());
                        progressDialog.dismiss();
                    }
                });
            }
        });

    }

    // Validates the email and password input by the user.
    private Boolean isValidated() {
        // Trimming to remove any leading or trailing white spaces.
        email = binding.emailEt.getText().toString().trim();
        password = binding.passET.getText().toString().trim();

        // Check if the email field is empty and show a message if it is.
        if (email.isEmpty()) {
            showMessage("Please enter email");
            return false;
        }
        // Check if the email entered is in the correct format.
        if (!(Patterns.EMAIL_ADDRESS).matcher(email).matches()) {
            showMessage("Please enter email in correct format");
        }
        // Check if the password field is empty and show a message if it is.
        if (password.isEmpty()) {
            showMessage("Please enter password");
            return false;
        }

        // Return true if all validations pass.
        return true;
    }

    // Helper method to show toast messages.
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}