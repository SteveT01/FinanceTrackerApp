package com.example.financetrackerapp.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

// HomeActivity class definition, extending AppCompatActivity for compatibility support.
public class HomeActivity extends AppCompatActivity {
    // Binding instance for accessing views in the activity's layout.
    ActivityHomeBinding binding;
    // Navigation host fragment that contains the navigation graph.
    NavHostFragment navHostFragment;
    // Controller for navigating among fragments within the navHostFragment.
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this activity using view binding.
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the color of the status bar to match the app's theme.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_color));

        // Initialize the navigation components.
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        // Setup bottom navigation with NavController.
        setNavigation();

    }

    // Method to initialize and configure bottom navigation and its interactions.
    private void setNavigation() {

        // Add navigation items to the bottom navigation bar
        binding.rlBottomNav.bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_baseline_home_24));
        binding.rlBottomNav.bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.baseline_auto_graph_24));
        binding.rlBottomNav.bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_person_24));

        // Navigate to HomeFragment initially
        navController.navigate(R.id.homeFragment);
        binding.rlBottomNav.bottomNavigation.show(1, true);

        // Set click listener for bottom navigation items
        binding.rlBottomNav.bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @SuppressLint("SetTextI18n")
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {

                switch (model.getId()) {
                    case 1: // Home item
                        navController.navigate(R.id.homeFragment);
                        break;

                    case 2: // Stats item
                        navController.navigate(R.id.statsFragment);
                        break;

                    case 3: // Profile item
                        navController.navigate(R.id.profileFragment);
                        break;
                }

                return null; // Kotlin Unit, equivalent to void in Java.
            }
        });

        // Set click listeners for custom bottom navigation buttons
        binding.rlBottomNav.tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.homeFragment);
                binding.rlBottomNav.bottomNavigation.show(1, true);
            }
        });

        binding.rlBottomNav.tvForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlBottomNav.bottomNavigation.show(2, true);
                navController.navigate(R.id.statsFragment);
            }
        });

        binding.rlBottomNav.tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlBottomNav.bottomNavigation.show(3, true);
                navController.navigate(R.id.profileFragment);
            }
        });

    }

}