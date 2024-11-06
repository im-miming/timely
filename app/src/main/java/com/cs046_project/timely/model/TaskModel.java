package com.cs046_project.timely.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskModel implements Parcelable {
    private String taskId;
    private String taskName;
    private String category;
    private String date;
    private String duration;
    private String startTime;
    private String endTime;

    public TaskModel(String taskId, String taskName, String category, String date, String duration, String startTime, String endTime) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.category = category;
        this.date = date;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    protected TaskModel(Parcel in) {
        taskId = in.readString();
        taskName = in.readString();
        category = in.readString();
        date = in.readString();
        duration = in.readString();
        startTime = in.readString();
        endTime = in.readString();
    }

    public static final Creator<TaskModel> CREATOR = new Creator<TaskModel>() {
        @Override
        public TaskModel createFromParcel(Parcel in) {
            return new TaskModel(in);
        }

        @Override
        public TaskModel[] newArray(int size) {
            return new TaskModel[size];
        }
    };

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    // Public copy constructor method
    public TaskModel copy() {
        return new TaskModel(taskId, taskName, category, date, duration, startTime, endTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeString(category);
        dest.writeString(date);
        dest.writeString(duration);
        dest.writeString(startTime);
        dest.writeString(endTime);
    }
}
