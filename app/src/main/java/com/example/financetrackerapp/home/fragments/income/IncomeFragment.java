package com.example.financetrackerapp.home.fragments.income;

// Importing necessary Android, Firebase, and app-specific components
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.financetrackerapp.R;
import com.example.financetrackerapp.adapter.IncomeAdapter;
import com.example.financetrackerapp.databinding.FragmentIncomeBinding;
import com.example.financetrackerapp.model.AmountModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Defining the IncomeFragment class that extends Fragment to manage UI related to income tracking.
public class IncomeFragment extends Fragment {
    FragmentIncomeBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference dbRefIncome;
    List<AmountModel> list = new ArrayList<>();
    double totalIncome = 0;

    IncomeAdapter adapter;

    // Default constructor for the fragment, required for proper fragment management.
    public IncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handling any arguments passed to the fragment, for future use if required.
        if (getArguments() != null) {
            // Argument handling logic can go here.

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentIncomeBinding.inflate(inflater);
        return binding.getRoot(); // Returns the root view for the fragment, with binding initialized.
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize a ProgressDialog to display loading state during data fetch operations.
        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name)); // Setting title to app's name
        progressDialog.setMessage("Please wait..."); // Display message during loading
        progressDialog.setCancelable(false); // Prevent dismissal by tapping outside

        // Initialize Firebase Auth and DatabaseReference for income data.
        auth = FirebaseAuth.getInstance();
        dbRefIncome = FirebaseDatabase.getInstance().getReference("Incomes");

        // Setting up UI elements and navigation listeners for the Income fragment.
        binding.rlTop.ivBack.setVisibility(View.VISIBLE); // Show back button
        binding.rlTop.ivRight.setVisibility(View.VISIBLE); // Show add button
        binding.rlTop.ivRight.setImageResource(R.drawable.baseline_add_circle_outline_24); // Set add icon
        binding.rlTop.tvLabel.setText("Incomes"); // Set toolbar title to 'Incomes'.

        // Navigation listener for back button, navigates to the previous screen.
        binding.rlTop.ivBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Navigation listener for right button, navigates to the AddIncomeFragment.
        binding.rlTop.ivRight.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_incomeFragment_to_addIncomeFragment));

    }

    @Override
    public void onResume() {
        super.onResume(); // Ensures the parent class's onResume method is also executed.
        progressDialog.show(); // Display the progress dialog while data is being fetched.

        // Listen for a single snapshot of the income data, reducing the number of reads.
        dbRefIncome.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the snapshot exists to handle data or display no data UI.
                if (snapshot.exists()){
                    list.clear(); // Clear existing data to prevent duplication.
                    totalIncome = 0; // Reset total income to 0.
                    progressDialog.dismiss(); // Dismiss the loading dialog.
                    for (DataSnapshot ds : snapshot.getChildren()) { // Iterate over each child node.
                        try {
                            // Parse the snapshot into an AmountModel object.
                            AmountModel model = ds.getValue(AmountModel.class);
                            // Check if the income belongs to the current user.
                            if (model.getUserId().equals(auth.getCurrentUser().getUid())){
                                list.add(model); // Add the model to the list.
                                totalIncome = totalIncome+Double.parseDouble(model.getAmount()); // Add to total income.
                            }
                        }catch (DatabaseException e){
                            e.printStackTrace(); // Handle any parsing errors.
                        }
                    }

                    // Update the UI with the total income calculated.
                    binding.tvTotalIncome.setText("Total Income: £"+totalIncome);
                    setAdapter(); // Setup the RecyclerView adapter with the data.

                    // Toggle visibility based on whether data is found.
                    if (list.isEmpty()){
                        // Show "No Budget Found" and hide the RecyclerView and total income TextView.
                        binding.noBudgetFoundTV.setVisibility(View.VISIBLE);
                        binding.budgetRV.setVisibility(View.GONE);
                        binding.tvTotalIncome.setVisibility(View.GONE);
                    }else{
                        // Show the RecyclerView and total income TextView, hide "No Budget Found".
                        binding.noBudgetFoundTV.setVisibility(View.GONE);
                        binding.budgetRV.setVisibility(View.VISIBLE);
                        binding.tvTotalIncome.setVisibility(View.VISIBLE);
                    }
                }else{
                    // Handle case where there is no data available.
                    progressDialog.dismiss();
                    binding.noBudgetFoundTV.setVisibility(View.VISIBLE);
                    binding.budgetRV.setVisibility(View.GONE);
                    binding.tvTotalIncome.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors with database operations.
                progressDialog.dismiss(); // Ensure dialog is dismissed to prevent UI lockup.
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show(); // Display error.
            }
        });

    }

    // Method to setup the RecyclerView adapter and layout manager.
    private void setAdapter(){
        adapter = new IncomeAdapter(list, requireContext(), "income"); // Initialize adapter with data.
        binding.budgetRV.setLayoutManager(new LinearLayoutManager(requireContext())); // Set layout manager.
        binding.budgetRV.setAdapter(adapter); // Attach adapter to RecyclerView
    }

}