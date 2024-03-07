package com.example.financetrackerapp;

// Importing necessary classes and packages for the activity.
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

// Importing specific classes from the application for authentication and data binding.
import com.example.financetrackerapp.auth.LoginActivity;
import com.example.financetrackerapp.databinding.ActivityMainBinding;

// Declaration of the MainActivity class which extends AppCompatActivity,
// allowing it to function as an activity within the app.
public class MainActivity extends AppCompatActivity {
    // Binding variable to interact with the activity's views without using findViewById.
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing the binding for this activity using the inflate method,
        // which allows for direct interaction with the views defined in the corresponding XML layout file.
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Setting the content view of this activity to the root view of the binding class,
        // effectively initializing the UI as defined in the XML layout file for this activity.
        setContentView(binding.getRoot());

        // Setting the color of the status bar to a specific color defined in the app's resources.
        // This enhances the visual continuity of the app's theme.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_color));

        // Setting an OnClickListener on the 'Get Started' button. When the button is clicked,
        // this listener will execute the onClick method.
        binding.btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating an intent to start the LoginActivity. This intent specifies
                // MainActivity as the context and LoginActivity as the class to start.
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

                // Calling finish() to remove this activity from the stack. This ensures that
                // when the user presses the back button from the LoginActivity, they won't return to this MainActivity,
                // which is typical behavior for a splash or introductory screen.
                finish();
            }
        });

    }
}