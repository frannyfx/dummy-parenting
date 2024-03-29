package com.example.dummyparenting;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListItemListener, YNDialogResultListener {
    private static final String TAG = "main";
    private static final int DELETE_RECORDING_DIALOG_ID = 0;

    // Deleting
    private int deletionPosition = -1;

    // Recordings
    private List<Recording> recordingsList;
    private RecyclerView recyclerView;
    private RecordingAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Player player = Player.getInstance();

    // Service
    private MonitorManager monitorManager;

    // UI
    private TextView emptyDatasetTextView;

    /**
     * Initialise the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialise UI
        getSupportActionBar().setTitle(getString(R.string.main_activity_title));
        emptyDatasetTextView = findViewById(R.id.recordings_empty_dataset);
        recyclerView = findViewById(R.id.recordings_list);

        // Initialise list
        recordingsList = new ArrayList<>();
        adapter = new RecordingAdapter(recordingsList);
        adapter.setListItemListener(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Get recordings
        setupDatabase();

        // Setup monitor manager
        monitorManager = MonitorManager.getInstance(this);

        // Request required permissions
        ActivityCompat.requestPermissions(this, MonitorManager.permissions, MonitorManager.REQUEST_PERMISSIONS);
    }

    private void setupDatabase() {
        // Asynchronously update the list
        AppDatabase.getInstance(getApplicationContext()).recordingDao().getByDate().observe(this, new Observer<List<Recording>>() {
            @Override
            public void onChanged(List<Recording> recordings) {
                Log.d(TAG, String.format("Loaded %d recordings.", recordings.size()));
                recordingsList.clear();
                recordingsList.addAll(recordings);
                adapter.notifyDataSetChanged();

                // Update visibility
                updateDatasetEmpty();
            }
        });
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

    @Override
    public void onClick(View cardView, int position) {
        // Start activity with this specific recording
        Recording selectedRecording = recordingsList.get(position);
        Log.d(TAG, String.format("Recording #%d clicked - Path: '%s'.", selectedRecording.recordingId, selectedRecording.filePath));

        player.setSelectedRecording(selectedRecording);
        player.setup(true);

        // Open player activity
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onLongClick(View cardView, int position) {
        // Get the recording to be deleted and save the position fo when the dialog 'returns'.
        Recording selectedRecording = recordingsList.get(position);
        deletionPosition = position;
        Log.d(TAG, String.format("Attempting to delete recording #%d.", selectedRecording.recordingId));

        // Show the dialog
        Utils.showYNDialog(
                this,
                this,
                DELETE_RECORDING_DIALOG_ID,
                getString(R.string.main_delete_recording_title),
                String.format(getString(R.string.main_delete_recording_message), selectedRecording.getTitle()),
                getString(R.string.main_delete_recording_confirm),
                getString(R.string.main_delete_recording_cancel)
        );
    }

    @Override
    public void onYNDialogResult(int dialogId, boolean result) {
        if (dialogId == DELETE_RECORDING_DIALOG_ID) {
            if (result) {
                Recording selectedRecording = recordingsList.get(deletionPosition);
                Utils.runInBackground(() -> {
                    AppDatabase.getInstance(this).recordingDao().deleteWithFile(selectedRecording);
                });
            }

            deletionPosition = -1;
        }
    }

    private void updateDatasetEmpty() {
        emptyDatasetTextView.setVisibility(recordingsList.size() == 0 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(recordingsList.size() == 0 ? View.GONE : View.VISIBLE);
    }
}
