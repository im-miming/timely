package com.cs046_project.timely;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class OverviewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Call a method to set the "Overview" option as active in the navigation bar
        setOverviewActive();

        // Find the "Schedule" and "Widget" options in the shared navigation bar and set OnClickListener
        View scheduleOption = findViewById(R.id.text_schedule);
        View widgetOption = findViewById(R.id.text_widget);

        if (scheduleOption != null) {
            scheduleOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the "Schedule" activity (MainActivity)
                    Intent scheduleIntent = new Intent(OverviewActivity.this, MainActivity.class);
                    startActivity(scheduleIntent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (widgetOption != null) {
            widgetOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the "Widget" activity
                    Intent widgetIntent = new Intent(OverviewActivity.this, WidgetActivity.class);
                    startActivity(widgetIntent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }
    private void setOverviewActive() {
        View overviewOption = findViewById(R.id.text_overview);
        if (overviewOption != null) {
            overviewOption.setBackgroundResource(R.drawable.bg_selected_nav_option);
            ((TextView) overviewOption).setTextColor(Color.WHITE);
        }
    }

}