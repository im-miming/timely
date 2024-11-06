package com.cs046_project.timely.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cs046_project.timely.R;
import com.cs046_project.timely.model.CategoryModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryModel> yourCategoryList;
    private Context context;
    private int positionToDelete; // The position of the item to delete
    private CategoryModel editingCategory;
    private ImageView colorViolet;
    private ImageView colorYellow;
    private ImageView colorRed;
    private ImageView colorPink;
    private ImageView colorOrange;
    private ImageView colorGreen;
    private ImageView colorBlue;
    // Declare preselectedColorView as a class member
    private ImageView preselectedColorView;

    public CategoryAdapter(Context context, List<CategoryModel> categoryList) {
        this.context = context;
        this.yourCategoryList = categoryList;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new CategoryViewHolder(view, positionToDelete);
    }


    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        if (yourCategoryList != null && position < yourCategoryList.size()) {
            CategoryModel category = yourCategoryList.get(position);
            holder.categoryNameTextView.setText(category.getCategoryName());

            // Set the background of the colorIndicatorView to the circle_background drawable
            holder.colorIndicatorView.setBackgroundResource(R.drawable.bg_category_color);

            // Set the color of the circle_indicator to the category's color
            holder.colorIndicatorView.getBackground().setColorFilter(category.getCategoryColor(), PorterDuff.Mode.SRC);
        }
    }


    @Override
    public int getItemCount() {
        return yourCategoryList != null ? yourCategoryList.size() : 0;
    }

    public void updateColorIndicator(int position, int color) {
        if (position < yourCategoryList.size()) {
            CategoryModel category = yourCategoryList.get(position);
            category.setCategoryColor(color);

            // Notify the adapter that the item has changed
            notifyItemChanged(position);
        }
    }
    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        View colorIndicatorView;
        TextView categoryNameTextView;
        ImageView moreVertIcon;

        public CategoryViewHolder(View itemView, int position) {
            super(itemView);
            colorIndicatorView = itemView.findViewById(R.id.colorIndicatorView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            moreVertIcon = itemView.findViewById(R.id.category_option_more_vert);

            moreVertIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass the position to showCategoryOptionsDialog
                    showCategoryOptionsDialog(moreVertIcon, getAdapterPosition());
                }
            });
        }
    }

    private void showCategoryOptionsDialog(View anchorView, final int position) {
        // Pass the current category details to the dialog
        editingCategory = yourCategoryList.get(position);
        // Create a PopupWindow to display the options
        View popupView = LayoutInflater.from(context).inflate(R.layout.dialog_category_options, null);

        // Calculate the x and y offsets to move the PopupWindow leftward and upward
        int xOffset = -250; // Replace 'yourXOffset' with the desired x offset
        int yOffset = -80; // Replace 'yourYOffset' with the desired y offset

        // Create the PopupWindow with the calculated offsets
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Find the edit and delete options
        TextView editOption = popupView.findViewById(R.id.editOption);
        TextView deleteOption = popupView.findViewById(R.id.deleteOption);

        // Set click listeners for the options
        editOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Edit" option
                // Retrieve the category at the selected position
                CategoryModel categoryToEdit = yourCategoryList.get(position);

                // Get the new color (you should replace this with the actual new color)
                int newColor = categoryToEdit.getCategoryColor();

                // Update the color of the category
                categoryToEdit.setCategoryColor(newColor);
                showEditCategoryDialog();
                popupWindow.dismiss();
            }
        });
        deleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Delete" option
                showDeleteConfirmationDialog(position);
                popupWindow.dismiss();
            }
        });

        // Show the popup window near the anchor view with the specified offsets
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset);
    }
    private void showEditCategoryDialog() {
        // Inside the showEditCategoryDialog method
        preselectedColorView = getColorViewForCategoryColor(editingCategory.getCategoryColor());

        // Calculate the desired width (80% of screen width)
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int dialogWidth = (int) (screenWidth * 0.8);

        // Inflate the custom layout for the edit dialog
        View editDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_category, null);

        // Find views in the dialog layout
        EditText categoryNameEditText = editDialogView.findViewById(R.id.categoryNameEditText);
        LinearLayout colorOptionsLayout = editDialogView.findViewById(R.id.colorOptionsLayout);

        // Set the current category name in the EditText
        categoryNameEditText.setText(editingCategory.getCategoryName());

        // Determine the category's color
        int categoryColor = editingCategory.getCategoryColor();
        ImageView selectedColorView = getColorViewForCategoryColor(categoryColor);

        // Handle the selected color view
        if (selectedColorView != null) {
            handleColorOptionSelection(selectedColorView, getColorNameForCategoryColor(categoryColor));
        }

        // Find the color option ImageViews
        colorViolet = editDialogView.findViewById(R.id.colorViolet);
        colorYellow = editDialogView.findViewById(R.id.colorYellow);
        colorRed = editDialogView.findViewById(R.id.colorRed);
        colorPink = editDialogView.findViewById(R.id.colorPink);
        colorOrange = editDialogView.findViewById(R.id.colorOrange);
        colorGreen = editDialogView.findViewById(R.id.colorGreen);
        colorBlue = editDialogView.findViewById(R.id.colorBlue);

        // Add similar lines for other color options
        // Initialize the colorImageViewMap here
        Map<String, ImageView> colorImageViewMap = new HashMap<>();
        colorImageViewMap.put("violet", colorViolet);
        colorImageViewMap.put("yellow", colorYellow);
        colorImageViewMap.put("red", colorRed);
        colorImageViewMap.put("orange", colorOrange);
        colorImageViewMap.put("pink", colorPink);
        colorImageViewMap.put("green", colorGreen);
        colorImageViewMap.put("blue", colorBlue);
        // Add other color options

        // Set the background drawable for the color options based on the selectedColor
        int selectedColor = editingCategory.getCategoryColor();

        // Reset all color options to their default unselected state
        resetColorOptions();

        String colorName = getColorNameForCategoryColor(selectedColor);

        if (colorImageViewMap.containsKey(colorName)) {
            ImageView selectedColorOption = colorImageViewMap.get(colorName);
            selectedColorOption.setBackgroundResource(getSelectedColorResource(colorName));
        }

        if (selectedColor == context.getResources().getColor(R.color.colorViolet)) {
            colorViolet.setBackgroundResource(R.drawable.bg_selected_color_violet);
        } else if (selectedColor == context.getResources().getColor(R.color.colorYellow)) {
            colorYellow.setBackgroundResource(R.drawable.bg_selected_color_yellow);
        } else if (selectedColor == context.getResources().getColor(R.color.colorRed)) {
            colorRed.setBackgroundResource(R.drawable.bg_selected_color_red);
        } else if (selectedColor == context.getResources().getColor(R.color.colorPink)) {
            colorPink.setBackgroundResource(R.drawable.bg_selected_color_pink);
        } else if (selectedColor == context.getResources().getColor(R.color.colorOrange)) {
            colorOrange.setBackgroundResource(R.drawable.bg_selected_color_orange);
        } else if (selectedColor == context.getResources().getColor(R.color.colorGreen)) {
            colorGreen.setBackgroundResource(R.drawable.bg_selected_color_green);
        } else if (selectedColor == context.getResources().getColor(R.color.colorBlue)) {
            colorBlue.setBackgroundResource(R.drawable.bg_selected_color_blue);
        }

        colorViolet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorViolet, "violet");
            }
        });

        colorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorYellow, "yellow");
            }
        });

        colorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorRed, "red");
            }
        });

        colorPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorPink, "pink");
            }
        });

        colorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorOrange, "orange");
            }
        });

        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorGreen, "green");
            }
        });

        colorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleColorOptionSelection(colorBlue, "blue");
            }
        });



        // Create an AlertDialog for the edit dialog
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        editDialogBuilder.setView(editDialogView);

        final AlertDialog editDialog = editDialogBuilder.create();

        // Handle "Cancel" click
        TextView cancelTextView = editDialogView.findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog.dismiss();
            }
        });

        // Handle "Save" click
        TextView saveTextView = editDialogView.findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the category with the new name and color
                String newCategoryName = categoryNameEditText.getText().toString();
                int updatedColor = getColorFromSelectedColor();

                // Update the category's color based on the selected color
                editingCategory.setCategoryColor(updatedColor);
                editingCategory.setCategoryName(newCategoryName);

                // Notify the adapter of the changes
                notifyItemChanged(yourCategoryList.indexOf(editingCategory));

                editDialog.dismiss();
            }
        });

        // Adjust the dialog's window attributes to control its width
        editDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = editDialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = dialogWidth;
        window.setAttributes(layoutParams);
    }

    // Function to handle color option selection
    private void handleColorOptionSelection(ImageView selectedColorView, String colorName) {
        // Reset all color options to their default unselected state
        resetColorOptions();

        // Set the selected color view as the selected state
        int selectedColorResource = getSelectedColorResource(colorName);
        selectedColorView.setBackgroundResource(selectedColorResource);

        // Update the preselectedColorView to the currently selected color
        preselectedColorView = selectedColorView;
    }

    // Helper method to get the resource ID for the selected color
    private int getSelectedColorResource(String colorName) {
        return context.getResources().getIdentifier("bg_selected_color_" + colorName, "drawable", context.getPackageName());
    }

    // Helper method to get the resource ID for the unselected color
    private int getSelectColorResource(String colorName) {
        return context.getResources().getIdentifier("bg_select_" + colorName, "drawable", context.getPackageName());
    }




    // Function to reset all color options to their default unselected state
    private void resetColorOptions() {
        // Set backgrounds for all color options to their default unselected state
        colorViolet.setBackgroundResource(R.drawable.bg_select_violet);
        colorYellow.setBackgroundResource(R.drawable.bg_select_yellow);
        colorRed.setBackgroundResource(R.drawable.bg_select_red);
        colorPink.setBackgroundResource(R.drawable.bg_select_pink);
        colorOrange.setBackgroundResource(R.drawable.bg_select_orange);
        colorGreen.setBackgroundResource(R.drawable.bg_select_green);
        colorBlue.setBackgroundResource(R.drawable.bg_select_blue);
        // Add similar lines for other color options
    }

    // Function to get the selected color based on the selected color view
    private int getColorFromSelectedColor() {
        if (preselectedColorView == null) {
            // This can happen if no color was preselected
            return 0;
        }

        int selectedColor = Color.TRANSPARENT;

        if (preselectedColorView == colorViolet) {
            selectedColor = ContextCompat.getColor(context, R.color.colorViolet);
        } else if (preselectedColorView == colorYellow) {
            selectedColor = ContextCompat.getColor(context, R.color.colorYellow);
        } else if (preselectedColorView == colorRed) {
            selectedColor = ContextCompat.getColor(context, R.color.colorRed);
        } else if (preselectedColorView == colorPink) {
            selectedColor = ContextCompat.getColor(context, R.color.colorPink);
        } else if (preselectedColorView == colorOrange) {
            selectedColor = ContextCompat.getColor(context, R.color.colorOrange);
        } else if (preselectedColorView == colorGreen) {
            selectedColor = ContextCompat.getColor(context, R.color.colorGreen);
        } else if (preselectedColorView == colorBlue) {
            selectedColor = ContextCompat.getColor(context, R.color.colorBlue);
        }

        return selectedColor;
    }


    // Helper method to get the color name for a given category color
    private String getColorNameForCategoryColor(int color) {
        if (color == ContextCompat.getColor(context, R.color.colorViolet)) {
            return "violet";
        } else if (color == ContextCompat.getColor(context, R.color.colorYellow)) {
            return "yellow";
        } else if (color == ContextCompat.getColor(context, R.color.colorRed)) {
            return "red";
        } else if (color == ContextCompat.getColor(context, R.color.colorPink)) {
            return "pink";
        } else if (color == ContextCompat.getColor(context, R.color.colorOrange)) {
            return "orange";
        } else if (color == ContextCompat.getColor(context, R.color.colorGreen)) {
            return "green";
        } else if (color == ContextCompat.getColor(context, R.color.colorBlue)) {
            return "blue";
        } else {
            return "unknown"; // Handle other colors or return a default color name
        }
    }

    // Helper method to get the color view for a given category color
    private ImageView getColorViewForCategoryColor(int color) {
        if (color == ContextCompat.getColor(context, R.color.colorViolet)) {
            return colorViolet;
        } else if (color == ContextCompat.getColor(context, R.color.colorYellow)) {
            return colorYellow;
        } else if (color == ContextCompat.getColor(context, R.color.colorRed)) {
            return colorRed;
        } else if (color == ContextCompat.getColor(context, R.color.colorPink)) {
            return colorPink;
        } else if (color == ContextCompat.getColor(context, R.color.colorOrange)) {
            return colorOrange;
        } else if (color == ContextCompat.getColor(context, R.color.colorGreen)) {
            return colorGreen;
        } else if (color == ContextCompat.getColor(context, R.color.colorBlue)) {
            return colorBlue;
        } else {
            return colorViolet; // Handle other colors or return a default color view
        }
    }


    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);

        // Inflate the custom layout for the AlertDialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_category_confirm_delete, null);

        // Set rounded background for the dialog layout
        dialogView.setBackgroundResource(R.drawable.bg_rounded_dialog);

        // Find the "Confirm" and "Cancel" buttons in the custom layout
        TextView confirmButton = dialogView.findViewById(R.id.confirmTextView);
        TextView cancelButton = dialogView.findViewById(R.id.cancelTextView);

        // Create the AlertDialog
        final AlertDialog alertDialog = builder.create();
        // Retrieve the category name before deleting it
        String categoryName = getCategoryName(position);

        // Set click listeners for the "Confirm" and "Cancel" buttons
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Confirm" option (delete the category)
                alertDialog.dismiss();
                removeCategory(position);

                // Show a success message with the category name
                showCategoryDeletedMessage(categoryName);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Cancel" option
                alertDialog.dismiss();
            }
        });

        // Show the AlertDialog
        alertDialog.setView(dialogView); // Set the custom view with the rounded background
        alertDialog.show();
        // Calculate the desired width as a fraction of the screen width (80% in this example)
        int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8);
        alertDialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private String getCategoryName(int position) {
        if (position >= 0 && position < yourCategoryList.size()) {
            CategoryModel category = yourCategoryList.get(position);
            return category.getCategoryName();
        }
        return "";
    }

    private void showCategoryDeletedMessage(String categoryName) {
        // Show a success message using a Toast with the category name
        String message = "Category \"" + categoryName + "\" successfully deleted!";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void removeCategory(int position) {
        if (position >= 0 && position < yourCategoryList.size()) {
            yourCategoryList.remove(position);
            notifyItemRemoved(position);
        }
    }


}
