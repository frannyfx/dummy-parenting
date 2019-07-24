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
        getSupportActionBar().setTitle("Settings");

        // Set-up form
        backgroundRecordingSwitch = findViewById(R.id.background_recording_switch);
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

    private void loadPreferences() {
        // Load the values
        backgroundRecordingEnabled = Preferences.getBackgroundRecordingEnabled(this);
        scheduleEnabled = Preferences.getScheduleEnabled(this);
        circularRecordingLength = Preferences.getCircularRecordingLength(this);
        postTriggerRecordingLength = Preferences.getPostTriggerRecordingLength(this);
    }


    private void updateUI() {
        // Update switches
        backgroundRecordingSwitch.setChecked(backgroundRecordingEnabled);
        scheduleSwitch.setChecked(scheduleEnabled);
        toggleControlsEnabled();

        // Update circular recording progress
        circularRecordingSeekBar.setProgress(circularRecordingLength);
        circularRecordingTextView.setText(circularRecordingLength == 0 ? "Disabled" : String.format("%d min%s", circularRecordingLength, circularRecordingLength == 1 ? "" : "s"));

        // Update post-trigger recording progress
        postTriggerRecordingSeekbar.setProgress(postTriggerRecordingLength);
        postTriggerRecordingTextView.setText(postTriggerRecordingLength == 0 ? "Disabled": String.format("%d min%s", postTriggerRecordingLength, postTriggerRecordingLength == 1 ? "" : "s"));
    }

    private void toggleControlsEnabled() {
        scheduleSwitch.setEnabled(backgroundRecordingEnabled);
        scheduleTextView.setEnabled(backgroundRecordingEnabled);
        scheduleButton.setEnabled(backgroundRecordingEnabled && scheduleEnabled);

        circularRecordingSeekBar.setEnabled(!backgroundRecordingEnabled);
        postTriggerRecordingSeekbar.setEnabled(!backgroundRecordingEnabled);
    }

    private void backgroundRecordingToggled(boolean enabled) {
        Log.d(TAG, "Background recording toggled.");
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

    private void scheduleToggled(boolean enabled) {
        Log.d(TAG, "Schedule toggled.");

        Preferences.setScheduleEnabled(this, enabled);
        scheduleEnabled = enabled;
        toggleControlsEnabled();
    }

    private void scheduleButtonClicked() {
        Intent intent = new Intent(SettingsActivity.this, ScheduleActivity.class);
        SettingsActivity.this.startActivity(intent);
    }

    private void circularRecordingLengthChanged(int progress) {
        circularRecordingLength = progress;
        Preferences.setCircularRecordingLength(this, progress);
        updateUI();
    }

    private void postTriggerRecordingLengthChanged(int progress) {
        postTriggerRecordingLength = progress;
        Preferences.setPostTriggerRecordingLength(this, progress);
        updateUI();
    }
}
