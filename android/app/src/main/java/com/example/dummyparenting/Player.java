package com.example.dummyparenting;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class Player implements MediaPlayer.OnPreparedListener {
    private static Player instance;
    private static final String TAG = "player";

    // Recording
    private Recording selectedRecording;
    private MediaPlayer mediaPlayer;

    private boolean ready = false;
    private boolean playWhenReady = false;

    private Player() {

    }

    /**
     * Create the simpleton.
     */
    public static Player getInstance() {
        if (instance == null)
            instance = new Player();

        return instance;
    }

    public void setSelectedRecording(Recording recording) {
        // Destroy the old media player if we're already playing something
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Create new player
        selectedRecording = recording;
        ready = false;
    }

    /**
     * Setup the player.
     * @return Return whether setting up the player was successful.
     */
    public boolean setup(boolean playWhenReady) {
        // Don't attempt setup if the recording is null
        if (this.selectedRecording == null) {
            Log.d(TAG, "Invalid recording!");
            return false;
        }

        // Set the player to play upon load when specified
        this.playWhenReady = playWhenReady;

        // Setup the player
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(selectedRecording.filePath);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
        } catch (Exception e) {
            Log.d(TAG, "Unable to setup.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Called when the player has finished loading the recording in the background.
     * @param player The player that has finished loading.
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "Prepared MediaPlayer successfully!");
        ready = true;
        if (playWhenReady)
            play();
    }

    /**
     * Start playback of the recording.
     */
    public void play() {
        if (!ready || mediaPlayer == null || selectedRecording == null) {
            Log.d(TAG, "Not ready to play!");
            return;
        }

        Log.d(TAG, String.format("Playing recording at '%s'", selectedRecording.filePath));
        mediaPlayer.start();
    }

    public void pause() {
        if (!ready || mediaPlayer == null || selectedRecording == null)
            return;

        mediaPlayer.pause();
    }

    /**
     * Accessor method for the selected recording.
     * @return The selected recording.
     */
    public Recording getSelectedRecording() {
        return selectedRecording;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public float getProgress() {
        // Progress is zero if we haven't yet loaded anything
        if (mediaPlayer == null || selectedRecording == null || !ready)
            return 0;

        return (float)mediaPlayer.getCurrentPosition() / (float)mediaPlayer.getDuration();
    }

    public void setProgress(float progress) {
        if (mediaPlayer == null || selectedRecording == null || !ready)
            return;

        mediaPlayer.seekTo((int)(progress * mediaPlayer.getDuration()));
    }
}
