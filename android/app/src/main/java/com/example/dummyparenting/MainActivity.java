package com.example.dummyparenting;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 1;
    private static final String TAG = "main";

    // Permissions
    private String permissions[] =  new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean audioRecordingPermission = false;
    private boolean writeExternalStoragePermission = false;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialise UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Switch
        Switch backgroundServiceSwitch = (Switch) findViewById(R.id.switch_background_service);
        backgroundServiceSwitch.setChecked(isServiceRunningInForeground(this, MonitorService.class));
        backgroundServiceSwitch.setOnCheckedChangeListener((CompoundButton view, boolean checked) -> backgroundServiceToggled(checked));
    }

    /**
     * Check whether a specific service is currently running.
     * TODO: Move to utils.
     */
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Stop and start the background service.
     * @param enabled Whether the background service should be enabled or disabled.
     */
    private void backgroundServiceToggled(boolean enabled) {
        Log.d(TAG, "Background service " + (enabled ? "enabled" : "disabled") + ".");
        if (enabled) {
            // Request audio perms
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        } else {
            Intent serviceIntent = new Intent(this, MonitorService.class);
            stopService(serviceIntent);
        }
    }

    /**
     * Receive the results to the permissions request before starting the background service.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSIONS:
                audioRecordingPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                writeExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (audioRecordingPermission && writeExternalStoragePermission) {
            // Start service
            Intent serviceIntent = new Intent(this, MonitorService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            Log.d(TAG, "No permissions!");
        }
    }
}
