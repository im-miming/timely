package com.cs046_project.timely.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cs046_project.timely.R;
import com.cs046_project.timely.adapter.TaskAdapter;
import com.cs046_project.timely.model.TaskModel;
import com.cs046_project.timely.database.DbHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwipeController extends ItemTouchHelper.Callback {

    private final RecyclerView recyclerView;
    private final List<TaskModel> taskList;
    private final TaskAdapter adapter;
    private final DbHelper dbHelper;

    public SwipeController(RecyclerView recyclerView, List<TaskModel> taskList, TaskAdapter adapter, DbHelper dbHelper) {
        this.recyclerView = recyclerView;
        this.taskList = taskList;
        this.adapter = adapter;
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d("SwipeController", "Item swiped");
        showDeleteConfirmationDialog(viewHolder.getAdapterPosition());
    }

    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(recyclerView.getContext());
        View dialogView = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.dialog_delete_task, null);
        builder.setView(dialogView);

        final AlertDialog deleteDialog = builder.create();

        TextView cancelTextView = dialogView.findViewById(R.id.cancelTextView);
        TextView confirmTextView = dialogView.findViewById(R.id.confirmTextView);

        cancelTextView.setOnClickListener(v -> {
            deleteDialog.dismiss();
            adapter.notifyItemChanged(position); // Notify adapter to redraw the item
        });

        confirmTextView.setOnClickListener(v -> {
            deleteDialog.dismiss();
            handleDelete(position);
        });

        deleteDialog.show();
    }

    private void handleDelete(int position) {
        TaskModel deletedTask = taskList.get(position);
        taskList.remove(position);
        adapter.notifyItemRemoved(position);

        TaskModel existingTask = dbHelper.getTaskById(deletedTask.getTaskId());

        if (existingTask != null) {
            Log.d("SwipeController", "Deleting task from the database: " + existingTask.getTaskName());
            dbHelper.deleteTask(deletedTask.getTaskId());

            Context context = recyclerView.getContext();
            String toastMessage = "Task deleted: " + existingTask.getTaskName();
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("SwipeController", "Error: Task not found in the database. ID: " + deletedTask.getTaskId());
        }
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        LinearLayout editDeleteLayout = itemView.findViewById(R.id.editDeleteLayout);
        int editDeleteWidth = editDeleteLayout.getWidth();

        // Calculate the new translationX for the CardView
        float translationX = dX;  // Initial translation

        if (dX < 0) {  // Swiping left
            // Ensure editDeleteLayout is visible
            editDeleteLayout.setVisibility(View.VISIBLE);

            // Adjust translation to ensure editDeleteLayout appears on the right
            translationX = Math.max(dX, -editDeleteWidth); // Move left

            // Move the CardView
            itemView.setTranslationX(translationX);

            // Ensure editDeleteLayout is visible and not out of bounds
            if (translationX < -editDeleteWidth) {
                itemView.setTranslationX(-editDeleteWidth);
            }
        } else {
            // Reset to initial position when swiping right or not swiping
            itemView.setTranslationX(0);
            editDeleteLayout.setVisibility(View.GONE);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }


    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        LinearLayout editDeleteLayout = viewHolder.itemView.findViewById(R.id.editDeleteLayout);
        float translationX = viewHolder.itemView.getTranslationX();

        // Log the final state of the card and edit/delete layout
        Log.d("SwipeDebug", "Final TranslationX: " + translationX);
        Log.d("SwipeDebug", "Edit/Delete Layout Visibility in clearView: " + (editDeleteLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));

        if (translationX <= -editDeleteLayout.getWidth()) {
            editDeleteLayout.setVisibility(View.VISIBLE);
        } else {
            editDeleteLayout.setVisibility(View.GONE);
        }

        viewHolder.itemView.setTranslationX(0);
    }

}
