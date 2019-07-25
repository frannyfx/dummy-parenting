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
        getSupportActionBar().setTitle(getString(R.string.triggers_activity_title));

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
        builder.setTitle(getString(R.string.triggers_add_trigger_title));
        builder.setMessage(getString(R.string.triggers_add_trigger_message));

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

        builder.setPositiveButton(getString(R.string.triggers_add_trigger_add), (DialogInterface dialog, int which) -> {
            String trigger = input.getText().toString();
            addTrigger(trigger);
        });

        builder.setNegativeButton(getString(R.string.triggers_add_trigger_cancel), (DialogInterface dialog, int which) -> {
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
                    .setTitle(getString(R.string.triggers_invalid_trigger_title))
                    .setIcon(R.drawable.icon_error)
                    .setMessage(getString(R.string.triggers_invalid_trigger_message))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
            return;
        }

        if (triggersList.contains(trigger)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.triggers_duplicate_trigger_title))
                    .setIcon(R.drawable.icon_error)
                    .setMessage(getString(R.string.triggers_duplicate_trigger_message))
                    .setPositiveButton(getString(R.string.ok), null)
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
            .setTitle(getString(R.string.triggers_delete_trigger_title))
            .setMessage(getString(R.string.triggers_delete_trigger_message))
            .setPositiveButton(getString(R.string.triggers_delete_trigger_confirm), (DialogInterface dialog, int which) -> {
                triggersList.remove(position);
                Preferences.setTriggersList(this, triggersList);
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton(getString(R.string.triggers_delete_trigger_cancel), (DialogInterface dialog, int which) -> {

            })
            .show();
    }
}