package com.example.dummyparenting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "player";

    // Permissions
    public static final int REQUEST_PERMISSIONS = 2;
    public static final String permissions[] =  new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};


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

        // Request read permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }

    /**
     * Handle stopping the music when the activity is no longer visible.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Stop the music if it's been initialised
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    private void initialiseMediaPlayer() {
        Log.d(TAG, "Initialising media player.");
        try {
            Log.d(TAG, Uri.parse(selectedRecording.filePath).toString());
            Log.d(TAG, selectedRecording.filePath);
            mediaPlayer = new MediaPlayer();

            // Not working since it needs encoding!
            mediaPlayer.setDataSource(selectedRecording.filePath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
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

    /**
     * Receive the results to the permissions request to access the file.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    initialiseMediaPlayer();
                else
                    Log.d(TAG, "Unable to open file due to lack of permissions.");

                break;
        }
    }
}
