package com.example.dummyparenting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TriggersActivity extends AppCompatActivity implements OnTriggerLongPressListener {
    private static final String TAG = "triggers";

    // Triggers list
    private List<String> triggersList;
    private RecyclerView recyclerView;
    private TriggersAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triggers);

        // Initialise UI
        getSupportActionBar().setTitle("Triggers");

        // Setup list
        triggersList = new ArrayList(Preferences.getTriggersList(this));
        adapter = new TriggersAdapter(triggersList);

        recyclerView = (RecyclerView) findViewById(R.id.triggers_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLongPressListener(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Create a specific menu in the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.triggers_menu, menu);
        return true;
    }

    /**
     * Handle menu button presses.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_trigger:
                showAddTriggerDialog();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Show a dialog that allows the user to add a new trigger code
     */
    private void showAddTriggerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add trigger");
        builder.setMessage("Type in the serial number of the button you'd like to use as a trigger.");

        // Create input container
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        // Create input
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setLayoutParams(params);

        // Add input to container and set the view
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Add", (DialogInterface dialog, int which) -> {
            String trigger = input.getText().toString();
            addTrigger(trigger);
        });

        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    /**
     * Parse, validate and add a new trigger code.
     * @param trigger The trigger code entered by the user.
     */
    private void addTrigger(String trigger) {
        trigger = trigger.trim().toUpperCase();

        // Validate trigger
        if (trigger.length() != 16) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid trigger")
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setMessage("The trigger code you entered is invalid. Please check your spelling and try again.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (triggersList.contains(trigger)) {
            new AlertDialog.Builder(this)
                    .setTitle("Duplicate trigger")
                    .setIcon(R.drawable.ic_error_black_24dp)
                    .setMessage("The trigger code you entered is already in the list.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Add it, save it and update list
        Log.d(TAG, String.format("Added new trigger '%s'.", trigger));
        triggersList.add(trigger);
        Preferences.setTriggersList(this, triggersList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Handle the long press event for items in the list.
     * @param view The view that was long pressed.
     * @param position The position of the item.
     */
    @Override
    public void onLongPress(View view, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete trigger")
            .setMessage("Are you sure you like to delete this trigger?")
            .setPositiveButton("Yes", (DialogInterface dialog, int which) -> {
                triggersList.remove(position);
                Preferences.setTriggersList(this, triggersList);
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("No", (DialogInterface dialog, int which) -> {

            })
            .show();
    }
}