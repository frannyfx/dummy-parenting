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

    private MonitorManager(Context context) {
        started = Utils.isServiceRunningInForeground(context, MonitorManager.class);
        this.context = context;
    }

    public static MonitorManager getInstance(Context context) {
        if (instance == null)
            instance = new MonitorManager(context);

        return instance;
    }

    public void startMonitor() {
        if (started)
            return;

        Log.d(TAG, "Starting foreground service...");
        Intent serviceIntent = new Intent(context, MonitorService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
        started = true;
    }

    public void stopMonitor() {
        if (!started)
            return;

        Log.d(TAG, "Stopping foreground service...");
        Intent serviceIntent = new Intent(context, MonitorService.class);
        context.stopService(serviceIntent);
        started = false;
    }

    public void toggleMonitor(boolean status) {
        if (status)
            startMonitor();
        else
            stopMonitor();
    }
}
