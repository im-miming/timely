package com.cs046_project.timely.drawable;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.cs046_project.timely.R;

public class CustomTimePickerDialog2 extends Dialog {

    private Button setTimeButton2;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private NumberPicker amPmPicker;

    private String selectedTime = ""; // Initialize selectedTime as an empty string

    public CustomTimePickerDialog2(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_time_picker2);

        setTimeButton2 = findViewById(R.id.setTimeButton2);
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        amPmPicker = findViewById(R.id.amPmPicker);

        // Set values for hour picker (0-12)
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);

        // Set values for minute picker (0-59)
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        // Set values for AM/PM picker
        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(1);
        amPmPicker.setDisplayedValues(new String[]{"AM", "PM"});

        setTimeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedHour = hourPicker.getValue();
                int selectedMinute = minutePicker.getValue();
                String selectedAmPm = amPmPicker.getValue() == 0 ? "AM" : "PM";

                selectedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, selectedAmPm);

                // Set the selected time as the text of the "Set Time" button
                setTimeButton2.setText(selectedTime);

                // Close the dialog
                dismiss();
            }
        });
    }

    // Add a method to get the selected time
    public String getSelectedTime() {
        return selectedTime;
    }
}
