package com.example.dummyparenting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "settings";

    // UI elements
    private Switch backgroundRecordingSwitch;
    private TextView noPermissionsTextView;
    private Button triggersButton;
    private Switch scheduleSwitch;
    private TextView scheduleTextView;
    private Button scheduleButton;
    private SeekBar circularRecordingSeekBar;
    private TextView circularRecordingTextView;
    private SeekBar postTriggerRecordingSeekbar;
    private TextView postTriggerRecordingTextView;

    // Settings
    private boolean backgroundRecordingEnabled;
    private boolean scheduleEnabled;
    private int circularRecordingLength;
    private int postTriggerRecordingLength;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.settings_activity_title));

        // Set-up form
        backgroundRecordingSwitch = findViewById(R.id.background_recording_switch);
        noPermissionsTextView = findViewById(R.id.background_recording_permissions_textview);
        triggersButton = findViewById(R.id.triggers_button);
        scheduleSwitch = findViewById(R.id.schedule_switch);
        scheduleTextView = findViewById(R.id.schedule_textview);
        scheduleButton = findViewById(R.id.schedule_button);
        circularRecordingSeekBar = findViewById(R.id.circular_recording_seekbar);
        circularRecordingTextView = findViewById(R.id.circular_recording_textview);
        postTriggerRecordingSeekbar = findViewById(R.id.post_trigger_recording_seekbar);
        postTriggerRecordingTextView = findViewById(R.id.post_trigger_recording_textview);

        // Load preferences and update UI before listeners to prevent false triggers
        loadPreferences();
        updateUI();

        backgroundRecordingSwitch.setOnCheckedChangeListener((CompoundButton view, boolean checked) -> backgroundRecordingToggled(checked));
        triggersButton.setOnClickListener((View v) -> triggersButtonClicked());
        scheduleSwitch.setOnCheckedChangeListener((CompoundButton view, boolean checked) -> scheduleToggled(checked));
        scheduleButton.setOnClickListener((View v) -> scheduleButtonClicked());
        circularRecordingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                circularRecordingLengthChanged(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        postTriggerRecordingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                postTriggerRecordingLengthChanged(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Load the current preferences.
     */
    private void loadPreferences() {
        // Load the values
        backgroundRecordingEnabled = Preferences.getBackgroundRecordingEnabled(this);
        scheduleEnabled = Preferences.getScheduleEnabled(this);
        circularRecordingLength = Preferences.getCircularRecordingLength(this);
        postTriggerRecordingLength = Preferences.getPostTriggerRecordingLength(this);
    }


    /**
     * Perform a general UI update of all the settings so they reflect the parameters.
     */
    private void updateUI() {
        // Update switches
        backgroundRecordingSwitch.setChecked(backgroundRecordingEnabled);
        scheduleSwitch.setChecked(scheduleEnabled);
        toggleControlsEnabled();

        // Update circular recording progress
        circularRecordingSeekBar.setProgress(circularRecordingLength);

        // Update post-trigger recording progress
        postTriggerRecordingSeekbar.setProgress(postTriggerRecordingLength);

        // Update recording length labels
        updateRecordingLengthText();
    }

    /**
     * Update the labels under each recording length seekbar to show the length
     * of circular/post-trigger recording in minutes.
     */
    private void updateRecordingLengthText() {
        circularRecordingTextView.setText(getRecordingLength(circularRecordingLength));
        postTriggerRecordingTextView.setText(getRecordingLength(postTriggerRecordingLength));
    }

    /**
     * Process and pluralise the recording lengths into strings.
     * @param length The desired length.
     * @return The processed string.
     */
    private String getRecordingLength(int length) {
        return length == 0 ? getString(R.string.settings_recording_disabled) : String.format(getString(R.string.settings_minutes_template), length, getString(length == 1 ? R.string.settings_minutes_singular : R.string.settings_minutes_plural));
    }

    /**
     * Change whether specific controls are enabled depending on various
     * conditions that they depend on.
     */
    private void toggleControlsEnabled() {
        // Get permissions
        boolean permissions = MonitorManager.getInstance(this).getPermissions();

        backgroundRecordingSwitch.setEnabled(permissions);
        noPermissionsTextView.setVisibility(permissions ? View.GONE : View.VISIBLE);
        scheduleSwitch.setEnabled(backgroundRecordingEnabled);
        scheduleTextView.setEnabled(backgroundRecordingEnabled);
        scheduleButton.setEnabled(backgroundRecordingEnabled && scheduleEnabled);

        circularRecordingSeekBar.setEnabled(!backgroundRecordingEnabled);
        postTriggerRecordingSeekbar.setEnabled(!backgroundRecordingEnabled);
    }

    /**
     * Update the background recording value and start/stop the service
     * if necessary.
     * @param enabled Whether background recording is enabled.
     */
    private void backgroundRecordingToggled(boolean enabled) {
        Log.d(TAG, "Background recording toggled.");
        // Start or stop service
        MonitorManager.getInstance(this).toggleMonitor(enabled);

        // Save new preference
        Preferences.setBackgroundRecordingEnabled(this, enabled);
        backgroundRecordingEnabled = enabled;

        // Disable controls depending on value
        toggleControlsEnabled();

        // If we're disabled, switch off the schedule toggle and save it
        if (!enabled) {
            scheduleSwitch.setChecked(false);
        }
    }

    /**
     * Start the triggers activity so the user can add or remove trigger codes.
     */
    private void triggersButtonClicked() {
        Intent intent = new Intent(SettingsActivity.this, TriggersActivity.class);
        SettingsActivity.this.startActivity(intent);
    }

    /**
     * Update the schedule value.
     * @param enabled Whether a schedule should be used.
     */
    private void scheduleToggled(boolean enabled) {
        Log.d(TAG, "Schedule toggled.");

        Preferences.setScheduleEnabled(this, enabled);
        scheduleEnabled = enabled;
        toggleControlsEnabled();
    }

    /**
     * Start the schedule activity so the user can customise the recording schedule.
     */
    private void scheduleButtonClicked() {
        Intent intent = new Intent(SettingsActivity.this, ScheduleActivity.class);
        SettingsActivity.this.startActivity(intent);
    }

    /**
     * Update the circular recording length in minutes.
     * @param progress The number of minutes for the new circular recording length.
     */
    private void circularRecordingLengthChanged(int progress) {
        circularRecordingLength = progress;
        Preferences.setCircularRecordingLength(this, progress);
        updateRecordingLengthText();
    }

    /**
     * Update the post-trigger recording length in minutes.
     * @param progress The number of minutes for the new post-trigger recording length.
     */
    private void postTriggerRecordingLengthChanged(int progress) {
        postTriggerRecordingLength = progress;
        Preferences.setPostTriggerRecordingLength(this, progress);
        updateRecordingLengthText();
    }
}
