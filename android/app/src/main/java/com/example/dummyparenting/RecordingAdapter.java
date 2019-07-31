package com.example.dummyparenting;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordingHolder> {
    private List<Recording> dataset;
    private OnTriggerLongPressListener longPressListener;    // Long press listener

    /**
     * Create the adapter.
     * @param recordingList The dataset of recordings.
     */
    public RecordingAdapter(List<Recording> recordingList) {
        dataset = recordingList;
    }

    /**
     * Set the long press listener.
     * @param longPressListener The long press listener.
     */
    public void setLongPressListener(OnTriggerLongPressListener longPressListener) {
        this.longPressListener = longPressListener;
    }

    // Define the item type
    public class RecordingHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public CardView cardView;

        public RecordingHolder(CardView v) {
            // Setup the views
            super(v);
            cardView = v;

            // Set click listener
            v.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            // TODO: Setup long clicking on MainActivity
            //longPressListener.onLongPress(cardView, getAdapterPosition());
            return true;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecordingAdapter.RecordingHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recording_list_item, parent, false);

        RecordingAdapter.RecordingHolder vh = new RecordingAdapter.RecordingHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecordingAdapter.RecordingHolder holder, int position) {
        Recording recording = dataset.get(position);
        ((TextView)holder.cardView.findViewById(R.id.recording_title_textview)).setText(String.format("Recording #%d", recording.recordingId));
        ((TextView)holder.cardView.findViewById(R.id.recording_date_textview)).setText(recording.recordingDate.toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
