package com.example.dummyparenting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class ConnectivityChangeDetector {
    private final static String TAG = "conn_change";

    // Context
    private Context context;
    private ConnectivityChangeListener connectivityChangeListener;
    private boolean firstEventFired = false;

    public ConnectivityChangeDetector(Context context) {
        // Set context for retrieving system service
        this.context = context;
        detectConnectivityChanges();
    }

    public void setConnectivityChangeListener(ConnectivityChangeListener connectivityChangeListener) {
        this.connectivityChangeListener = connectivityChangeListener;
    }

    public void detectConnectivityChanges() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        // Register callbacks
        connectivityManager.registerNetworkCallback(builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        Log.d(TAG, "Connected!");
                        if (connectivityChangeListener != null && firstEventFired)
                            connectivityChangeListener.onConnectivityChanged(true);

                        firstEventFired = true;
                    }

                    @Override
                    public void onLost(Network network) {
                        Log.d(TAG, "Disconnected.");
                        if (connectivityChangeListener != null && firstEventFired)
                            connectivityChangeListener.onConnectivityChanged(false);

                        firstEventFired = true;
                    }
                }

        );
    }
}