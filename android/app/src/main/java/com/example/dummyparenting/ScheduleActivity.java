package com.example.dummyparenting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ScheduleActivity extends AppCompatActivity {
    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.schedule_activity_title));
    }
}
