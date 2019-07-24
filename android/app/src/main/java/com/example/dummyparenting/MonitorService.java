package com.example.dummyparenting;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

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

    private int shortChunkSize = 1024;
    private int sampleRate = 44100;
    private int numChannels = 2;

    // PubNub
    private PubNub pn;

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

    private void startMonitoring() {
        isMonitoring = true;
        monitoringThread = new Thread(new Runnable() {
            public void run() {
                monitor();
            }
        }, "Monitor thread");

        monitoringThread.start();
    }

    private void monitor() {
        while (isMonitoring) {
            // Get preferences
            recordingEnabled = Preferences.getBackgroundRecordingEnabled(this);
            scheduleEnabled = Preferences.getScheduleEnabled(this);
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
     * @param event
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

        // Set recording flag to true and start the thread.
        isRecording = true;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, shortChunkSize * numChannels);
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

    private Notification getNotification() {
        return new Notification.Builder(this)
                .setContentTitle(isRecording ? "DummyParenting is active" : "DummyParenting is waiting")
                .setContentText(isRecording ? "Your microphone is being monitored." : "Waiting for schedule...")
                .setSmallIcon(isRecording ? R.drawable.ic_mic_black_24dp : R.drawable.ic_mic_off_black_24dp).build();
    }

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
        pnConfiguration.setSubscribeKey("sub-c-a663fb80-ad59-11e9-a87a-b2acb6d6da6e");
        pnConfiguration.setSecure(false);

        pn = new PubNub(pnConfiguration);
        pn.subscribe().channels(Arrays.asList("trigger_test")).execute();
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

            // Prepare to save audio buffers
            String filePath = "/sdcard/dummyparenting_buffer.pcm";
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(filePath);

                // Save already recorded circular buffer
                if (circularBuffer != null) {
                    Log.d(TAG, String.format("Recorded %d seconds of circular buffer.", circularBuffer.length / sampleRate / numChannels));

                    byte circularBytes[] = short2byte(circularBuffer);
                    outputStream.write(circularBytes);
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

                    // Convert to bytes
                    byte postTriggerBytes[] = short2byte(postTriggerData);
                    outputStream.write(postTriggerBytes);
                }


                // Close audio buffer.
                outputStream.close();
            } catch (Exception e) {
                // If any part of the process goes wrong, prevent crashing and log it.
                Log.d(TAG, "Unable to save audio buffers.");
                e.printStackTrace();
            }

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
            rearrangedBuffer = new short[sampleRate * circularRecordingLength * numChannels];

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
        pn.unsubscribe().channels(Arrays.asList("trigger_test")).execute();
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