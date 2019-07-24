package com.example.dummyparenting;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class MonitorManager {
    private static MonitorManager instance;
    private static final String TAG = "monitor_manager";

    // Status
    private Context context;
    private boolean started;

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
}
