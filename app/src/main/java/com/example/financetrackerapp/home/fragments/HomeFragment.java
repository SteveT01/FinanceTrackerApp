package com.example.financetrackerapp.home.fragments;

// Importing necessary Android and Jetpack components for fragment and navigation.
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Importing generated binding classes for the fragment layouts.
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.FragmentHomeBinding;
import com.example.financetrackerapp.databinding.FragmentProfileBinding;

// HomeFragment class declaration, extending the Fragment class to create a UI section within the app.
public class HomeFragment extends Fragment {
    // Variable for binding instance to access views in the fragment_home.xml layout directly.
    FragmentHomeBinding binding;

    // Default constructor for the fragment. Required for the Android framework to re-instantiate the fragment.
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Handling any arguments passed to the fragment. Currently, there is no implementation,
        // indicating future use for data passing or configurations.
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Argument handling logic can be added here.
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater);
        // Returns the root view from the binding class, effectively inflating the layout for the fragment.
        return binding.getRoot();
    }

    // This method is called immediately after onCreateView() and is used for final initializations.
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setting the toolbar title to "Home" using the binding class to access the TextView directly.
        binding.rlTop.tvLabel.setText("Home");

        // Setting onClickListeners for the CardViews representing Income and Expense sections.
        // Uses the Navigation component to navigate to the respective fragments upon click events.

        // Listener for the Income CardView. On click, it navigates to the IncomeFragment.

        binding.cvIncome.setOnClickListener(view1 ->
                Navigation.findNavController(view1).navigate(R.id.action_homeFragment_to_incomeFragment));

        // Listener for the Expense CardView. On click, it navigates to the ExpenseFragment.
        binding.cvExpense.setOnClickListener(view12 ->
                Navigation.findNavController(view12).navigate(R.id.action_homeFragment_to_expenseFragment));

    }
}