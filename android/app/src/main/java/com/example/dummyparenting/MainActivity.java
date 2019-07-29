package com.example.dummyparenting;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private static final String TAG = "main";

    // Service
    private MonitorManager monitorManager;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.main_activity_title));

        // Setup monitor manager
        monitorManager = MonitorManager.getInstance(this);

        // Request required permissions
        ActivityCompat.requestPermissions(this, MonitorManager.permissions, MonitorManager.REQUEST_PERMISSIONS);
    }

    /**
     * Used to stop and start the monitoring service when coming back from the Settings panel.
     */
    @Override
    public void onResume(){
        super.onResume();
        monitorManager.toggleMonitor(Preferences.getBackgroundRecordingEnabled(this));
    }

    /**
     * Create a specific menu in the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handle menu button presses.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Open settings activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Receive the results to the permissions request before starting the monitoring service.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check required permissions
        boolean audioRecordingPermission = false;
        boolean writeExternalStoragePermission = false;

        switch (requestCode){
            case MonitorManager.REQUEST_PERMISSIONS:
                audioRecordingPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                writeExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        monitorManager.setPermissions(audioRecordingPermission, writeExternalStoragePermission);
    }
}
