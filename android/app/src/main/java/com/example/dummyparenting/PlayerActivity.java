package com.example.dummyparenting;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "player";

    // Recording
    Recording selectedRecording;
    MediaPlayer mediaPlayer;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        // Initialise UI
        selectedRecording = (Recording) getIntent().getSerializableExtra(getString(R.string.player_intent_extra_recording));
        getSupportActionBar().setTitle(selectedRecording.recordingTitle == null ? String.format("Recording #%s", selectedRecording.recordingId) : selectedRecording.recordingTitle);

        // Initialise media player
        try {
            Log.d(TAG, Uri.parse(selectedRecording.filePath).toString());
            Log.d(TAG, selectedRecording.filePath);
            mediaPlayer = new MediaPlayer();

            // Not working since it needs encoding!
            //mediaPlayer.setDataSource(selectedRecording.filePath);
            //mediaPlayer.prepareAsync();
            //mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Something here
            Log.d(TAG, "Unable to load file!");
        }
    }

    /**
     * Called when the file has finished loading.
     * @param player The player object that has finished loading.
     */
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "Prepared file!");
        mediaPlayer.start();
    }
}
