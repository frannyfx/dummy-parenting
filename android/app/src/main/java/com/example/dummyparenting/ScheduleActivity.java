package com.example.dummyparenting;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.DELETE;

public class ScheduleActivity extends AppCompatActivity implements ListItemListener, YNDialogResultListener {
    private static final String TAG = "schedule_activity";
    private static final int DELETE_TIME_SLOT_DIALOG_ID = 0;

    // Deletion
    private int deletionPosition = -1;

    // Time slots
    private List<TimeSlot> timeSlotsList;
    private RecyclerView recyclerView;
    private TimeSlotAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // UI
    private TextView emptyDatasetTextView;

    // New time slot
    private int newStartTime;
    private int newEndTime;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.schedule_activity_title));
        emptyDatasetTextView = findViewById(R.id.schedule_empty_dataset_textview);
        recyclerView = findViewById(R.id.time_slots_list);

        // Setup list
        timeSlotsList = new ArrayList<>();
        adapter = new TimeSlotAdapter(timeSlotsList);
        adapter.setListItemListener(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Get schedule items
        setupDatabase();
    }

    private void setupDatabase() {
        // Asynchronously update the list
        AppDatabase.getInstance(getApplicationContext()).timeSlotDao().getByTime().observe(this, (List<TimeSlot> timeSlots) -> {
            Log.d(TAG, String.format("Loaded %d time slots.", timeSlots.size()));
            timeSlotsList.clear();
            timeSlotsList.addAll(timeSlots);
            adapter.notifyDataSetChanged();
            updateDatasetEmpty();
        });
    }

    /**
     * Create a specific menu in the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_menu, menu);
        return true;
    }

    /**
     * Handle menu button presses.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_time_slot:
                showAddTimeSlotDialog();
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddTimeSlotDialog() {
        Log.d(TAG, "Adding time slots!");

        // Reset the start and end time
        newStartTime = getResources().getInteger(R.integer.schedule_default_start_time);
        newEndTime = getResources().getInteger(R.integer.schedule_default_end_time);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add time slot");

        // Get layout
        View v = (View) this.getLayoutInflater().inflate(R.layout.time_slot_picker, null);

        // Handle set start button
        Button setStart = v.findViewById(R.id.time_slot_set_start);
        setStart.setText(Utils.getTimeStringFromMinutes(newStartTime));

        setStart.setOnClickListener((View button) -> {
            TimePickerDialog tpd = new TimePickerDialog(this, (TimePicker timePicker, int hours, int mins) -> {
                // Validate start time
                int startTime = (hours * 60) + mins;
                if (startTime >= newEndTime) {
                    Utils.showErrorDialog(this, "Invalid start time", "The time slot's start time can't be later than its end time.", "OK");
                    Log.d(TAG, "Start time greater or equal to end time!");
                    return;
                }

                // Set the new time
                newStartTime = startTime;
                setStart.setText(String.format("%d:%02d", hours, mins));
            }, newStartTime / 60, newStartTime % 60, false);

            tpd.show();
        });


        // Handle set end button
        Button setEnd = v.findViewById(R.id.time_slot_set_end);
        setEnd.setText(Utils.getTimeStringFromMinutes(newEndTime));

        setEnd.setOnClickListener((View button) -> {
            TimePickerDialog tpd = new TimePickerDialog(this, (TimePicker timePicker, int hours, int mins) -> {
                // Validate end time
                int endTime = (hours * 60) + mins;
                if (endTime <= newStartTime) {
                    Utils.showErrorDialog(this, "Invalid end time", "The time slot's end time can't be earlier than its start time.", "OK");
                    return;
                }

                // If the new end time is valid, then set it.
                newEndTime = endTime;
                setEnd.setText(String.format("%d:%02d", hours, mins));
            }, newEndTime / 60, newEndTime % 60, false);

            tpd.show();
        });

        // Handle add/cancel buttons
        builder.setView(v);
        builder.setPositiveButton("Add", (DialogInterface dialog, int which) -> {
            addTimeSlot();
        });

        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
            Log.d(TAG, "Cancelled.");
            dialog.cancel();
        });

        builder.show();
    }

    private void addTimeSlot() {
        Utils.runInBackground(() -> {
            AppDatabase.getInstance(this).timeSlotDao().insertAll(new TimeSlot(newStartTime, newEndTime));
        });
    }

    private void updateDatasetEmpty() {
        emptyDatasetTextView.setVisibility(timeSlotsList.size() == 0 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(timeSlotsList.size() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View cardView, int position) {
        // ...
        Log.d(TAG, "Click!");
    }

    @Override
    public void onLongClick(View cardView, int position) {
        // ...
        Log.d(TAG, "Long click!");
        Utils.showYNDialog(
                this,
                this,
                DELETE_TIME_SLOT_DIALOG_ID,
                "Delete time slot",
                "Are you sure you'd like to delete this time slot?",
                "Yes", "No");

        deletionPosition = position;
    }

    @Override
    public void onYNDialogResult(int dialogId, boolean result) {
        if (dialogId == DELETE_TIME_SLOT_DIALOG_ID && result && deletionPosition != -1) {
            TimeSlot slot = timeSlotsList.get(deletionPosition);
            Utils.runInBackground(() -> {
                AppDatabase.getInstance(this).timeSlotDao().delete(slot);
            });

            deletionPosition = -1;
        }
    }
}
