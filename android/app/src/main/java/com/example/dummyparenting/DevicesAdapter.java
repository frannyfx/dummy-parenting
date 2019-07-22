package com.example.dummyparenting;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceHolder> {
    private BluetoothDevice[] dataset;              // The list of elements
    private OnDeviceClickListener clickListener;    // Click listener for items

    /**
     * Create the adapter.
     * @param devices The dataset of devices.
     */
    public DevicesAdapter(BluetoothDevice[] devices) {
        dataset = devices;
    }

    /**
     * Set the click listener.
     * @param clickListener The click listener.
     */
    public void setClickListener(OnDeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // Define the item type
    public class DeviceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView cardView;

        public DeviceHolder(CardView v) {
            // Setup the views
            super(v);
            cardView = v;

            // Set click listener
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(cardView, getAdapterPosition());
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DevicesAdapter.DeviceHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_view, parent, false);

        DeviceHolder vh = new DeviceHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ((TextView)holder.cardView.findViewById(R.id.device_name)).setText(dataset[position].getName());
        ((TextView)holder.cardView.findViewById(R.id.device_mac)).setText(dataset[position].getAddress());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.length;
    }
}