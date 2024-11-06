package com.cs046_project.timely;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cs046_project.timely.adapter.TaskAdapter;
import com.cs046_project.timely.controller.SwipeController;
import com.cs046_project.timely.database.DbHelper;
import com.cs046_project.timely.drawable.CircularBackgroundDrawable;
import com.cs046_project.timely.listener.SwipeGestureListener;
import com.cs046_project.timely.listener.TaskClickListener;
import com.cs046_project.timely.model.CategoryModel;
import com.cs046_project.timely.model.TaskModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements TaskClickListener {

    // database thingy related
    // Declare dbHelper as a class variable
    private DbHelper dbHelper;
    private NestedScrollView categoryLayout;

    FloatingActionButton fab;

    // for calendar view
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private FrameLayout bottomSheet; // Declare bottomSheet as a class member
    private View peekLayout; // Reference to calendar_layout_peek
    private View fullLayout; // Reference to calendar_layout_full
    private Calendar currentMonthCalendar;
    private Calendar currentMonth;
    private GridLayout calendarGridFull;
    private TextView monthTextViewFull;
    private TextView yearTextViewFull;

    // for top navigation
    private View scheduleOption;
    private View overviewOption;
    private View widgetOption;

    // for category scroll view
    private TextView currentOption;
    private TextView allOption;
    private FrameLayout contentFrameLayout;
    private View categoryCurrentContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DbHelper class
        dbHelper = new DbHelper(this);
        // Initialize categoryLayout in onCreate or wherever appropriate
        categoryLayout = findViewById(R.id.categoryScrollBar);

        // for top navigation
        scheduleOption = findViewById(R.id.text_schedule);
        overviewOption = findViewById(R.id.text_overview);
        widgetOption = findViewById(R.id.text_widget);

        // for floating action button
        FloatingActionButton fabButton = findViewById(R.id.fabButton);

        // for calendar view
        bottomSheet = findViewById(R.id.calendar_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // for category scroll view
        // Find the HorizontalScrollView and LinearLayout for the category bar
        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scroll_view);
        LinearLayout categoryLayout = findViewById(R.id.category_layout);
        // Find the "Current" and "All" options
        currentOption = findViewById(R.id.currentOption);
        allOption = findViewById(R.id.allOption);
        contentFrameLayout = findViewById(R.id.contentFrameLayout);
        boolean isCurrentActive = true; // Initially, "Current" is active

        // Find the "All" category content layout and its subviews
        View categoryAllContent = getLayoutInflater().inflate(R.layout.category_all_content, null);
        TextView toDoButton = categoryAllContent.findViewById(R.id.toDoButton);
        TextView freeTimeButton = categoryAllContent.findViewById(R.id.freeTimeButton);
        TextView historyButton = categoryAllContent.findViewById(R.id.historyButton);

        // Set the "Schedule" option as active
        setScheduleActive();

        scheduleOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Schedule" is already active, so no need to start it again.
                // You can leave this block empty or add any specific behavior you want.
            }
        });

        overviewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the "Overview" activity
                Intent overviewIntent = new Intent(MainActivity.this, OverviewActivity.class);
                startActivity(overviewIntent);
                overridePendingTransition(0, 0);
            }
        });

        widgetOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the "Widget" activity
                Intent widgetIntent = new Intent(MainActivity.this, WidgetActivity.class);
                startActivity(widgetIntent);
                overridePendingTransition(0, 0);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////
        /* ////////////////////////////// FOR FAB BUTTON VIEW ////////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////

        fab = findViewById(R.id.fabButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to the next activity
                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                startActivity(intent);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////
        /* /////////////////////////// FOR CATEGORY SCROLL VIEW //////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////

        // Create a layout params object with the same settings as other categories
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int topBottomPaddingPx = getResources().getDimensionPixelSize(R.dimen.category_top_bottom_padding);
        int leftRightPaddingPx = getResources().getDimensionPixelSize(R.dimen.category_left_right_padding);

        // Set padding
        currentOption.setPadding(leftRightPaddingPx, topBottomPaddingPx, leftRightPaddingPx, topBottomPaddingPx);
        allOption.setPadding(leftRightPaddingPx, topBottomPaddingPx, leftRightPaddingPx, topBottomPaddingPx);

        categoryCurrentContent = getLayoutInflater().inflate(R.layout.category_current_content, null);
        View defaultAllContentLayout = getLayoutInflater().inflate(R.layout.to_do_layout, null);
        contentFrameLayout.addView(categoryCurrentContent);

        // Set the custom border drawable as the background
        currentOption.setBackgroundResource(R.drawable.bg_category_current_active);
        allOption.setBackgroundResource(R.drawable.bg_category_all_inactive);

        // Set margin to create spacing (except for the last item)
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.category_margin));
        currentOption.setLayoutParams(params);
        allOption.setLayoutParams(params);

        // Call the setupCategoryBar method to dynamically set up the category bar
        setupCategoryBar();

        currentOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click action for "Current" option
                // For example, switch to the current category view
                // Handle the click action for the "All" option

                // Update the background of "Current" to indicate it's active
                currentOption.setBackgroundResource(R.drawable.bg_category_current_active);
                allOption.setBackgroundResource(R.drawable.bg_category_all_inactive);


                // Here, you can also display the content for the "Current" option.
                // You might want to replace the contentFrameLayout content accordingly.

                // For example, display the "Current" category content by default
                categoryCurrentContent = getLayoutInflater().inflate(R.layout.category_current_content, null);

                // Clear the existing content in contentFrameLayout
                contentFrameLayout.removeAllViews();

                // Set the "Current" category content as the contentFrameLayout's content
                contentFrameLayout.addView(categoryCurrentContent);
            }
        });

        allOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the "All" category content layout
                View categoryAllContent = getLayoutInflater().inflate(R.layout.category_all_content, null);

                // Clear the existing content in contentFrameLayout
                contentFrameLayout.removeAllViews();

                // Set the "All" category content as the contentFrameLayout's content
                contentFrameLayout.addView(categoryAllContent);

                // Update the background of "All" to indicate it's active
                allOption.setBackgroundResource(R.drawable.bg_category_all_active);
                currentOption.setBackgroundResource(R.drawable.bg_category_current_inactive);

                // Set the initial content to "To do" when "All" is clicked
                View toDoLayout = getLayoutInflater().inflate(R.layout.to_do_layout, null);

                // Replace the content inside the categoryContentFrame with the "To do" layout
                FrameLayout categoryContentFrame = categoryAllContent.findViewById(R.id.AllContentFrame);
                categoryContentFrame.removeAllViews();
                categoryContentFrame.addView(toDoLayout);

                ///////////////////////////////////////////////////////////////////////////////////////
                /* /////////////////////////// FOR SCROLLABLE TASK CARD //////////////////////////// */
                ///////////////////////////////////////////////////////////////////////////////////////
                // Set up RecyclerView, adapter, and ItemTouchHelper for swipe gestures
                RecyclerView recyclerView = toDoLayout.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                // Get all tasks from the database
                DbHelper dbHelper = new DbHelper(MainActivity.this);

                List<TaskModel> taskList = dbHelper.getAllTasks();

                TaskAdapter adapter = new TaskAdapter(taskList, recyclerView, dbHelper, MainActivity.this);
                recyclerView.setAdapter(adapter);

                SwipeController swipeController = new SwipeController(recyclerView, taskList, adapter, dbHelper);
                ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT
                ) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false; // Dummy implementation, as we don't want to handle item movement
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        // Leave this method empty to prevent the item from being removed

                        // Reset the translation of the swiped item
                        viewHolder.itemView.setTranslationX(0);

                        // If you're using an ArrayAdapter or similar, do not remove the item from the dataset here
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);
                        // Reset the translation of the swiped item
                        viewHolder.itemView.setTranslationX(0);
                    }
                };

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                // Set the background of "To do" to indicate it's active
                TextView toDoButton = categoryAllContent.findViewById(R.id.toDoButton);
                toDoButton.setBackgroundResource(R.drawable.bg_all_options_active);

                // Set click listeners for "Free time" and "History" options
                TextView freeTimeButton = categoryAllContent.findViewById(R.id.freeTimeButton);
                TextView historyButton = categoryAllContent.findViewById(R.id.historyButton);

                freeTimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle "Free time" button click
                        View freeTimeLayout = getLayoutInflater().inflate(R.layout.free_time_layout, null);

                        // Replace the content inside the categoryContentFrame with the "Free time" layout
                        categoryContentFrame.removeAllViews();
                        categoryContentFrame.addView(freeTimeLayout);

                        // Update the background to indicate "Free time" is active
                        freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_active);

                        // Reset the backgrounds for other options
                        toDoButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                        historyButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                    }
                });

                historyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle "History" button click
                        View historyLayout = getLayoutInflater().inflate(R.layout.history_layout, null);

                        // Replace the content inside the categoryContentFrame with the "History" layout
                        categoryContentFrame.removeAllViews();
                        categoryContentFrame.addView(historyLayout);

                        // Update the background to indicate "History" is active
                        historyButton.setBackgroundResource(R.drawable.bg_all_options_active);

                        // Reset the backgrounds for other options
                        toDoButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                        freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                    }
                });

                // Set the "To do" button click listener within the "All" option
                toDoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle "To do" button click
                        categoryContentFrame.removeAllViews();
                        categoryContentFrame.addView(toDoLayout);

                        // Update the background to indicate "To do" is active
                        toDoButton.setBackgroundResource(R.drawable.bg_all_options_active);

                        // Reset the backgrounds for other options
                        freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                        historyButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                    }
                });
            }
        });

        ImageButton moreVertIcon = findViewById(R.id.manage_categories_more_vert);
        moreVertIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showManageCategoriesPopup(view);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////
        /* /////////////////////////////// FOR CALENDAR VIEW /////////////////////////////// */
        ///////////////////////////////////////////////////////////////////////////////////////
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Handle state changes if needed
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Handle slide offset if needed
            }
        });

        // Initialize references to layouts
        peekLayout = LayoutInflater.from(this).inflate(R.layout.calendar_layout_peek, bottomSheet, false);
        fullLayout = LayoutInflater.from(this).inflate(R.layout.calendar_layout_full, bottomSheet, false);

        // Now, calculate the height based on a single row and set it
        //calculateAndSetPeekLayoutHeight();
        // Initialize currentMonth to the current date
        currentMonth = Calendar.getInstance();

        // Initially inflate the "peek" calendar layout
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

        // Set a touch listener for the calendar sheet to enable swiping between layouts
        bottomSheet.setOnTouchListener(new SwipeGestureListener(this) {
            @Override
            public void onSwipeUp() {
                // Swipe up, switch to "full" layout
                inflateFullCalendarLayout();
                fabButton.setVisibility(View.GONE);
            }
            @Override
            public void onSwipeDown() {
                // Swipe down, switch to "peek" layout
                inflatePeekCalendarLayout();
                fabButton.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onCardClick(int position) {
        // Implement your card click action here
        Log.d("MainActivity", "Card clicked for position: " + position);
    }

    @Override
    public void onEditClick(int position) {
        // Implement your edit action here
        Log.d("MainActivity", "Edit clicked for position: " + position);
    }

    @Override
    public void onDeleteClick(int position) {
        // Implement your delete action here
        Log.d("MainActivity", "Delete clicked for position: " + position);
    }

    private void setScheduleActive() {
        scheduleOption.setBackgroundResource(R.drawable.bg_selected_nav_option);
        ((TextView) scheduleOption).setTextColor(Color.WHITE);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /* ////////////////////// FOR SCROLLABLE TASK CARD: TASK ITEM ////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////
    // Inside MainActivity
    public void showDeleteConfirmationDialog(TaskModel task) {
        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_confirm_delete, null);

        // Create a Dialog with the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Find the TextViews for cancel and confirm actions
        TextView cancelTextView = dialogView.findViewById(R.id.cancelTextView);
        TextView confirmTextView = dialogView.findViewById(R.id.confirmTextView);

        // Set click listeners for cancel and confirm actions
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog if cancel is clicked
                dialog.dismiss();
            }
        });

        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your delete logic here
                dbHelper.deleteTask(task.getTaskId());

                // Dismiss the dialog after handling the confirmation
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    /* /////////////////////////// FOR CATEGORY SCROLL VIEW //////////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////
    private void showManageCategoriesPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.dialog_manage_categories, null);

        PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);

        // Calculate the position to show the popup just above the anchorView
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int upwardShift = 150;
        int x = location[0] + (anchorView.getWidth() / 2) - (popupView.getWidth() / 2);
        int y = location[1] - popupView.getHeight() - upwardShift;

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);

        // Add an OnClickListener to the popupView
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click action, e.g., open the "Manage Categories" page
                openManageCategoriesPage();
                // Dismiss the popup if needed
                popupWindow.dismiss();
            }
        });
    }
    private void openManageCategoriesPage() {
        Intent intent = new Intent(this, ManageCategoriesActivity.class);
        startActivity(intent);
    }

    // New method to set up the category bar dynamically
    private void setupCategoryBar() {
        // Get the list of categories from the database
        List<CategoryModel> categoryList = dbHelper.getAllCategories();

        // Create a LinearLayout to hold the dynamically created TextView items
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.HORIZONTAL); // or VERTICAL, depending on your layout

        // Add category items dynamically to the containerLayout
        for (CategoryModel category : categoryList) {
            TextView categoryItem = new TextView(this);
            categoryItem.setText(category.getCategoryName());

            // Set different padding values for top and bottom and keep the same for left and right
            int topBottomPaddingPx = getResources().getDimensionPixelSize(R.dimen.category_top_bottom_padding);
            int leftRightPaddingPx = getResources().getDimensionPixelSize(R.dimen.category_left_right_padding);

            categoryItem.setPadding(leftRightPaddingPx, topBottomPaddingPx, leftRightPaddingPx, topBottomPaddingPx);

            // Set the custom border drawable as the background
            int inactiveBackgroundResource = getInactiveCategoryBackgroundResource(category.getCategoryColor());
            categoryItem.setBackgroundResource(inactiveBackgroundResource);

            // Set margin to create spacing (except for the last item)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            // Add margin to the right of the category item
            params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.category_margin));

            categoryItem.setLayoutParams(params);

            // Set a click listener for the category item
            categoryItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the click action for the category item
                    // Get the CategoryModel associated with the clicked item
                    CategoryModel clickedCategory = categoryList.get(categoryList.indexOf(category));

                    // Call the method to set up category content
                    setupCategoryContent(clickedCategory);
                    // Handle the click action for the category item
                    // Call the method to set up category content
                    //setupCategoryContent();
                    allOption.setBackgroundResource(R.drawable.bg_category_all_inactive);
                    currentOption.setBackgroundResource(R.drawable.bg_category_current_inactive);

                    // Reset the backgrounds for all category items to indicate they are inactive
                    for (int i = 0; i < containerLayout.getChildCount(); i++) {
                        View child = containerLayout.getChildAt(i);
                        CategoryModel otherCategory = categoryList.get(i);
                        int otherInactiveBackgroundResource = getInactiveCategoryBackgroundResource(otherCategory.getCategoryColor());
                        child.setBackgroundResource(otherInactiveBackgroundResource);
                        Log.d("CategoryClick", "Setting inactive background for " + otherCategory.getCategoryName());
                    }

                    // Set the background of the clicked category item to indicate it's active
                    int activeBackgroundResource = getCategoryBackgroundResource(true, category.getCategoryColor());
                    categoryItem.setBackgroundResource(activeBackgroundResource);
                    Log.d("CategoryClick", "Setting active background for " + category.getCategoryName());
                    Log.d("CategoryClick", "Category name: " + category.getCategoryName());
                    Log.d("CategoryClick", "Color resource: " + activeBackgroundResource);
                    Log.d("CategoryClick", "Color value: " + category.getCategoryColor());
                }
            });

            // Add the dynamically created category item to the containerLayout
            containerLayout.addView(categoryItem);
        }

        // Add the containerLayout to the NestedScrollView
        LinearLayout categoryLayout = findViewById(R.id.category_layout);
        categoryLayout.addView(containerLayout);
    }

    private int getCategoryBackgroundResource(boolean isActive, int categoryColor) {
        int colorResource = R.drawable.category_border;

        // Get the color resource ID from the color value
        int colorResourceId = getColorResourceId(categoryColor);

        Log.d("CategoryBackground", "Color Resource ID: " + colorResourceId);

        if (isActive) {
            colorResource = getActiveCategoryBackgroundResource(colorResourceId);
        } else {
            colorResource = getInactiveCategoryBackgroundResource(colorResourceId);
        }

        Log.d("CategoryBackground", "Final Background Resource: " + colorResource);

        return colorResource;
    }


    // Helper method to get the color resource ID from the color value
    private int getColorResourceId(int colorValue) {
        Resources res = getResources();

        int[] colorResources = {
                R.color.colorBlue,
                R.color.colorYellow,
                R.color.colorRed,
                R.color.colorGreen,
                R.color.colorOrange,
                R.color.colorPink,
                R.color.colorViolet
                // Add more color resources as needed
        };

        for (int colorRes : colorResources) {
            int colorInt = res.getColor(colorRes);
            if (colorInt == colorValue) {
                return colorRes;
            }
        }

        // Return a default color resource if not found
        return R.color.colorBlue;
    }

    private int getActiveCategoryBackgroundResource(int colorResourceId) {
        if (colorResourceId == R.color.colorBlue) {
            return R.drawable.bg_category_active_blue;
        } else if (colorResourceId == R.color.colorYellow) {
            return R.drawable.bg_category_active_yellow;
        } else if (colorResourceId == R.color.colorRed) {
            return R.drawable.bg_category_active_red;
        } else if (colorResourceId == R.color.colorGreen) {
            return R.drawable.bg_category_active_green;
        } else if (colorResourceId == R.color.colorOrange) {
            return R.drawable.bg_category_active_orange;
        } else if (colorResourceId == R.color.colorPink) {
            return R.drawable.bg_category_active_pink;
        } else if (colorResourceId == R.color.colorViolet) {
            return R.drawable.bg_category_active_violet;
        } else {
            return R.drawable.bg_category_active_blue;
        }
    }

    private int getInactiveCategoryBackgroundResource(int categoryColor) {
        Resources res = getResources();

        int[] colorResources = {
                res.getColor(R.color.colorBlue),
                res.getColor(R.color.colorYellow),
                res.getColor(R.color.colorRed),
                res.getColor(R.color.colorGreen),
                res.getColor(R.color.colorOrange),
                res.getColor(R.color.colorPink),
                res.getColor(R.color.colorViolet)
                // Add more color resources as needed
        };

        for (int i = 0; i < colorResources.length; i++) {
            if (colorResources[i] == categoryColor) {
                switch (i) {
                    case 0:
                        return R.drawable.bg_category_inactive_blue;
                    case 1:
                        return R.drawable.bg_category_inactive_yellow;
                    case 2:
                        return R.drawable.bg_category_inactive_red;
                    case 3:
                        return R.drawable.bg_category_inactive_green;
                    case 4:
                        return R.drawable.bg_category_inactive_orange;
                    case 5:
                        return R.drawable.bg_category_inactive_pink;
                    case 6:
                        return R.drawable.bg_category_inactive_violet;
                    default:
                        return R.drawable.bg_category_inactive_blue; // Default
                }
            }
        }

        // Return a default background resource if the color is not found
        return R.drawable.bg_category_inactive_blue;
    }


    private void setupCategoryContent(CategoryModel category) {
        // Inflate the "All" category content layout
        View categoryAllContent = getLayoutInflater().inflate(R.layout.category_all_content, null);

        // Clear the existing content in contentFrameLayout
        contentFrameLayout.removeAllViews();

        // Set the "All" category content as the contentFrameLayout's content
        contentFrameLayout.addView(categoryAllContent);

        // Update the background of "All" to indicate it's active (if needed)
        // ...

        // Set the initial content to "To do" when "All" is clicked
        View toDoLayout = getLayoutInflater().inflate(R.layout.to_do_layout, null);

        // Replace the content inside the categoryContentFrame with the "To do" layout
        FrameLayout categoryContentFrame = categoryAllContent.findViewById(R.id.AllContentFrame);
        categoryContentFrame.removeAllViews();
        categoryContentFrame.addView(toDoLayout);

        // Set up RecyclerView, adapter, and ItemTouchHelper for swipe gestures
        RecyclerView recyclerView = toDoLayout.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Get all tasks for the selected category from the database
        DbHelper dbHelper = new DbHelper(MainActivity.this);
        List<TaskModel> taskList = dbHelper.getAllTasks(category.getCategoryName());

        TaskAdapter adapter = new TaskAdapter(taskList, recyclerView, dbHelper, MainActivity.this);
        recyclerView.setAdapter(adapter);

        SwipeController swipeController = new SwipeController(recyclerView, taskList, adapter, dbHelper);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Dummy implementation, as we don't want to handle item movement
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Leave this method empty to prevent the item from being removed

                // Reset the translation of the swiped item
                viewHolder.itemView.setTranslationX(0);

                // If you're using an ArrayAdapter or similar, do not remove the item from the dataset here
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Reset the translation of the swiped item
                viewHolder.itemView.setTranslationX(0);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Set the background of "To do" to indicate it's active
        TextView toDoButton = categoryAllContent.findViewById(R.id.toDoButton);
        toDoButton.setBackgroundResource(R.drawable.bg_all_options_active);

        // Set click listeners for "Free time" and "History" options
        TextView freeTimeButton = categoryAllContent.findViewById(R.id.freeTimeButton);
        TextView historyButton = categoryAllContent.findViewById(R.id.historyButton);

        freeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Free time" button click
                View freeTimeLayout = getLayoutInflater().inflate(R.layout.free_time_layout, null);

                // Replace the content inside the categoryContentFrame with the "Free time" layout
                categoryContentFrame.removeAllViews();
                categoryContentFrame.addView(freeTimeLayout);

                // Update the background to indicate "Free time" is active
                freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_active);

                // Reset the backgrounds for other options
                toDoButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                historyButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "History" button click
                View historyLayout = getLayoutInflater().inflate(R.layout.history_layout, null);

                // Replace the content inside the categoryContentFrame with the "History" layout
                categoryContentFrame.removeAllViews();
                categoryContentFrame.addView(historyLayout);

                // Update the background to indicate "History" is active
                historyButton.setBackgroundResource(R.drawable.bg_all_options_active);

                // Reset the backgrounds for other options
                toDoButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
            }
        });

        // Set the "To do" button click listener within the "All" option
        toDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "To do" button click
                categoryContentFrame.removeAllViews();
                categoryContentFrame.addView(toDoLayout);

                // Update the background to indicate "To do" is active
                toDoButton.setBackgroundResource(R.drawable.bg_all_options_active);

                // Reset the backgrounds for other options
                freeTimeButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
                historyButton.setBackgroundResource(R.drawable.bg_all_options_inactive);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /* /////////////////////////// FOR ALL CATEGORY CONTENT //////////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////////////////
    /* /////////////////////////////// FOR CALENDAR VIEW /////////////////////////////// */
    ///////////////////////////////////////////////////////////////////////////////////////
    private void inflatePeekCalendarLayout() {
        FrameLayout calendarSheet = findViewById(R.id.calendar_sheet);
        calendarSheet.removeAllViews();

        View peekLayout = LayoutInflater.from(this).inflate(R.layout.calendar_layout_peek, calendarSheet, false);
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

        // Set the bottom sheet state to collapsed (peek)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

    private void inflateFullCalendarLayout() {
        // Initialize the currentMonthCalendar at the beginning of the method
        currentMonthCalendar = Calendar.getInstance();

        FrameLayout calendarSheet = findViewById(R.id.calendar_sheet);
        calendarSheet.removeAllViews();

        fullLayout = LayoutInflater.from(this).inflate(R.layout.calendar_layout_full, calendarSheet, false);
        calendarSheet.addView(fullLayout);

        // Show the "calendar_layout_full"
        fullLayout.setVisibility(View.VISIBLE);

        // Get references to the TextViews and GridViews in "calendar_layout_full.xml"
        monthTextViewFull = fullLayout.findViewById(R.id.monthTextViewFull);
        yearTextViewFull = fullLayout.findViewById(R.id.yearTextViewFull);
        GridLayout dayNamesGridFull = fullLayout.findViewById(R.id.dayNamesGridFull);
        calendarGridFull = fullLayout.findViewById(R.id.calendarGridFull); // Correct assignment here

        // Optionally, populate the month and year TextViews with the current date
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(new Date());
        currentMonth = currentMonth.toUpperCase(); // Convert to uppercase
        String currentYear = yearFormat.format(new Date());
        monthTextViewFull.setText(currentMonth);
        yearTextViewFull.setText(currentYear);

        // Optionally, populate the day names in the dayNamesGridFull
        populateDayNames(dayNamesGridFull);

        // Optionally, populate the dates for the current month in calendarGridFull
        populateMonthDates(calendarGridFull, currentMonthCalendar);

        // Set the bottom sheet state to expanded (full)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Get references to the left and right arrow ImageViews
        ImageView leftArrowImageView = fullLayout.findViewById(R.id.leftArrowImageView);
        ImageView rightArrowImageView = fullLayout.findViewById(R.id.rightArrowImageFull);

        leftArrowImageView.setOnClickListener(v -> {
            // Handle left arrow click (navigate to previous month in the background)
            navigateToPreviousMonth();
        });

        rightArrowImageView.setOnClickListener(v -> {
            // Handle right arrow click (navigate to next month in the background)
            navigateToNextMonth();
        });
    }

    // Method to populate day names for the "calendar_layout_full.xml"
    private void populateDayNames(GridLayout dayNamesGrid) {
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


    private void populateMonthDates(GridLayout calendarGrid, Calendar monthCalendar) {
        // Clear existing views in the GridLayout
        calendarGrid.removeAllViews();

        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());

        // Define the desired margin (in pixels)
        int marginPx = getResources().getDimensionPixelSize(R.dimen.date_margin);

        // Set the calendar to the specified month
        Calendar calendar = (Calendar) monthCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the month

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Determine the number of rows needed based on the number of days in the month
        int numRows = (int) Math.ceil((float) daysInMonth / 7f);

        for (int row = 0; row < numRows; row++) {
            for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                Date date = calendar.getTime();

                // Create a new TextView for each date
                TextView dateTextView = new TextView(this);
                dateTextView.setText(dateFormat.format(date));
                dateTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Set text color
                dateTextView.setTextSize(14); // Set text size
                dateTextView.setGravity(Gravity.CENTER); // Center the text within the cell

                // Set layout parameters for the TextView
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.width = 0;
                layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.columnSpec = GridLayout.spec(dayOfWeek - 1, 1f);
                layoutParams.rowSpec = GridLayout.spec(row);
                layoutParams.topMargin = marginPx; // Set top margin
                dateTextView.setLayoutParams(layoutParams);

                // Add the TextView to the GridLayout
                calendarGrid.addView(dateTextView);

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

                // Move to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);

                // If we've reached the end of the month, break the loop
                if (day == daysInMonth) {
                    break;
                }
            }
        }
    }

    private void navigateToPreviousMonth() {
        // Start a background thread for the navigation operation
        new Thread(() -> {
            // Perform the necessary calculations and navigation logic here
            Calendar previousMonth = getPreviousMonth(currentMonthCalendar);

            // Update the UI on the main thread
            runOnUiThread(() -> {
                // Clear existing dates in calendarGridFull
                calendarGridFull.removeAllViews();

                // Repopulate the dates for the new month
                populateMonthDates(calendarGridFull, previousMonth);

                // Update the monthTextViewFull and yearTextViewFull with the new month and year
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                String currentMonth = monthFormat.format(previousMonth.getTime());
                currentMonth = currentMonth.toUpperCase(); // Convert to uppercase
                String currentYear = yearFormat.format(previousMonth.getTime());
                monthTextViewFull.setText(currentMonth);
                yearTextViewFull.setText(currentYear);

                // Update currentMonthCalendar
                currentMonthCalendar = previousMonth;
            });
        }).start();
    }

    // Helper method to get the previous month's Calendar instance
    private Calendar getPreviousMonth(Calendar currentMonth) {
        Calendar previousMonth = (Calendar) currentMonth.clone();
        previousMonth.add(Calendar.MONTH, -1);
        return previousMonth;
    }

    private void navigateToNextMonth() {
        // Start a background thread for the navigation operation
        new Thread(() -> {
            // Perform the necessary calculations and navigation logic here
            Calendar nextMonth = getNextMonth(currentMonthCalendar);

            // Update the UI on the main thread
            runOnUiThread(() -> {
                // Clear existing dates in calendarGridFull
                calendarGridFull.removeAllViews();

                // Repopulate the dates for the new month
                populateMonthDates(calendarGridFull, nextMonth);

                // Update the monthTextViewFull and yearTextViewFull with the new month and year
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                String currentMonth = monthFormat.format(nextMonth.getTime());
                currentMonth = currentMonth.toUpperCase(); // Convert to uppercase
                String currentYear = yearFormat.format(nextMonth.getTime());
                monthTextViewFull.setText(currentMonth);
                yearTextViewFull.setText(currentYear);

                // Update currentMonthCalendar
                currentMonthCalendar = nextMonth;
            });
        }).start();
    }

    // Helper method to get the next month's Calendar instance
    private Calendar getNextMonth(Calendar currentMonth) {
        Calendar nextMonth = (Calendar) currentMonth.clone();
        nextMonth.add(Calendar.MONTH, 1);
        return nextMonth;
    }
}