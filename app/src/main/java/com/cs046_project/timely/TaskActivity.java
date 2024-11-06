package com.cs046_project.timely;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

public class TaskActivity extends BaseActivity {

    private Button setTimeButton;

    private Spinner spinner;

    private ArrayAdapter<String> adapter;
    private DbHelper dbHelper;
    private ArrayList<String> arrayList;

    private Button startTime;
    private Button endTime;

    private FrameLayout calendarSheet;
    private ImageView leftArrowImageView, rightArrowImageView;
    private TextView monthTextView, yearTextView;
    private GridLayout dayNamesGrid, calendarGrid;
    private Calendar currentMonth; // Declare as a class member


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        dbHelper = new DbHelper(this);

        spinner = findViewById(R.id.spinner_main);
        setTimeButton = findViewById(R.id.setTimeButton);
        startTime = findViewById(R.id.setTimeButton2m);
        endTime = findViewById(R.id.setTimeButton3);

        // Retrieve categories from the database
        List<CategoryModel> categories = dbHelper.getAllCategories();

        // Extract category names from the list
        List<String> categoryNames = new ArrayList<>();

        // Add "No category" to the list
        categoryNames.add("No Category");

        for (CategoryModel category : categories) {
            categoryNames.add(category.getCategoryName());
        }

        // Add "Add New Category" to the end
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
                // Show the custom time picker dialog when the "Set Time" button is clicked.
                showCustomTimePickerDialog2();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the custom time picker dialog when the "Set Time" button is clicked.
                showCustomTimePickerDialog3();
            }
        });

        ImageView backArrow = findViewById(R.id.iconImageView);
        backArrow.setOnClickListener(view -> {
            // Navigate to the MainActivity
            Intent intent = new Intent(TaskActivity.this, MainActivity.class);
            startActivity(intent);
        });

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

        ///////////////////////////////////////////////////////////////////////////////////////
        /* /////////////////////////////// FOR SAVING TASK /////////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////
        TextView saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the task name from the EditText
                EditText taskNameEditText = findViewById(R.id.editTextInput);
                String taskName = taskNameEditText.getText().toString();

                // Get the selected category from the Spinner
                String selectedCategory = spinner.getSelectedItem().toString();

                // Get the current date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());

                // Get the start time and end time from the buttons
                String startTime = TaskActivity.this.startTime.getText().toString();
                String endTime = TaskActivity.this.endTime.getText().toString();

                // Parse the start and end times into Date objects
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date startDate = null;
                Date endDate = null;
                try {
                    startDate = timeFormat.parse(startTime);
                    endDate = timeFormat.parse(endTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Calculate the duration in milliseconds
                long durationMillis = endDate.getTime() - startDate.getTime();

                // Convert the duration from milliseconds to hours and minutes
                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % TimeUnit.HOURS.toMinutes(1);

                // Format the duration as a string
                String duration = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);

                // Create a TaskModel object with the gathered data
                TaskModel task = new TaskModel(
                        null, // taskId can be set to null or generate a unique ID in your dbHelper
                        taskName,
                        selectedCategory,
                        currentDate,
                        duration,
                        startTime,
                        endTime
                );

                // Save the task to the database
                long result = dbHelper.insertTask(task);

                if (result != -1) {
                    // Task saved successfully
                    Toast.makeText(TaskActivity.this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                    // Optionally, you can navigate back to the main activity
                    Intent intent = new Intent(TaskActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Error saving task
                    Toast.makeText(TaskActivity.this, "Error saving task!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showAddCategoryDialog() {
        View popupView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        EditText categoryNameEditText = popupView.findViewById(R.id.categoryNameEditText);
        TextView cancelTextView = popupView.findViewById(R.id.cancelTextView);
        TextView saveTextView = popupView.findViewById(R.id.saveTextView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        cancelTextView.setOnClickListener(v -> {
            dialog.dismiss();
        });

        saveTextView.setOnClickListener(v -> {
            String newCategoryName = categoryNameEditText.getText().toString();
            if (!newCategoryName.isEmpty()) {
                // Add the new category to the adapter's dataset
                adapter.insert(newCategoryName, adapter.getCount() - 1); // Insert at second to last position

                // Notify the adapter that the dataset has changed
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                // Show a message indicating no category was entered
                Toast.makeText(TaskActivity.this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

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