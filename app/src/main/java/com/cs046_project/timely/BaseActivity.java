package com.cs046_project.timely;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarWhite();
    }

    // Method to set the status bar color to white and darken icons
    private void setStatusBarWhite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // Set status bar color to white

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Dark icons
            }
        }
        //View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android:windowLightStatusBar="true"
        // will make the status bar icons and text dark (black)
        // so they remain visible against a white or light-colored background.
    }

    private void setButtonColors() {
        // Set default button color for all buttons in the activity
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            for (View view : rootView.getTouchables()) {
                if (view instanceof Button) {
                    view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                }
            }
        }
    }
}
/*
 *Benefits:
DRY Principle: Reduces the need to repeat code in each activity.
Scalability: You can easily add more shared functionality in BaseActivity in the future (e.g., logging, analytics tracking).
Conclusion:
By extending BaseActivity, you can make all activities inherit common behavior like setting the status bar color while keeping the structure clean and organized.
 */