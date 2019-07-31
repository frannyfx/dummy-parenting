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
    private RecordingListener recordingListener;

    /**
     * Create the adapter.
     * @param recordingList The dataset of recordings.
     */
    public RecordingAdapter(List<Recording> recordingList) {
        dataset = recordingList;
    }

    /**
     * Set the listener for recording events.
     * @param recordingListener The listener.
     */
    public void setRecordingListener(RecordingListener recordingListener) {
        this.recordingListener = recordingListener;
    }

    // Define the item type
    public class RecordingHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CardView cardView;

        public RecordingHolder(CardView v) {
            // Setup the views
            super(v);
            cardView = v;

            // Set interaction listeners
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (recordingListener != null)
                recordingListener.onClick(cardView, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (recordingListener != null)
                recordingListener.onLongClick(cardView, getAdapterPosition());
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
        ((TextView)holder.cardView.findViewById(R.id.recording_title_textview)).setText(recording.recordingTitle == null ? String.format("Recording #%d", recording.recordingId) : recording.recordingTitle);
        ((TextView)holder.cardView.findViewById(R.id.recording_date_textview)).setText(recording.recordingDate.toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
