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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TriggersActivity extends AppCompatActivity implements OnTriggerLongPressListener, TextInputDialogResultListener, YNDialogResultListener {
    private static final String TAG = "triggers";
    private static final int ADD_TRIGGER_DIALOG_ID = 0;
    private static final int DELETE_TRIGGER_DIALOG_ID = 1;

    // Deleting
    private int deletionPosition = -1;

    // Triggers list
    private List<String> triggersList;
    private RecyclerView recyclerView;
    private TriggerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // UI
    private TextView emptyDatasetTextView;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triggers);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.triggers_activity_title));
        emptyDatasetTextView = findViewById(R.id.triggers_empty_dataset_textview);
        recyclerView = findViewById(R.id.triggers_list);

        // Setup list
        triggersList = new ArrayList(Preferences.getTriggersList(this));
        adapter = new TriggerAdapter(triggersList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLongPressListener(this);
        recyclerView.setAdapter(adapter);

        // Update visibility
        updateDatasetEmpty();
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
        Utils.showTextInputDialog(
                this,
                this,
                ADD_TRIGGER_DIALOG_ID,
                getString(R.string.triggers_add_trigger_title),
                getString(R.string.triggers_add_trigger_message),
                getString(R.string.triggers_add_trigger_add),
                getString(R.string.triggers_add_trigger_cancel));
    }

    /**
     * Parse, validate and add a new trigger code.
     * @param trigger The trigger code entered by the user.
     */
    private void addTrigger(String trigger) {
        trigger = trigger.trim().toUpperCase();

        // Validate trigger
        if (trigger.length() != 16) {
            Utils.showErrorDialog(this, getString(R.string.triggers_invalid_trigger_title), getString(R.string.triggers_invalid_trigger_message), getString(R.string.ok));
            return;
        }

        if (triggersList.contains(trigger)) {
            Utils.showErrorDialog(this, getString(R.string.triggers_duplicate_trigger_title), getString(R.string.triggers_duplicate_trigger_message), getString(R.string.ok));
            return;
        }

        // Add it, save it and update list
        Log.d(TAG, String.format("Added new trigger '%s'.", trigger));
        triggersList.add(trigger);
        Preferences.setTriggersList(this, triggersList);
        adapter.notifyDataSetChanged();
        updateDatasetEmpty();
    }

    /**
     * Handle the long press event for items in the list.
     * @param view The view that was long pressed.
     * @param position The position of the item.
     */
    @Override
    public void onLongPress(View view, int position) {
        Utils.showYNDialog(
                this,
                this,
                DELETE_TRIGGER_DIALOG_ID,
                getString(R.string.triggers_delete_trigger_title),
                getString(R.string.triggers_delete_trigger_message),
                getString(R.string.triggers_delete_trigger_confirm),
                getString(R.string.triggers_delete_trigger_cancel));

        deletionPosition = position;
    }

    private void updateDatasetEmpty() {
        emptyDatasetTextView.setVisibility(triggersList.size() == 0 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(triggersList.size() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTextInputDialogResult(int dialogId, String result) {
        if (dialogId == ADD_TRIGGER_DIALOG_ID && result != null) {
            addTrigger(result);
        }
    }

    @Override
    public void onYNDialogResult(int dialogId, boolean result) {
        if (dialogId == DELETE_TRIGGER_DIALOG_ID) {
            if (result && deletionPosition != -1) {
                triggersList.remove(deletionPosition);
                Preferences.setTriggersList(this, triggersList);
                adapter.notifyDataSetChanged();
                updateDatasetEmpty();
            }

            deletionPosition = -1;
        }
    }
}