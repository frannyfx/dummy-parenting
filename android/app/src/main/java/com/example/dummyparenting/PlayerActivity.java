package com.example.dummyparenting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import java.io.IOException;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements PlayerListener, TextInputDialogResultListener {
    private static final String TAG = "player_activity";
    private static final int UPDATE_PROGRESS = 0;
    private static final int EDIT_RECORDING_NAME_DIALOG_ID = 1;

    // Player
    private Player player = Player.getInstance();
    private Recording selectedRecording;
    private boolean isSeeking = false;
    private boolean shouldPlayAfterSeek = false;

    // UI
    private TextView currentSeekTimeTextView;
    private TextView timeRemainingTextView;
    private SeekBar seekBar;
    private ImageButton skipBackButton;
    private ImageButton playPauseButton;
    private ImageButton skipForwardButton;

    private Thread updateUIThread;
    private boolean shouldUpdateUI = false;
    private Handler handler;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        // Set listener
        player.setPlayerListener(this);

        // Get recording and set the title
        selectedRecording = Player.getInstance().getSelectedRecording();
        updateTitle();

        // Initialise UI
        currentSeekTimeTextView = findViewById(R.id.player_current_seektime_textview);
        timeRemainingTextView = findViewById(R.id.player_time_remaining_textview);
        seekBar = findViewById(R.id.player_seekbar);
        skipBackButton = findViewById(R.id.player_skip_back_button);
        playPauseButton = findViewById(R.id.player_play_pause_button);
        skipForwardButton = findViewById(R.id.player_skip_forward_button);

        // Make buttons work
        playPauseButton.setOnClickListener((View view) -> player.playPause());
        skipBackButton.setOnClickListener((View view) -> handleSkip(-1));
        skipForwardButton.setOnClickListener((View view) -> handleSkip(1));

        // Seekbar setup
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (!isSeeking)
                    return;

                player.setProgress((float)seekBar.getProgress() / (float)seekBar.getMax());
                updateProgress(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
                shouldPlayAfterSeek = player.isPlaying();
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                if (shouldPlayAfterSeek)
                    player.play();
            }

        });

        // Start thread
        startUpdatingUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start updating UI again
        startUpdatingUI();
        updateButtons();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop updating UI when no longer visible
        shouldUpdateUI = false;
        player.pause();
    }

    /**
     * Create a specific menu in the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);
        return true;
    }

    /**
     * Handle menu button presses.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_recording_name:
                editRecordingName();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void startUpdatingUI() {
        // Don't try to start thread if it's already running
        if (shouldUpdateUI)
            return;

        // Start thread
        shouldUpdateUI = true;

        // Setup handler for the background thread to access UI
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch(inputMessage.what) {
                    case UPDATE_PROGRESS:
                        updateProgress(true);
                        break;
                }
            }
        };

        updateUIThread = new Thread(new Runnable() {
            public void run() {
                updateLoop();
            }
        }, "Update UI thread");
        updateUIThread.start();
    }

    private void updateLoop() {
        Log.d(TAG, "Update UI thread starting...");

        while(shouldUpdateUI) {
            // Update the UI
            if (!isSeeking)
                handler.sendEmptyMessage(UPDATE_PROGRESS);

            // Sleep
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }

        Log.d(TAG, "Update UI thread exiting!");
    }

    private void updateTitle() {
        getSupportActionBar().setTitle(selectedRecording.getTitle());
    }

    /**
     * Update play/pause icon.
     */
    private void updateButtons() {
        Log.d(TAG, "Updating buttons!");
        playPauseButton.setImageResource(player.isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
        // ...
    }

    /**
     * Update the elements of the UI showing the progress in the song.
     */
    private void updateProgress(boolean updateSeekBar) {
        float progress = player.getProgress();
        if (updateSeekBar)
            seekBar.setProgress((int)(progress * 1000));

        currentSeekTimeTextView.setText(String.format("%d:%02d", (int)(progress * selectedRecording.recordingLength / 60), (int)((progress * selectedRecording.recordingLength) % 60)));
        timeRemainingTextView.setText(String.format("-%d:%02d", (int)((1 - progress) * selectedRecording.recordingLength / 60), (int)(((1 - progress) * selectedRecording.recordingLength) % 60)));
    }

    /**
     * Called when the player pauses or starts.
     */
    @Override
    public void onPlayerStateChanged() {
        Log.d(TAG, "Player state changed.");
        updateButtons();
    }

    /**
     * Checks whether the user may want to simply go to the beginning of the song
     * rather than go to the previous track.
     * @param idDelta
     */
    private void handleSkip(int idDelta) {
        // Before skipping to the previous track, check if we just want to rewind it.
        if (idDelta < 0 && player.getCurrentPosition() > 3000) {
            goToStart();
            return;
        }

        // If these conditions are not met, just change the track.
        changeTrack(idDelta);
    }

    /**
     * Change the track that's currently playing to either the next track or the previous.
     * @param idDelta The number to add to the current track's ID to get the new one.
     */
    private void changeTrack(int idDelta) {
        if (selectedRecording == null)
            return;

        // Get the new track
        int newRecordingId = selectedRecording.recordingId + idDelta;
        AppDatabase.getInstance(this).recordingDao().getById(newRecordingId).observe(this, (Recording newRecording) -> {
            // Check if the new recording exists
            if (newRecording == null) {
                // If the user attempted to go back, then go to the start, even if it's past the threshold point
                if (idDelta < 0)
                    goToStart();

                return;
            }

            // Play the new recording
            player.setSelectedRecording(newRecording);
            selectedRecording = newRecording;
            player.setup(true);

            // Update the title of the page
            updateTitle();
        });
    }

    /**
     * Go to the beginning of the song.
     */
    private void goToStart() {
        player.setProgress(0);
        updateProgress(true);
    }

    private void editRecordingName() {
        Utils.showTextInputDialog(
                this,
                this,
                EDIT_RECORDING_NAME_DIALOG_ID,
                "Edit recording title",
                String.format("Enter a new title for \"%s\"",
                        selectedRecording.getTitle()),
                "OK",
                "Cancel");
    }

    @Override
    public void onTextInputDialogResult(int dialogId, String result) {
        if (dialogId == EDIT_RECORDING_NAME_DIALOG_ID && result != null) {
            selectedRecording.recordingTitle = result;
            updateTitle();
            Utils.runInBackground(() -> {
                AppDatabase.getInstance(this).recordingDao().update(selectedRecording);
            });
        }
    }
}
