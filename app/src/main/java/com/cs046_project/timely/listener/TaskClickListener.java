package com.cs046_project.timely.listener;

public interface TaskClickListener {
    void onCardClick(int position);
    void onEditClick(int position);
    void onDeleteClick(int position);
}