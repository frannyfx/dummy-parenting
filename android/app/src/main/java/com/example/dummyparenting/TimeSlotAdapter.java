package com.example.dummyparenting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotHolder> {
    private List<TimeSlot> dataset;
    private ListItemListener listItemListener;

    /**
     * Create the adapter.
     * @param timeSlotList The dataset of time slots.
     */
    public TimeSlotAdapter(List<TimeSlot> timeSlotList) {
        dataset = timeSlotList;
    }

    /**
     * Set the listener for recording events.
     * @param listItemListener The listener.
     */
    public void setListItemListener(ListItemListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    // Define the item type
    public class TimeSlotHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public CardView cardView;

        public TimeSlotHolder(CardView v) {
            // Setup the views
            super(v);
            cardView = v;

            // Set interaction listeners
            v.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if (listItemListener != null)
                listItemListener.onLongClick(cardView, getAdapterPosition());
            return true;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TimeSlotAdapter.TimeSlotHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.time_slot_list_item, parent, false);

        TimeSlotAdapter.TimeSlotHolder vh = new TimeSlotAdapter.TimeSlotHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TimeSlotAdapter.TimeSlotHolder holder, int position) {
        TimeSlot timeSlot = dataset.get(position);
        ((TextView)holder.cardView.findViewById(R.id.time_slot_start_time_textview)).setText(Utils.getTimeStringFromMinutes(timeSlot.startTime));
        ((TextView)holder.cardView.findViewById(R.id.time_slot_end_time_textview)).setText(Utils.getTimeStringFromMinutes(timeSlot.endTime));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
