package com.cs046_project.timely.model;

public class CategoryModel {
    private String categoryName;
    private int categoryColor; // Add a color property

    public CategoryModel(String categoryName, int categoryColor) {
        this.categoryName = categoryName;
        this.categoryColor = categoryColor; // Initialize the color property
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    // Add a setter method for the categoryColor
    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }
}