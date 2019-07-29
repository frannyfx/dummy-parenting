package com.example.dummyparenting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class MonitorManager {
    private static MonitorManager instance;
    private static final String TAG = "monitor_manager";

    // Status
    private Context context;
    private boolean started;

    // Permissions
    public static final int REQUEST_PERMISSIONS = 1;
    public static final String permissions[] =  new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean audioRecordingPermission = false;
    private boolean writeExternalStoragePermission = false;

    /**
     * Create the monitor manager simpleton instance and set the initial state
     * of whether the service is already running.
     * @param context The context where it's instantiated from.
     */
    private MonitorManager(Context context) {
        started = Utils.isServiceRunningInForeground(context, MonitorManager.class);
        this.context = context;
    }

    /**
     * Create the simpleton.
     */
    public static MonitorManager getInstance(Context context) {
        if (instance == null)
            instance = new MonitorManager(context);

        return instance;
    }

    /**
     * Start the foreground service.
     */
    public void startMonitor() {
        if (started)
            return;

        // Check permissions
        if (!audioRecordingPermission || !writeExternalStoragePermission) {
            return;
        }

        Log.d(TAG, "Starting foreground service...");
        Intent serviceIntent = new Intent(context, MonitorService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
        started = true;
    }

    /**
     * Stop the foreground service.
     */
    public void stopMonitor() {
        if (!started)
            return;

        Log.d(TAG, "Stopping foreground service...");
        Intent serviceIntent = new Intent(context, MonitorService.class);
        context.stopService(serviceIntent);
        started = false;
    }

    /**
     * Toggle the foreground service.
     * @param status Whether the service should be stopped or started.
     */
    public void toggleMonitor(boolean status) {
        if (status)
            startMonitor();
        else
            stopMonitor();
    }

    /**
     * Set the local permission booleans so we can prevent starting the service without
     * the required permissions.
     * @param audioRecordingPermission Whether we have permission to record audio.
     * @param writeExternalStoragePermission Whether we have permission to write to the SD card.
     */
    public void setPermissions(boolean audioRecordingPermission, boolean writeExternalStoragePermission) {
        this.audioRecordingPermission = audioRecordingPermission;
        this.writeExternalStoragePermission = writeExternalStoragePermission;

        // If we don't have the correct permissions, simply disable it to prevent it from triggering.
        if (!this.audioRecordingPermission || !this.writeExternalStoragePermission) {
            Preferences.setBackgroundRecordingEnabled(context, false);
        }
    }

    /**
     * Accessor function for whether the service has the required permissions.
     * @return Whether the service has all the permissions it needs to start.
     */
    public boolean getPermissions() {
        return audioRecordingPermission && writeExternalStoragePermission;
    }
}
