package com.example.dummyparenting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnDeviceClickListener {
    // Bluetooth
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothManager bluetoothManager;
    private BluetoothDevice[] currentDeviceList;

    // UI
    private RecyclerView recyclerView;
    private DevicesAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_device_list);

        // Initialise UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.devices_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Empty list
        adapter = new DevicesAdapter(new BluetoothDevice[0]);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // Start BT
        initialiseBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshDeviceList();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Enable BT if it is supported.
     */
    private void initialiseBluetooth() {
        // Create new BT manager
        bluetoothManager = BluetoothManager.getInstance();

        // Check if Bluetooth is supported.
        if (!bluetoothManager.isBluetoothSupported()) {
            // Show alert
            new AlertDialog.Builder(this)
                .setTitle("Bluetooth not supported.")
                .setMessage("Your device does not support Bluetooth.").show();
            return;
        }

        // Enable BT if it is not yet enabled.
        if (!bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.enableBluetooth(this, REQUEST_ENABLE_BT);
        } else {
            bluetoothManager.printPairedDevices();
            refreshDeviceList();
        }
    }

    /**
     * Handle the response received from the request to enable BT.
     */
    private void handleBluetoothResponse(int result) {
        Log.d("blem", "Received REQUEST_ENABLE_BT response.");

        if (result == RESULT_OK) {
            bluetoothManager.printPairedDevices();
            refreshDeviceList();
        } else {
            // Show alert saying that BT is required!
            new AlertDialog.Builder(this)
                .setTitle("Bluetooth is required.")
                .setMessage("This application requires Bluetooth to function.").show();
        }
    }

    /**
     * Refresh the list of devices available.
     */
    private void refreshDeviceList() {
        Log.d("blem", "Refreshing device list...");

        // Get paired devices and convert it to an array
        Set<BluetoothDevice> devices = bluetoothManager.getPairedDevices();
        currentDeviceList = new BluetoothDevice[devices.size()];
        currentDeviceList = devices.toArray(currentDeviceList);

        // Create new adapter and swap the dataset
        adapter = new DevicesAdapter(currentDeviceList);
        adapter.setClickListener(this);
        recyclerView.swapAdapter(adapter, true);
    }

    /**
     * Handle the click event for items in the list.
     * @param view The view that was clicked.
     * @param position The position of the item.
     */
    @Override
    public void onClick(View view, int position) {
        Log.d("blem", String.format("Selected device with index %d - %s", position, currentDeviceList[position].getName()));

        // Connect to device
        bluetoothManager.connectToDevice(currentDeviceList[position]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                handleBluetoothResponse(resultCode);
                break;
            default:
                Log.w("blem", "Unhandled activity result received.");
        }
    }
}
