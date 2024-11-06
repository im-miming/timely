package com.cs046_project.timely.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cs046_project.timely.R;

import java.util.ArrayList;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;

    public CustomSpinnerAdapter(Context context, ArrayList<String> items) {
        super(context, R.layout.custom_dropdown_item, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.custom_dropdown_item, parent, false);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(getItem(position));
        return view;
    }
}

