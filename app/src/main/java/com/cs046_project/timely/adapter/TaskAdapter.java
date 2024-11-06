package com.cs046_project.timely.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cs046_project.timely.EditTaskActivity;
import com.cs046_project.timely.R;
import com.cs046_project.timely.controller.SwipeController;
import com.cs046_project.timely.database.DbHelper;
import com.cs046_project.timely.listener.TaskClickListener;
import com.cs046_project.timely.model.TaskModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskModel> taskList;
    private TaskClickListener clickListener;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DbHelper dbHelper;

    public TaskAdapter(List<TaskModel> taskList, RecyclerView recyclerView, DbHelper dbHelper, TaskClickListener clickListener) {
        this.taskList = taskList;
        this.recyclerView = recyclerView;
        this.dbHelper = dbHelper;
        this.clickListener = clickListener;

        // Set up ItemTouchHelper here
        SwipeController swipeController = new SwipeController(recyclerView, taskList, this, dbHelper);
        new ItemTouchHelper(swipeController).attachToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_items_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.taskNameTextView.setText(task.getTaskName());
        holder.durationTextView.setText(task.getDuration());
        holder.startTimeTextView.setText(task.getStartTime());
        holder.endTimeTextView.setText(task.getEndTime());

        // Set click listener for the entire card
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start EditTaskActivity with task details
                Intent intent = new Intent(v.getContext(), EditTaskActivity.class);
                intent.putExtra("taskDetails", task); // Assuming TaskModel is Serializable
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView taskNameTextView;
        TextView durationTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;
        LinearLayout editDeleteLayout;
        // Add references for "Edit" and "Delete" TextViews
        TextView editTextView;
        TextView deleteTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskNameTextView = itemView.findViewById(R.id.taskName);
            durationTextView = itemView.findViewById(R.id.duration);
            startTimeTextView = itemView.findViewById(R.id.startTime);
            endTimeTextView = itemView.findViewById(R.id.endTime);

            // Set click listener for the entire card
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Notify the card click event to the listener
                clickListener.onCardClick(position);
            }
        }
    }
    public void setData(List<TaskModel> newData) {
        this.taskList = newData;
        notifyDataSetChanged();
    }
}