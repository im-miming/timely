package com.cs046_project.timely;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cs046_project.timely.adapter.TaskAdapter;
import com.cs046_project.timely.database.DbHelper;
import com.cs046_project.timely.drawable.CircularBackgroundDrawable;
import com.cs046_project.timely.drawable.CustomTimePickerDialog2;
import com.cs046_project.timely.drawable.CustomTimePickerDialog3;
import com.cs046_project.timely.model.CategoryModel;
import com.cs046_project.timely.model.TaskModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EditTaskActivity extends BaseActivity {

    private TaskAdapter taskAdapter;

    private DbHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private Spinner spinner;
    private Button startTime;
    private Button endTime;

    private FrameLayout calendarSheet;
    private ImageView leftArrowImageView, rightArrowImageView;
    private TextView monthTextView, yearTextView;
    private GridLayout dayNamesGrid, calendarGrid;
    private Calendar currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        dbHelper = new DbHelper(this);

        // Initialize other member variables
        spinner = findViewById(R.id.spinner_main);
        startTime = findViewById(R.id.setTimeButton2m);
        endTime = findViewById(R.id.setTimeButton3);

        // Retrieve categories from the database
        List<CategoryModel> categories = dbHelper.getAllCategories();

        // Extract category names from the list
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");

        for (CategoryModel category : categories) {
            categoryNames.add(category.getCategoryName());
        }
        categoryNames.add("Add New Category");

        adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, categoryNames);
        adapter.setDropDownViewResource(R.layout.custom_dropdown_item);
        spinner.setAdapter(adapter);

        // Set "No Category" as the initially selected item
        spinner.setSelection(0);

        spinner.setDropDownWidth(640); // Set the width as defined in your custom layout
        spinner.setDropDownHorizontalOffset(0);
        spinner.setDropDownVerticalOffset(145);
        spinner.setPopupBackgroundResource(R.drawable.bg_custom_dropdown);

        // Set up the onItemSelectedListener for the Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Add New Category")) {
                    showAddCategoryDialog();
                } else {
                    // Handle selection of other items in the Spinner
                    // Perform actions based on the selected category
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the situation where nothing is selected
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePickerDialog2();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePickerDialog3();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////
        /* ////////////////////////////// FOR TASK RETRIEVAL /////////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////
        // Check if the intent has extras
        Intent intent = getIntent();
        if (intent.hasExtra("taskDetails")) {
            // Retrieve the TaskModel object from the intent
            TaskModel existingTask = intent.getParcelableExtra("taskDetails");

            // Populate the UI with existing task details
            populateUI(existingTask);

            // Update the save button click listener for editing
            TextView saveButton = findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Log UI values before updating
                    Log.d("UIValues", "Task Name: " + ((EditText) findViewById(R.id.editTextInput)).getText().toString());
                    Log.d("UIValues", "Category: " + spinner.getSelectedItem().toString());
                    Log.d("UIValues", "Start Time: " + startTime.getText().toString());
                    Log.d("UIValues", "End Time: " + endTime.getText().toString());

                    // Update the existing task with the modified details
                    updateExistingTask(existingTask);

                    // Finish the current activity and go back to the previous one
                    finish();
                }
            });

        } else {
            // Handle the case when there are no extras (e.g., creating a new task)
            // Your existing logic for creating a new task can go here
        }

        ///////////////////////////////////////////////////////////////////////////////////////
        /* ////////////////////////// FOR BACK TO PREVIOUS PAGE //////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////
        // Handle the click event of the back arrow
        ImageView iconImageView = findViewById(R.id.iconImageView);
        iconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and go back to the previous one
                finish();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////
        /* ////////////////////////////// FOR CALENDAR VIEW //////////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////
        // Inflate the custom calendar layout into the calendar_sheet FrameLayout
        calendarSheet = findViewById(R.id.calendar_sheet);
        getLayoutInflater().inflate(R.layout.calendar_layout_weekly, calendarSheet, true);

        // Find and initialize views within the inflated layout
        leftArrowImageView = findViewById(R.id.leftArrowImageView);
        rightArrowImageView = findViewById(R.id.rightArrowImageView);
        monthTextView = findViewById(R.id.monthTextViewPeek);
        yearTextView = findViewById(R.id.yearTextViewPeek);
        dayNamesGrid = findViewById(R.id.dayNamesGridPeek);
        calendarGrid = findViewById(R.id.calendarGridPeek);
        // Initialize currentMonth to the current date
        currentMonth = Calendar.getInstance();

        inflatePeekCalendarLayout();

        // Set click listeners for the arrows
        ImageView leftArrow = findViewById(R.id.leftArrowImageView);
        ImageView rightArrow = findViewById(R.id.rightArrowImageView);

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decrement the current month
                currentMonth.add(Calendar.MONTH, -1);
                // Update the UI
                updateCalendarUI();
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the current month
                currentMonth.add(Calendar.MONTH, 1);
                // Update the UI
                updateCalendarUI();
            }
        });
    }

    private void showAddCategoryDialog() {
        // Your existing code for showing the add category dialog...
    }

    private void showCustomTimePickerDialog2() {
        CustomTimePickerDialog2 customTimePickerDialog2 = new CustomTimePickerDialog2(this);

        customTimePickerDialog2.show();
        customTimePickerDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Retrieve the selected time from the dialog
                String selectedTime = customTimePickerDialog2.getSelectedTime();
                if (!selectedTime.isEmpty()) {
                    // Set the selected time to both setTimeButton and setTimeButton2 text
                    startTime.setText(selectedTime);
                }
            }
        });
    }

    private void showCustomTimePickerDialog3() {
        CustomTimePickerDialog3 customTimePickerDialog3 = new CustomTimePickerDialog3(this);

        customTimePickerDialog3.show();
        customTimePickerDialog3.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Retrieve the selected time from the dialog
                String selectedTime = customTimePickerDialog3.getSelectedTime();
                if (!selectedTime.isEmpty()) {
                    // Set the selected time to both setTimeButton and setTimeButton2 text
                    endTime.setText(selectedTime);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /* ///////////////////////////// FOR TASK MODIFICATION ///////////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////
    private void populateUI(TaskModel task) {
        // Log values before populating UI
        Log.d("UIPopulate", "Task Name: " + task.getTaskName());
        Log.d("UIPopulate", "Category: " + task.getCategory());
        Log.d("UIPopulate", "Start Time: " + task.getStartTime());
        Log.d("UIPopulate", "End Time: " + task.getEndTime());
        Log.d("UIPopulate", "Duration: " + task.getDuration());

        // Populate UI elements with existing task details
        EditText taskNameEditText = findViewById(R.id.editTextInput);
        if (taskNameEditText != null) {
            taskNameEditText.setText(task.getTaskName());
        } else {
            Log.e("UIPopulate", "Task name EditText is null");
        }

        // Optional: Add null checks for adapter and spinner
        if (adapter != null && spinner != null) {
            // Set the selected category in the Spinner
            String selectedCategory = task.getCategory();
            int categoryPosition = adapter.getPosition(selectedCategory);

            if (categoryPosition != -1) {
                spinner.setSelection(categoryPosition);
            } else {
                Log.e("UIPopulate", "Category not found in adapter: " + selectedCategory);
            }
        } else {
            Log.e("UIPopulate", "Adapter or Spinner is null");
        }

        // Set the start and end time buttons (use class-level variables directly)
        startTime.setText(task.getStartTime());
        endTime.setText(task.getEndTime());

        // Set the duration in the UI (replace R.id.durationTextView with the actual ID of your duration TextView)
        TextView durationTextView = findViewById(R.id.duration);
        if (durationTextView != null) {
            durationTextView.setText(task.getDuration());
        } else {
            Log.e("UIPopulate", "Duration TextView is null");
        }
    }
    private void updateExistingTask(TaskModel existingTask) {
        // Log UI values before updating
        Log.d("UIValues", "Task ID: " + existingTask.getTaskId());
        Log.d("UIValues", "Task Name: " + ((EditText) findViewById(R.id.editTextInput)).getText().toString());
        Log.d("UIValues", "Category: " + spinner.getSelectedItem().toString());
        Log.d("UIValues", "Start Time: " + startTime.getText().toString());
        Log.d("UIValues", "End Time: " + endTime.getText().toString());

        // Update the existing task with the modified details
        existingTask.setTaskName(((EditText) findViewById(R.id.editTextInput)).getText().toString());
        existingTask.setCategory(spinner.getSelectedItem().toString());
        existingTask.setStartTime(startTime.getText().toString());
        existingTask.setEndTime(endTime.getText().toString());

        // Recalculate and update the task's duration
        calculateAndSetDuration(existingTask);

        // Update the task in the database
        boolean isUpdated = dbHelper.updateTask(existingTask);

        if (isUpdated) {
            // Display a Toast for successful update
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();

            // Run the UI update on the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Query the database to get the updated task
                    TaskModel updatedTask = dbHelper.getTaskById(existingTask.getTaskId());
                    // Populate the UI with updated task details
                    populateUI(updatedTask);

                    updateDisplayedData();
                }
            });
        } else {
            // Handle the case where the update failed
            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }


    private void calculateAndSetDuration(TaskModel task) {
        // Assuming your duration is a String and you calculate it based on start and end times
        // Adjust this calculation based on your actual logic
        String duration = calculateDuration(task.getStartTime(), task.getEndTime());
        task.setDuration(duration);
    }
    private String calculateDuration(String startTime, String endTime) {
        // Specify the time format (24-hour format in this example)
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        try {
            // Parse the start and end times into Date objects
            Date startDate = format.parse(startTime);
            Date endDate = format.parse(endTime);

            // Calculate the duration in milliseconds
            long durationMillis = endDate.getTime() - startDate.getTime();

            // If the duration is negative, add a day to it
            if (durationMillis < 0) {
                durationMillis += TimeUnit.DAYS.toMillis(1);
            }

            // Convert milliseconds to minutes and seconds
            long minutes = durationMillis / (60 * 1000);
            long seconds = (durationMillis / 1000) % 60;

            // Format the duration as a string (e.g., "1h 30m" or "45m")
            if (minutes > 0) {
                return String.format("%dh %02dm", minutes / 60, minutes % 60);
            } else {
                return String.format("%02ds", seconds);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "Error calculating duration";
        }
    }

    private void updateDisplayedData() {
        // Assuming you have a method to fetch the updated data
        List<TaskModel> updatedData = fetchData(); // Replace fetchData() with your actual method

        // Assuming you have a method to update the data in your adapter
        updateAdapterData(updatedData); // Replace updateAdapterData() with your actual method

        // Assuming you have a reference to your adapter (taskAdapter)
        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }

    // Replace this method with your actual method to fetch updated data
    private List<TaskModel> fetchData() {
        // Implement the logic to fetch the updated data from your data source (e.g., database)
        // Example: return dbHelper.getAllTasks();
        return dbHelper.getAllTasks(); // Replace this line with your actual data fetching logic
    }

    // Replace this method with your actual method to update data in the adapter
    private void updateAdapterData(List<TaskModel> updatedData) {
        // Implement the logic to update the data in your adapter
        // Example: taskAdapter.setData(updatedData);
        // Make sure your adapter has a method to update its data
        if (taskAdapter != null) {
            taskAdapter.setData(updatedData);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /* /////////////////////////////// FOR CALENDAR VIEW /////////////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////
    private void inflatePeekCalendarLayout() {
        FrameLayout calendarSheet = findViewById(R.id.calendar_sheet);
        calendarSheet.removeAllViews();

        View peekLayout = LayoutInflater.from(this).inflate(R.layout.calendar_layout_weekly, calendarSheet, false);
        calendarSheet.addView(peekLayout);

        // Get the current date
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(new Date());
        currentMonth = currentMonth.toUpperCase(); // Convert to uppercase
        String currentYear = yearFormat.format(new Date());

        // Set the current month and year in the TextViews
        TextView monthTextView = peekLayout.findViewById(R.id.monthTextViewPeek);
        TextView yearTextView = peekLayout.findViewById(R.id.yearTextViewPeek);
        monthTextView.setText(currentMonth);
        yearTextView.setText(currentYear);

        // Populate the day names
        populateDayNames();

        // Populate the dates for the current week
        populateWeekDates(peekLayout);
    }

    private void populateDayNames() {
        // Get a reference to the GridLayout
        GridLayout dayNamesGrid = findViewById(R.id.dayNamesGridPeek);

        // Define an array of day names
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        // Loop through the day names array and add TextViews to the grid layout
        for (int i = 0; i < dayNames.length; i++) {
            // Create a new TextView for each day name
            TextView dayNameTextView = new TextView(this);
            dayNameTextView.setText(dayNames[i]);
            // Set text color
            // Set text color for "Sun" and "Sat" to red, and black for others
            if (dayNames[i].equals("Sun") || dayNames[i].equals("Sat")) {
                dayNameTextView.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            } /*else {
                dayNameTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }*/
            dayNameTextView.setTextSize(14); // Set text size
            dayNameTextView.setGravity(Gravity.CENTER); // Center the text within the cell

            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
            // Set the typeface to bold
            dayNameTextView.setTypeface(boldTypeface);

            // Set layout parameters for the TextView
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 0;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.columnSpec = GridLayout.spec(i, 1f); // Make each column equally spaced
            dayNameTextView.setLayoutParams(layoutParams);

            // Add the TextView to the GridLayout
            dayNamesGrid.addView(dayNameTextView);
        }
    }
    private void populateWeekDates(View peekLayout) {
        // Get a reference to the GridLayout
        GridLayout calendarGrid = peekLayout.findViewById(R.id.calendarGridPeek);

        // Clear any existing views in the calendarGrid
        calendarGrid.removeAllViews();

        // Set the calendar to the current date
        Calendar currentWeekStart = Calendar.getInstance();

        // Find the Sunday of the current week
        while (currentWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -1);
        }

        // Check if the current month is being displayed
        boolean isCurrentMonth = currentMonth.get(Calendar.YEAR) == currentWeekStart.get(Calendar.YEAR) &&
                currentMonth.get(Calendar.MONTH) == currentWeekStart.get(Calendar.MONTH);

        // Set the calendar to the first day of the selected month
        Calendar firstWeekStart = (Calendar) currentMonth.clone();
        firstWeekStart.set(Calendar.DAY_OF_MONTH, 1);

        // Find the Sunday of the first week of the month
        while (firstWeekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            firstWeekStart.add(Calendar.DAY_OF_MONTH, -1);
        }

        // Use the appropriate calendar based on whether it's the current month
        Calendar displayCalendar = isCurrentMonth ? currentWeekStart : firstWeekStart;

        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());

        // Loop to populate the dates for the week
        for (int i = 0; i < 7; i++) {
            Date date = displayCalendar.getTime();

            // Create a new TextView for each date
            TextView dateTextView = new TextView(this);
            dateTextView.setText(dateFormat.format(date));
            dateTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Set text color
            dateTextView.setTextSize(14); // Set text size
            dateTextView.setGravity(Gravity.CENTER); // Center the text within the cell

            // Replace the deprecated method with ContextCompat.getColor()
            int backgroundColor = ContextCompat.getColor(this, R.color.blue);
            CircularBackgroundDrawable circularBackground = new CircularBackgroundDrawable(backgroundColor);

            if (isCurrentDate(date)) {
                dateTextView.setBackground(circularBackground);
                dateTextView.setTextColor(ContextCompat.getColor(this, android.R.color.white)); // Set text color to white
            } else {
                // If it's not the current date, you can set a different text color or no background as needed.
                dateTextView.setBackground(null);
                dateTextView.setTextColor(ContextCompat.getColor(this, R.color.black)); // Set text color to your default color
            }

            // Set layout parameters for the TextView
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 0;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.columnSpec = GridLayout.spec(i, 1f); // Make each column equally spaced
            dateTextView.setLayoutParams(layoutParams);

            // Add the TextView to the GridLayout
            calendarGrid.addView(dateTextView);

            // Move to the next day (increment by one day)
            displayCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }



    // Check if a date is the current date
    private boolean isCurrentDate(Date date) {
        Calendar currentDate = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return currentDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && currentDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void updateCalendarUI() {
        // Update the month and year in the TextViews
        TextView monthTextView = findViewById(R.id.monthTextViewPeek);
        TextView yearTextView = findViewById(R.id.yearTextViewPeek);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        monthTextView.setText(monthFormat.format(currentMonth.getTime()).toUpperCase());
        yearTextView.setText(yearFormat.format(currentMonth.getTime()));

        // Populate the dates for the current month
        populateWeekDates(findViewById(R.id.calendar_sheet));
    }
}
