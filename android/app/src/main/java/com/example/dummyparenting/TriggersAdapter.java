package com.example.dummyparenting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TriggersAdapter extends RecyclerView.Adapter<TriggersAdapter.TriggerHolder> {
    private List<String> dataset;
    private OnTriggerLongPressListener longPressListener;    // Long press listener

    /**
     * Create the adapter.
     * @param triggers The dataset of triggers.
     */
    public TriggersAdapter(List<String> triggers) {
        dataset = triggers;
    }

    /**
     * Set the long press listener.
     * @param longPressListener The long press listener.
     */
    public void setLongPressListener(OnTriggerLongPressListener longPressListener) {
        this.longPressListener = longPressListener;
    }

    // Define the item type
    public class TriggerHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public CardView cardView;

        public TriggerHolder(CardView v) {
            // Setup the views
            super(v);
            cardView = v;

            // Set click listener
            v.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            longPressListener.onLongPress(cardView, getAdapterPosition());
            return true;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TriggersAdapter.TriggerHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trigger_list_item, parent, false);

        TriggerHolder vh = new TriggerHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TriggerHolder holder, int position) {
        String serial = dataset.get(position);
        String displaySerial = String.format("%s %s %s %s", serial.substring(0, 4), serial.substring(4, 8), serial.substring(8, 12), serial.substring(12, 16));
        ((TextView)holder.cardView.findViewById(R.id.serial_number_textview)).setText(displaySerial);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
