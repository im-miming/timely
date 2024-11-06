package com.cs046_project.timely.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cs046_project.timely.model.CategoryModel;
import com.cs046_project.timely.model.TaskModel;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TimelyDatabase.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DbContract.CategoryEntry.TABLE_NAME + " (" +
                    DbContract.CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                    DbContract.CategoryEntry.COLUMN_NAME_NAME + " TEXT," +
                    DbContract.CategoryEntry.COLUMN_NAME_COLOR + " INTEGER)";

    private static final String SQL_CREATE_TASK_ENTRIES =
            "CREATE TABLE " + DbContract.TaskEntry.TABLE_NAME + " (" +
                    DbContract.TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    DbContract.TaskEntry.COLUMN_NAME_TASK_NAME + " TEXT," +
                    DbContract.TaskEntry.COLUMN_NAME_CATEGORY + " TEXT," +
                    DbContract.TaskEntry.COLUMN_NAME_DATE + " TEXT," +
                    DbContract.TaskEntry.COLUMN_NAME_DURATION + " TEXT," +
                    DbContract.TaskEntry.COLUMN_NAME_START_TIME + " TEXT," +
                    DbContract.TaskEntry.COLUMN_NAME_END_TIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DbContract.CategoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_TASK_ENTRIES =
            "DROP TABLE IF EXISTS " + DbContract.TaskEntry.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_TASK_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_TASK_ENTRIES);
        onCreate(db);
    }

    // Method to get all categories from the database
    public List<CategoryModel> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<CategoryModel> categoryList = new ArrayList<>();

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

    // Method to insert a task into the database
    public long insertTask(TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.TaskEntry.COLUMN_NAME_TASK_NAME, task.getTaskName());
        values.put(DbContract.TaskEntry.COLUMN_NAME_CATEGORY, task.getCategory());
        values.put(DbContract.TaskEntry.COLUMN_NAME_DATE, task.getDate());
        values.put(DbContract.TaskEntry.COLUMN_NAME_DURATION, task.getDuration());
        values.put(DbContract.TaskEntry.COLUMN_NAME_START_TIME, task.getStartTime());
        values.put(DbContract.TaskEntry.COLUMN_NAME_END_TIME, task.getEndTime());

        long newRowId = db.insert(DbContract.TaskEntry.TABLE_NAME, null, values);
        db.close();

        return newRowId;
    }

    public List<TaskModel> getAllTasks() {
        return getAllTasks(null);
    }

    public List<TaskModel> getAllTasks(String filterCategory) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<TaskModel> taskList = new ArrayList<>();

        String[] projection = {
                DbContract.TaskEntry._ID,
                DbContract.TaskEntry.COLUMN_NAME_TASK_NAME,
                DbContract.TaskEntry.COLUMN_NAME_CATEGORY,
                DbContract.TaskEntry.COLUMN_NAME_DATE,
                DbContract.TaskEntry.COLUMN_NAME_DURATION,
                DbContract.TaskEntry.COLUMN_NAME_START_TIME,
                DbContract.TaskEntry.COLUMN_NAME_END_TIME
        };

        String selection = null;
        String[] selectionArgs = null;

        if (filterCategory != null) {
            // If filterCategory is not null, filter by category
            selection = DbContract.TaskEntry.COLUMN_NAME_CATEGORY + "=?";
            selectionArgs = new String[]{filterCategory};
        }

        Cursor cursor = db.query(
                DbContract.TaskEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            // Extract data from the cursor and create a TaskModel object
            // Adjust the column indices based on your database schema
            String taskId = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry._ID));
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_TASK_NAME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_CATEGORY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_DATE));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_DURATION));
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_START_TIME));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_END_TIME));

            TaskModel task = new TaskModel(taskId, taskName, category, date, duration, startTime, endTime);
            taskList.add(task);
        }

        cursor.close();
        db.close();

        return taskList;
    }

    // Method to delete a task by ID
    public void deleteTask(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = DbContract.TaskEntry._ID + "=?";
        String[] selectionArgs = {taskId};
        db.delete(DbContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public boolean updateTask(TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put the updated values in the ContentValues
        values.put(DbContract.TaskEntry.COLUMN_NAME_TASK_NAME, task.getTaskName());
        values.put(DbContract.TaskEntry.COLUMN_NAME_CATEGORY, task.getCategory());
        values.put(DbContract.TaskEntry.COLUMN_NAME_DATE, task.getDate());
        values.put(DbContract.TaskEntry.COLUMN_NAME_DURATION, task.getDuration());
        values.put(DbContract.TaskEntry.COLUMN_NAME_START_TIME, task.getStartTime());
        values.put(DbContract.TaskEntry.COLUMN_NAME_END_TIME, task.getEndTime());

        // Update the task in the database
        int rowsAffected = db.update(
                DbContract.TaskEntry.TABLE_NAME,
                values,
                DbContract.TaskEntry._ID + "=?",
                new String[]{String.valueOf(task.getTaskId())}
        );

        // Close the database connection
        db.close();

        // Return true if the update was successful, false otherwise
        return rowsAffected > 0;
    }
    // Inside DbHelper class
    public TaskModel getTaskById(String taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        TaskModel task = null;

        String[] projection = {
                DbContract.TaskEntry._ID,
                DbContract.TaskEntry.COLUMN_NAME_TASK_NAME,
                DbContract.TaskEntry.COLUMN_NAME_CATEGORY,
                DbContract.TaskEntry.COLUMN_NAME_DATE,
                DbContract.TaskEntry.COLUMN_NAME_DURATION,
                DbContract.TaskEntry.COLUMN_NAME_START_TIME,
                DbContract.TaskEntry.COLUMN_NAME_END_TIME
        };

        String selection = DbContract.TaskEntry._ID + "=?";
        String[] selectionArgs = {taskId};

        Cursor cursor = db.query(
                DbContract.TaskEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            // Extract data from the cursor and create a TaskModel object
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_TASK_NAME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_CATEGORY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_DATE));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_DURATION));
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_START_TIME));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.TaskEntry.COLUMN_NAME_END_TIME));

            task = new TaskModel(taskId, taskName, category, date, duration, startTime, endTime);
        }

        cursor.close();
        db.close();

        return task;
    }

}
