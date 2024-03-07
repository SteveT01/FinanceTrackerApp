package com.example.financetrackerapp.home.fragments;

// Import statements
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.financetrackerapp.R;
import com.example.financetrackerapp.adapter.SortAdapter; // Adapter for sorting options
import com.example.financetrackerapp.databinding.FragmentStatsBinding; // Binding for this fragment
import com.example.financetrackerapp.model.AmountModel; // Model for income/expense data
import com.github.mikephil.charting.charts.PieChart; // Library for pie chart
import com.github.mikephil.charting.data.PieData; // Data for pie chart
import com.github.mikephil.charting.data.PieDataSet; // Data for pie chart
import com.github.mikephil.charting.data.PieEntry; // Entries for pie chart
import com.github.mikephil.charting.utils.ColorTemplate; // Colour template for chart
import com.google.firebase.auth.FirebaseAuth; // Firebase Authentication
import com.google.firebase.database.DataSnapshot; // Snapshot for Firebase data
import com.google.firebase.database.DatabaseError; // Error handling for Firebase
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference; // Database reference for Firebase
import com.google.firebase.database.FirebaseDatabase; // Firebase Database
import com.google.firebase.database.ValueEventListener; // Event listener for Firebase

import java.text.SimpleDateFormat; // Formatting dates
import java.util.ArrayList; // Dynamic arrays
import java.util.Calendar; // Calendar for date calculations
import java.util.Date; // Date class for handling dates

public class StatsFragment extends Fragment {
    // View binding for this fragment
    FragmentStatsBinding binding;
    // ProgressDialog to show loading messages
    ProgressDialog progressDialog;
    // Firebase auth instance for current user
    FirebaseAuth auth;
    // Database references for income and expense
    DatabaseReference dbRefIncome;
    DatabaseReference dbRefExpense;
    // Lists for holding income and expense data

    // Variables to track total income, expense and balance
    double totalIncome = 0;
    double totalExpense = 0;
    double totalBalance = 0;
    ArrayList<String> sortList = new ArrayList<>();
    // Adapter for sorting the statistics
    SortAdapter sortAdapter;
    // Variable for user-selected sorting option
    String sortBy;
    // PieChart for displaying income vs expense
    private PieChart pieChart;

    // Required empty public constructor
    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle fragment arguments if any
        if (getArguments() != null) {

        }
    }

    // Inflate the fragment layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatsBinding.inflate(inflater);
        return binding.getRoot();
    }

    // Setup the fragment view and intialize components
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Initialize Firebase auth and database references
        auth = FirebaseAuth.getInstance();
        dbRefIncome = FirebaseDatabase.getInstance().getReference("Incomes");
        dbRefExpense = FirebaseDatabase.getInstance().getReference("Expenses");

        binding.rlTop.tvLabel.setText("Stats");
        setSortAdapter();
        pieChart = binding.pieChart;
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch all incomes and expenses to update statistics
        getAllIncomes();

    }

    // Fetch all income records from Firebase
    private void getAllIncomes(){
        // Show ProgressDialog while loading data
        progressDialog.show();
        dbRefIncome.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    totalIncome = 0;
                    totalExpense = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            AmountModel model = ds.getValue(AmountModel.class);
                            if (model.getUserId().equals(auth.getCurrentUser().getUid())) {
                                if (isDateInRange(model.getFormattedDate())) {
                                    totalIncome = totalIncome + Double.parseDouble(model.getAmount());
                                }
                            }
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                        }
                    }

                    getAllExpenses();
                } else {
                    getAllExpenses();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch all expense records from Firebase
    private void getAllExpenses() {
        // Fetch data and handle the response
        dbRefExpense.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    totalExpense = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            AmountModel model = ds.getValue(AmountModel.class);
                            if (model.getUserId().equals(auth.getCurrentUser().getUid())) {
                                if (isDateInRange(model.getFormattedDate())) {
                                    totalExpense = totalExpense + Double.parseDouble(model.getAmount());
                                }
                            }
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setupPieChart();
                totalBalance =  totalIncome - totalExpense;
                binding.tvTotalIncome.setText("Total Income: £" + totalIncome);
                binding.tvTotalExpense.setText("Total Expense: £" + totalExpense);
                if (totalBalance > 0){
                    binding.tvTotalBalance.setText("Account Balance: £" + totalBalance);
                }
                if (totalIncome == 0 && totalExpense == 0) {
                    binding.clMain.setVisibility(View.GONE);
                    binding.tvNoDataFound.setVisibility(View.VISIBLE);
                } else {
                    binding.clMain.setVisibility(View.VISIBLE);
                    binding.tvNoDataFound.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Setup the PieChart with income and expense data
    private void setupPieChart() {
        // Configure sorting options and handle user selections
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalIncome, "Income"));
        entries.add(new PieEntry((float) totalExpense, "Expense"));

        PieDataSet dataSet = new PieDataSet(entries, "Income vs Expense");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(getResources().getColor(R.color.app_color));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }


    // Configure sorting options and handle user selections
    private void setSortAdapter() {
        sortList.clear();
        sortList.add("Daily");
        sortList.add("Weekly");
        sortList.add("Monthly");
        sortList.add("Yearly");
        sortAdapter = new SortAdapter(requireContext(), sortList);
        binding.spRating.setAdapter(sortAdapter);
        binding.spRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i) != null) {
                    sortBy = (String) adapterView.getItemAtPosition(i);
                    getAllIncomes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // Checks if a date is within the range specified by sortBy option
    private boolean isDateInRange(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();

        switch (sortBy) {
            case "Daily":
                // Check if the date is today
                return date.equals(dateFormat.format(calendar.getTime()));

            case "Weekly":
                // Check if the date is within the last 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                Date startDateWeekly = calendar.getTime();
                try {
                    Date entryDateWeekly = dateFormat.parse(date);
                    return entryDateWeekly != null && entryDateWeekly.after(startDateWeekly);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    return false;
                }

            case "Monthly":
                // Check if the date is within the last 30 days
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                Date startDateMonthly = calendar.getTime();
                try {
                    Date entryDateMonthly = dateFormat.parse(date);
                    return entryDateMonthly != null && entryDateMonthly.after(startDateMonthly);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    return false;
                }

            case "Yearly":
                // Check if the date is within the last 365 days
                calendar.add(Calendar.DAY_OF_YEAR, -365);
                Date startDateYearly = calendar.getTime();
                try {
                    Date entryDateYearly = dateFormat.parse(date);
                    return entryDateYearly != null && entryDateYearly.after(startDateYearly);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    return false;
                }

            default:
                return true;
        }
    }


}