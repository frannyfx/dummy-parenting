package com.example.dummyparenting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MonitorService extends Service {
    public static final String CHANNEL_ID = "MonitorServiceChannel";
    private static final String TAG = "dummy_monitor";

    // Notification
    private static final int NOTIFICATION_ID = 1;

    // Monitoring
    private boolean isMonitoring = false;
    private boolean waitingForUpdate = false;
    private Thread monitoringThread;

    // Preferences
    private boolean recordingEnabled = false;
    private boolean scheduleEnabled = false;

    // Audio recording
    private AudioRecord recorder;
    private Thread recordingThread;
    private boolean isRecording = false;
    private boolean isTriggered = false;

    private int shortChunkSize;
    private int sampleRate = 44100;
    private int numChannels = 2;

    // PubNub
    private PubNub pn;
    private List<String> subscribedChannels;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handle starting the service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Foreground service started.");

        // Subscribe to events
        EventBus.getDefault().register(this);

        // Initialise
        initialisePubNub();
        startMonitoring();

        // Show notification
        startForeground(1, getNotification());
        return START_NOT_STICKY;
    }

    /**
     * Start the monitoring thread that managed settings updates and launching
     * the audio recording thread.
     */
    private void startMonitoring() {
        isMonitoring = true;
        monitoringThread = new Thread(new Runnable() {
            public void run() {
                monitor();
            }
        }, "Monitor thread");

        monitoringThread.start();
    }

    /**
     * Monitor thread loop.
     */
    private void monitor() {
        while (isMonitoring) {
            // Get preferences
            recordingEnabled = Preferences.getBackgroundRecordingEnabled(this);
            scheduleEnabled = Preferences.getScheduleEnabled(this);
            updateChannelSubscriptions(pn);
            waitingForUpdate = true;

            // If background recording is enabled...
            if (recordingEnabled) {
                // Check if the schedule is enabled
                if (scheduleEnabled) {
                    // We do have a schedule, retrieve it...
                    stopRecording();
                } else {
                    // We don't have a schedule, start/keep recording
                    startRecording();
                }
            } else
                stopRecording();    // Recording is not enabled, so if we were already recording, stop.

            // Update notification
            updateNotification();

            // Wait for update
            while (waitingForUpdate) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {}
            }

            Log.d(TAG, "Monitor thread updating...");
        }

        Log.d(TAG, "Monitor thread exiting.");
    }

    /**
     * When the settings are changed in the settings app, switch the waitingForUpdate
     * flag so the monitoring thread refreshes the settings.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingsChangedEvent(SettingsChangedEvent event) {
        waitingForUpdate = false;
    }

    /**
     * Start the recording thread so we can begin recording audio.
     */
    private void startRecording() {
        // If we're already recording, prevent this from running.
        if (isRecording)
            return;

        // Calculate short chunk size
        shortChunkSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        // Set recording flag to true and start the thread.
        isRecording = true;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, shortChunkSize);
        recorder.startRecording();
        recordingThread = new Thread(new Runnable() {
            public void run() {
                record();
            }
        }, "Recording thread");

        recordingThread.start();
    }

    /**
     * Stop the recording thread.
     */
    private void stopRecording() {
        // If we're already not recording, prevent this from running.
        if (!isRecording)
            return;

        isRecording = false;
    }

    /**
     * Create the correct notification depending on whether we're currently recording.
     * @return The notification.
     */
    private Notification getNotification() {
        return new Notification.Builder(this)
                .setContentTitle(getString(isRecording ? R.string.recording_notification_title_active : R.string.recording_notification_title_inactive))
                .setContentText(getString(isRecording ? R.string.recording_notification_content_active : R.string.recording_notification_content_inactive))
                .setSmallIcon(isRecording ? R.drawable.icon_mic : R.drawable.icon_mic_off).build();
    }

    /**
     * Update the notification that has already been created.
     */
    private void updateNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    /**
     * Initialise PubNub to we can receive trigger events.
     */
    private void initialisePubNub() {
        // Initialise PN
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(getString(R.string.pubnub_subscribe_key));
        pnConfiguration.setSecure(false);

        // Create PubNub and subscribe to channels
        pn = new PubNub(pnConfiguration);
        updateChannelSubscriptions(pn);

        // Add subscribe callback
        pn.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                switch (status.getOperation()) {
                    case PNSubscribeOperation:
                    case PNUnsubscribeOperation:
                        switch (status.getCategory()) {
                            case PNConnectedCategory:
                                Log.d(TAG, "PN connected successfully!");
                                break;
                            case PNReconnectedCategory:
                                Log.d(TAG, "PN reconnected!");
                                break;
                            case PNDisconnectedCategory:
                                Log.d(TAG, "PN disconnected.");
                                break;
                            case PNUnexpectedDisconnectCategory:
                                Log.d(TAG, "PN unexpected disconnection.");
                                break;
                            case PNAccessDeniedCategory:
                                Log.d(TAG, "PN access denied.");
                                break;
                        }

                    case PNHeartbeatOperation:
                        if (status.isError()) {
                            // There was an error with the heartbeat operation, handle here
                            Log.d(TAG, "PN heartbeat error!");
                        } else {
                            // heartbeat operation was successful
                        }
                        break;
                    default: {
                        // Encountered unknown status type
                    }
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                // Any message sets triggered to true
                // TODO: Parse message
                isTriggered = true;
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
    }

    /**
     * Update PubNub's channel subscriptions when settings change.
     * @param pn The PubNub instance.
     */
    private void updateChannelSubscriptions(PubNub pn) {
        // Unsubscribe from previous channels first
        if (subscribedChannels != null) {
            pn.unsubscribe().channels(subscribedChannels).execute();
        }

        // Subsrcibe to new channels
        subscribedChannels = new ArrayList<String>(Preferences.getTriggersList(this));
        pn.subscribe().channels(subscribedChannels.size() == 0 ? Arrays.asList("trigger_test") : subscribedChannels).execute();
        Log.d(TAG, String.format("PubNub subscribed to %d trigger channel%s.", subscribedChannels.size(), subscribedChannels.size() == 1 ? "" : "s"));
    }

    /**
     * Convert an short array to a byte array.
     * @param sData The short array.
     * @return The byte array.
     */
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    /**
     * Handle the audio recording thread. Loops until we stop recording.
     * First records a circular buffer, then when that function returns,
     * check if we need to keep recording a post-trigger audio buffer.
     */
    private void record() {
        Log.d(TAG, "Recording thread started.");

        // Keep this going while we're recording.
        while (isRecording) {
            // Get recording settings.
            int circularRecordingLength = Preferences.getCircularRecordingLength(this);
            int postTriggerRecordingLength = Preferences.getPostTriggerRecordingLength(this);

            // If circular recording is enabled, start it...
            short[] circularBuffer = null;
            if (circularRecordingLength > 0)
                circularBuffer = getCircularBuffer(circularRecordingLength);

            // If circular recording ended because the recording was disabled, then just end it.
            if (!isRecording)
                break;

            // Prevent the trigger logic from starting unless we've been triggered
            // (this is in case the circular buffer is disabled)
            while (!isTriggered && isRecording) {}

            // Again, prevent stalling if recording was disabled.
            if (!isRecording)
                break;
          
            // Generate file name
            Date recordingDate = new Date();
            String filePath = getRecordingPath(recordingDate);
            Log.d(TAG, "Saving new recording at " + filePath);

            // Create the encoder
            Encoder encoder = new Encoder(sampleRate, 2, 256, sampleRate, shortChunkSize);
            encoder.start(filePath);

            // Check if we have to save the circular buffer
            if (circularBuffer != null) {
                Log.d(TAG, String.format("Recorded %d seconds of circular buffer.", circularBuffer.length / numChannels / sampleRate));
                encoder.queueBuffer(circularBuffer);
            } else
                Log.d(TAG, "No circular buffer to save.");

            Log.d(TAG, String.format("Recording %d seconds of chunked post-trigger audio...", postTriggerRecordingLength * 60));

            // Begin recording post-trigger audio...
            int numSamples = sampleRate * numChannels * postTriggerRecordingLength * 60;
            int samplesRecorded = 0;

            while (isRecording && samplesRecorded < numSamples) {
                // Write audio data in chunks
                short postTriggerData[] = new short[shortChunkSize];
                int count = recorder.read(postTriggerData, 0, shortChunkSize);
                samplesRecorded += count;

                // Encode the chunk
                encoder.queueBuffer(postTriggerData);
            }

            // Save recording to DB
            if (isRecording) {
                AppDatabase.getInstance(getApplicationContext()).recordingDao().insertAll(new Recording((circularRecordingLength + postTriggerRecordingLength) * 60, new Date(), filePath));
            }

            // Stop encoder
            encoder.finish();

            // Set triggered flag back to false
            Log.d(TAG, "Done recording!");
            isTriggered = false;
        }

        // When exiting, stop the recording.
        Log.d(TAG, "Recording thread exiting.");
        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;
    }

    private String getRecordingPath(Date recordingDate) {
        File parent = new File(getApplicationContext().getExternalFilesDir(null), getString(R.string.recordings_folder_name));
        if (!parent.exists())
            parent.mkdirs();

        return new File(parent, String.format("recording_%s.mp3", Utils.getISODate(recordingDate).replace(':', '-'))).getAbsolutePath();
    }

    /**
     * Record a circular audio buffer and rearrange the buffer correctly once the recording
     * has ended.
     */
    private short[] getCircularBuffer(int circularRecordingLength) {
        // Create a buffer of the right length.
        short[] circularBuffer = new short[sampleRate * numChannels * circularRecordingLength * 60];

        // Initialise variables for the position in the buffer and whether the buffer has been fully wrapped.
        int bufferPosition = 0;
        boolean bufferWrapped = false;

        // Record the circular buffer while we're still recording and not yet triggered.
        while (isRecording && !isTriggered) {
            // Find the correct number of bytes to record
            int bufferSize = shortChunkSize;
            if (bufferPosition + bufferSize >= circularBuffer.length)
                bufferSize = circularBuffer.length - bufferPosition;

            // Write those bytes into the buffer and increment the counter
            int count = recorder.read(circularBuffer, bufferPosition, bufferSize);
            bufferPosition += count;

            // Ensure we know when the buffer has been completely filled
            if (bufferPosition >= circularBuffer.length) {
                bufferWrapped = true;
                bufferPosition -= circularBuffer.length;
            }
        }

        // If recording was stopped halfway through, and we weren't triggered, discard the whole thing
        if (!isRecording) {
            Log.d(TAG, "Recording disabled during circular buffer capture. Discarding...");
            return null;
        }

        // Rearrange the buffer if it has been wrapped
        short rearrangedBuffer[];
        if (bufferWrapped) {
            // Make it the maximum length
            rearrangedBuffer = new short[sampleRate * numChannels * circularRecordingLength * 60];

            for (int i = bufferPosition; i < circularBuffer.length; i++) {
                rearrangedBuffer[i - bufferPosition] = circularBuffer[i];
            }

            for (int i = 0; i < bufferPosition; i++) {
                rearrangedBuffer[i + (circularBuffer.length - bufferPosition)] = circularBuffer[i];
            }
        } else {
            // Make it the correct length
            rearrangedBuffer = new short[bufferPosition + 1];
            for (int i = 0; i < bufferPosition; i++) {
                rearrangedBuffer[i] = circularBuffer[i];
            }
        }

        return rearrangedBuffer;
    }

    /**
     * Handle the service being stopped.
     */
    @Override
    public void onDestroy() {
        stopRecording();

        // Unsubscribe from events
        EventBus.getDefault().unregister(this);

        // Stop monitoring
        isMonitoring = false;

        // Handle PN
        pn.unsubscribe().channels(subscribedChannels).execute();
        pn = null;

        Log.d(TAG, "Foreground service stopped.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
