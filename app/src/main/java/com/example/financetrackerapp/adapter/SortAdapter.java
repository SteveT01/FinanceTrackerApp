package com.example.financetrackerapp.adapter;

// Import statements
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.financetrackerapp.databinding.ItemSortBinding; // Binding class for the item layout

import java.util.ArrayList;

// SortAdapter class extending BaseAdapter to provide custom list items
public class SortAdapter extends BaseAdapter {
    Context context; // Context in which the adapter is being used
    ArrayList<String> list; // Data source of the adapter

    // Constructor to initialize the context and data source
    public SortAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    // Returns the number of items in the data source
    @Override
    public int getCount() {
        return list.size();
    }

    // Returns the item at the specified position in the data source
    @Override
    public Object getItem(int position) {
        return list.get(position); // Return the item at the specified position
    }

    // Returns the row id of the item at the specified position
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Creates a new View for each item referenced by the Adapter
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the custom layout using data binding
        ItemSortBinding binding = ItemSortBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        // Set the text of the TextView to the item's string value
        binding.tvTitle.setText(list.get(position));
        // Return the root view of the binding
        return binding.getRoot();
    }
}
