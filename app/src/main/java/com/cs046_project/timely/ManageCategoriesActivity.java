package com.cs046_project.timely;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs046_project.timely.adapter.CategoryAdapter;
import com.cs046_project.timely.database.DbContract;
import com.cs046_project.timely.database.DbHelper;
import com.cs046_project.timely.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends BaseActivity {
    //database thingy
    private DbHelper dbHelper;
    private CategoryAdapter categoryAdapter;
    private List<CategoryModel> categoryList;
    private RecyclerView categoryRecyclerView;
    private int selectedColor = -1;
    private LinearLayout colorOptionsLayout;
    private View lastSelectedColorView; // Changed from ImageView
    private int lastSelectedColor = -1;
    private int[] originalColors; // Store the original color values
    private int[] selectorDrawableIds = {
            R.drawable.bg_selected_color_violet,
            R.drawable.bg_selected_color_yellow,
            R.drawable.bg_selected_color_green,
            R.drawable.bg_selected_color_red,
            R.drawable.bg_selected_color_blue,
            R.drawable.bg_selected_color_orange,
            R.drawable.bg_selected_color_pink
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        //database thingy
        dbHelper = new DbHelper(this);

        // Initialize originalColors here
        originalColors = new int[] {
                ContextCompat.getColor(this, R.color.colorViolet),
                ContextCompat.getColor(this, R.color.colorYellow),
                ContextCompat.getColor(this, R.color.colorGreen),
                ContextCompat.getColor(this, R.color.colorRed),
                ContextCompat.getColor(this, R.color.colorBlue),
                ContextCompat.getColor(this, R.color.colorOrange),
                ContextCompat.getColor(this, R.color.colorPink)
        };

        // Get references to UI elements
        ImageView leftArrowIcon = findViewById(R.id.leftArrowIcon);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);

        // Initialize your list of categories
        categoryList = new ArrayList<>();

        // Create a CategoryAdapter with the list of categories
        categoryAdapter = new CategoryAdapter(this, categoryList);
        categoryRecyclerView.setAdapter(categoryAdapter);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load categories from the database
        loadCategoriesFromDatabase();

        // Set an OnClickListener for the back arrow icon
        leftArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToMainActivity();
            }
        });

        // Set an OnClickListener for the addCategoryLayout
        LinearLayout addCategoryLayout = findViewById(R.id.addCategoryLayout);
        addCategoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the custom dialog for adding a new category
                showAddCategoryDialog();
            }
        });
    }

    private void loadCategoriesFromDatabase() {
        // Clear the existing list
        categoryList.clear();

        // Retrieve categories from the database
        List<CategoryModel> categoriesFromDb = getCategoriesFromDatabase();

        // Add the retrieved categories to the list
        categoryList.addAll(categoriesFromDb);

        // Notify the adapter that the data set has changed
        categoryAdapter.notifyDataSetChanged();
    }

    private boolean addCategoryToDatabase(String categoryName, int categoryColor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbContract.CategoryEntry.COLUMN_NAME_NAME, categoryName);
        values.put(DbContract.CategoryEntry.COLUMN_NAME_COLOR, categoryColor);

        long newRowId = db.insert(DbContract.CategoryEntry.TABLE_NAME, null, values);

        // Close the database after the transaction
        db.close();

        return newRowId != -1; // Return true if the insertion was successful
    }


    private List<CategoryModel> getCategoriesFromDatabase() {
        List<CategoryModel> categoryList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DbContract.CategoryEntry._ID,
                DbContract.CategoryEntry.COLUMN_NAME_NAME,
                DbContract.CategoryEntry.COLUMN_NAME_COLOR
        };

        Cursor cursor = db.query(
                DbContract.CategoryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry._ID));
            String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME_NAME));
            int categoryColor = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.CategoryEntry.COLUMN_NAME_COLOR));

            CategoryModel category = new CategoryModel(categoryName, categoryColor);
            categoryList.add(category);
        }

        cursor.close();
        db.close();

        return categoryList;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateBackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private boolean addCategory(String categoryName, int color) {
        // Implement your logic to add the category here
        // If the addition is successful, return true; otherwise, return false
        boolean isCategoryAdded = addCategoryToDatabase(categoryName, color);// Replace with your logic

        if (isCategoryAdded) {
            CategoryModel newCategory = new CategoryModel(categoryName, color);
            categoryList.add(newCategory); // Add the new category to the list
            categoryAdapter.notifyItemInserted(categoryList.size() - 1); // Notify the adapter that an item is inserted
        }

        return isCategoryAdded;
    }

    private void showAddCategoryDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_add_category);

        final EditText categoryNameEditText = dialog.findViewById(R.id.categoryNameEditText);
        colorOptionsLayout = dialog.findViewById(R.id.colorOptionsLayout);
        TextView saveTextView = dialog.findViewById(R.id.saveTextView);
        TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);

        final int[] selectedColorIndex = { -1 };

        for (int i = 0; i < colorOptionsLayout.getChildCount(); i++) {
            final View colorOption = colorOptionsLayout.getChildAt(i);

            final int index = i; // Create a final copy of 'i'

            colorOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lastSelectedColorView != null) {
                        // Restore the background of the previously selected color
                        lastSelectedColorView.setBackgroundColor(originalColors[lastSelectedColor]);
                    }

                    // Select the new color and store its index
                    selectedColor = getColorForView(view);
                    lastSelectedColorView = view;
                    lastSelectedColor = index;

                    // Apply the selected background
                    view.setBackgroundResource(selectorDrawableIds[index]);
                }
            });
        }

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = categoryNameEditText.getText().toString();
                if (!categoryName.isEmpty() && selectedColor != -1) {
                    // Attempt to add the category
                    boolean isCategoryAdded = addCategory(categoryName, selectedColor);
                    dialog.dismiss();

                    if (isCategoryAdded) {
                        showToast("Category added successfully");
                    } else {
                        showToast("Failed to add category. Please try again.");
                    }
                } else {
                    showToast("Please enter a category name and select a color.");
                }
            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private int getColorForView(View view) {
        int colorId = -1; // Initialize with an invalid value

        if (view.getId() == R.id.colorViolet) {
            colorId = R.color.colorViolet;
        } else if (view.getId() == R.id.colorYellow) {
            colorId = R.color.colorYellow;
        } else if (view.getId() == R.id.colorGreen) {
            colorId = R.color.colorGreen;
        } else if (view.getId() == R.id.colorRed) {
            colorId = R.color.colorRed;
        } else if (view.getId() == R.id.colorBlue) {
            colorId = R.color.colorBlue;
        } else if (view.getId() == R.id.colorOrange) {
            colorId = R.color.colorOrange;
        } else if (view.getId() == R.id.colorPink) {
            colorId = R.color.colorPink;
        }
        return colorId != -1 ? getResources().getColor(colorId) : -1;
    }
}
