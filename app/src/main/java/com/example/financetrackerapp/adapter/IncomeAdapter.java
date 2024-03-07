package com.example.financetrackerapp.adapter;

// Import statements
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // For image loading
import com.example.financetrackerapp.R;
import com.example.financetrackerapp.databinding.ItemIcomeBinding; // Binding class generated
import com.example.financetrackerapp.model.AmountModel; // Data model class

import java.util.List;

// Adapter class for the RecyclerView, handling income or expense items
public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.ViewHolder> {
    // Member variables
    List<AmountModel> list; // List of income/expense items
    Context context; // To hold the context passed in the constructor, used for Glide
    String checkFrom; // To determine whether the adapter is used for income or expense items

    // Construtor
    public IncomeAdapter(List<AmountModel> list, Context context, String checkFrom) {
        this.list = list;
        this.context = context;
        this.checkFrom = checkFrom;
    }

    // Setter method to update the list of items and notify the adapter to refresh
    public void setList(List<AmountModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    // Inflates the item layout and returns a ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIcomeBinding binding = ItemIcomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Binds data to the views in the ViewHolder
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AmountModel amountModel = list.get(position);
        holder.bind(amountModel, position);
    }

    // Returns the size of the list
    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder class to hold and manage the views for each item
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemIcomeBinding binding;

        public ViewHolder(@NonNull ItemIcomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Method to bind data to the views
        @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
        public void bind(AmountModel amountModel, int position) {
            binding.tvTitle.setText(amountModel.getTitle()); // Set the title
            binding.tvAmount.setText("£"+amountModel.getAmount()); // Set the amount with currency symbol
            binding.tvDate.setText(amountModel.getDateTime()); // Set the date/time

            // Load the image with Glide if available
            if (!amountModel.getImage().equals("")){
                Glide.with(context).load(amountModel.getImage()).centerCrop().into(binding.ivNews);
            }

            // Set an onCLickListener to navigate to the detail/edit page for the item
            binding.ivNext.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", amountModel);
                // Navigate based on whether the item is an income or expense
                if (checkFrom.equals("income")){
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_incomeFragment_to_addIncomeFragment, bundle);
                }else{
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_expenseFragment_to_addExpenseFragment, bundle);
                }
            });
        }

    }
}
