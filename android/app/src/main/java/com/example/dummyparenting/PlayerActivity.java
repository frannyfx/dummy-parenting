package com.example.dummyparenting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "player_activity";
    private static final int UPDATE_PROGRESS = 0;

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

        // Get recording and set the title
        selectedRecording = Player.getInstance().getSelectedRecording();
        getSupportActionBar().setTitle(selectedRecording.recordingTitle == null ? String.format("Recording #%s", selectedRecording.recordingId) : selectedRecording.recordingTitle);

        // Initialise UI
        currentSeekTimeTextView = findViewById(R.id.player_current_seektime_textview);
        timeRemainingTextView = findViewById(R.id.player_time_remaining_textview);
        seekBar = findViewById(R.id.player_seekbar);
        skipBackButton = findViewById(R.id.player_skip_back_button);
        playPauseButton = findViewById(R.id.player_play_pause_button);
        skipForwardButton = findViewById(R.id.player_skip_forward_button);

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
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop updating UI when no longer visible
        shouldUpdateUI = false;
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

    /**
     * Update all UI components
     */
    private void updateUI() {
        updateButtons();
        updateProgress(true);
    }

    /**
     * Update play/pause icon.
     */
    private void updateButtons() {
        playPauseButton.setImageResource(player.isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play);
        // ...
    }

    /**
     * Update the elements of the UI showing the progress in the song.
     */
    private void updateProgress(boolean updateSeekBar) {
        float progress = player.getProgress();
        Log.d(TAG, String.format("%f", progress));
        Log.d(TAG, String.format("Recording length: %d", selectedRecording.recordingLength));

        if (updateSeekBar)
            seekBar.setProgress((int)(progress * 1000));

        currentSeekTimeTextView.setText(String.format("%d:%02d", (int)(progress * selectedRecording.recordingLength / 60), (int)((progress * selectedRecording.recordingLength) % 60)));
        timeRemainingTextView.setText(String.format("-%d:%02d", (int)((1 - progress) * selectedRecording.recordingLength / 60), (int)(((1 - progress) * selectedRecording.recordingLength) % 60)));
    }
}
