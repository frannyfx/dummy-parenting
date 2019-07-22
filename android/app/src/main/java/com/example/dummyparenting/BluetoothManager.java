package com.example.dummyparenting;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    // Singleton
    private static BluetoothManager instance = null;

    private BluetoothAdapter bluetoothAdapter;

    /**
     * Create Bluetooth adapter.
     */
    private BluetoothManager() {
        // Get default bluetooth adapter.
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Get the instance of the singleton class.
     * @return The instance of the singleton.
     */
    public static BluetoothManager getInstance() {
        if (instance == null)
            instance = new BluetoothManager();

        return instance;
    }

    /**
     * Whether Bluetooth is supported by the device.
     * @return True if Bluetooth is supported.
     */
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    /**
     * Whether Bluetooth is enabled on the device.
     * @return True if Bluetooth is enabled.
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * Ask the user to enable Bluetooth.
     * @param activity The current activity, needed to open the dialog to enable BT.
     * @param requestCode The desired request code used to retrieve the response.
     * @return True if device supports BT, false if it doesn't.
     */
    public void enableBluetooth(Activity activity, int requestCode) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, requestCode);
    }

    /**
     * Print the list of suitable (paired) Bluetooth devices.
     */
    public void printPairedDevices() {
        // Get paired devices
        Set<BluetoothDevice> pairedDevices = getPairedDevices();

        // List them in the console
        for (BluetoothDevice device : pairedDevices) {
            Log.d("blem", String.format("Got device: %s (%s)", device.getName(), device.getAddress()));
            for (ParcelUuid uuid : device.getUuids()) {
                Log.d("blem", uuid.toString());
            }
        }
    }

    /**
     * Attempt to connect to a specific BluetoothDevice.
     * @param device The device to connect to.
     */
    public void connectToDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
        } catch (IOException e) {
            Log.d("blem", e.getMessage());
        }
    }

    /**
     * Retrieve the list of paired Bluetooth devices.
     * @return The list of paired Bluetooth devices.
     */
    public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }
}
