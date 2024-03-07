package com.example.financetrackerapp.home.fragments.expense;

// Import statements
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
import com.example.financetrackerapp.adapter.IncomeAdapter; // Despite its name, used here for displaying expenses
import com.example.financetrackerapp.databinding.FragmentAddExpenseBinding;
import com.example.financetrackerapp.databinding.FragmentExpenseBinding;
import com.example.financetrackerapp.databinding.FragmentIncomeBinding;
import com.example.financetrackerapp.model.AmountModel; // Data model for expenses
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragment extends Fragment {
    // Binding and Firebase references
    FragmentExpenseBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference dbRefExpense;
    List<AmountModel> list = new ArrayList<>();
    double totalExpense = 0; // To keep track of the total expense
    IncomeAdapter adapter; // Adapter for displaying expenses

    // Empty constructor for the fragment
    public ExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    // Inflate the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentExpenseBinding.inflate(inflater);
        return binding.getRoot();
    }

    // Setup UI components and Firebase database reference
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Firebase authentication instance
        auth = FirebaseAuth.getInstance();
        // Reference to the 'Expenses' node in the Firebase database
        dbRefExpense = FirebaseDatabase.getInstance().getReference("Expenses");

        binding.rlTop.ivBack.setVisibility(View.VISIBLE);
        binding.rlTop.ivRight.setVisibility(View.VISIBLE);
        binding.rlTop.ivRight.setImageResource(R.drawable.baseline_add_circle_outline_24);
        binding.rlTop.tvLabel.setText("Expenses");
        binding.rlTop.ivBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.rlTop.ivRight.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_expenseFragment_to_addExpenseFragment));

    }

    // onResume method to fetch and display the expense records from Firebase
    @Override
    public void onResume() {
        super.onResume();
        progressDialog.show();
        dbRefExpense.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Process the snapshot to display expense records or show a message if none are found
                if (snapshot.exists()){
                    list.clear();
                    totalExpense = 0;
                    progressDialog.dismiss();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            AmountModel model = ds.getValue(AmountModel.class);
                            if (model.getUserId().equals(auth.getCurrentUser().getUid())){
                                list.add(model);
                                totalExpense = totalExpense+Double.parseDouble(model.getAmount());
                            }
                        }catch (DatabaseException e){
                            e.printStackTrace();
                        }
                    }

                    binding.tvTotalExpense.setText("Total Expense: £"+totalExpense);
                    setAdapter();

                    if (list.isEmpty()){
                        binding.noBudgetFoundTV.setVisibility(View.VISIBLE);
                        binding.budgetRV.setVisibility(View.GONE);
                        binding.tvTotalExpense.setVisibility(View.GONE);
                    }else{
                        binding.noBudgetFoundTV.setVisibility(View.GONE);
                        binding.budgetRV.setVisibility(View.VISIBLE);
                        binding.tvTotalExpense.setVisibility(View.VISIBLE);
                    }
                }else{
                    progressDialog.dismiss();
                    binding.noBudgetFoundTV.setVisibility(View.VISIBLE);
                    binding.budgetRV.setVisibility(View.GONE);
                    binding.tvTotalExpense.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle cancellation and errors
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // setAdapter method to configure the RecyclerView with the fetched expense records
    private void setAdapter(){
        adapter = new IncomeAdapter(list, requireContext(), "expense");
        binding.budgetRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.budgetRV.setAdapter(adapter);
    }

    // Additional helper methods like setupUI and processExpenseData to organise code for readability
}