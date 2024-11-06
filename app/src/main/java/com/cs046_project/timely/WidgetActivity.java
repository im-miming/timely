package com.cs046_project.timely;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class WidgetActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        MaterialButton button9 = findViewById(R.id.button9);
        button9.setBackgroundColor(getResources().getColor(android.R.color.white));


        // Call a method to set the "Widget" option as active in the navigation bar
        setWidgetActive();

        // Find the "Schedule" and "Overview" options in the shared navigation bar and set OnClickListener
        View scheduleOption = findViewById(R.id.text_schedule);
        View overviewOption = findViewById(R.id.text_overview);

        if (scheduleOption != null) {
            scheduleOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the "Schedule" activity (MainActivity)
                    Intent scheduleIntent = new Intent(WidgetActivity.this, MainActivity.class);
                    startActivity(scheduleIntent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (overviewOption != null) {
            overviewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the "Overview" activity
                    Intent overviewIntent = new Intent(WidgetActivity.this, OverviewActivity.class);
                    startActivity(overviewIntent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }

    private void setWidgetActive() {
        View widgetOption = findViewById(R.id.text_widget);
        if (widgetOption != null) {
            widgetOption.setBackgroundResource(R.drawable.bg_selected_nav_option);
            ((TextView) widgetOption).setTextColor(Color.WHITE);
        }
    }
}
