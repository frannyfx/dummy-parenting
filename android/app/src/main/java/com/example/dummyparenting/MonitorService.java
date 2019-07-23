package com.example.dummyparenting;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class MonitorService extends Service {
    public static final String CHANNEL_ID = "MonitorServiceChannel";
    private static final String TAG = "dummy_monitor";

    // Audio recording
    private AudioRecord recorder;
    private Thread recordingThread;
    private boolean isRecording = false;
    private boolean isTriggered = false;

    private short[] circularBuffer;

    private int shortChunkSize = 1024;
    private int sampleRate = 44100;
    private int numChannels = 2;
    private int circularRecordingLength = 5;
    private int triggeredRecordingLength = 10;

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
        // Create notification and start foreground service
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DummyParenting is currently monitoring")
                .setContentText("DummyParenting is recording audio.")
                .setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Initialise PubNub
        initialisePubNub();

        // Start audio recording
        startRecording();

        return START_NOT_STICKY;
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
     * Start the recording thread so we can begin recording audio.
     */
    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, shortChunkSize * numChannels);
        isRecording = true;
        recorder.startRecording();
        recordingThread = new Thread(new Runnable() {
            public void run() {
                recordAudio();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
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
    private void recordAudio() {
        Log.d(TAG, "Recording started...");

        // Until we receive a trigger, we will record a circular buffer of the desired length
        while (isRecording) {
            Log.d(TAG, "Recording circular buffer...");
            recordCircular();
            Log.d(TAG, "Done recording circular!");

            // If circular ended because recording was stopped, simply end it.
            if (!isRecording)
                break;

            // Setup saving
            String filePath = "/sdcard/dummyparenting_buffer.pcm";
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(filePath);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to save circular buffer.");
                return;
            }

            // Append already recorded circular buffer if we're triggered
            if (isTriggered) {
                // Write the output
                byte data[] = short2byte(circularBuffer);
                try {
                    outputStream.write(data);
                } catch (IOException e) {
                    Log.e(TAG, "Writing circular buffer failed.");
                }
            }

            Log.d(TAG, "Recording triggered audio...");

            // If we've received a trigger and we're still recording, save and add to buffer
            int sampleCount = sampleRate * triggeredRecordingLength * numChannels;
            int samplesRecorded = 0;

            while (isRecording && samplesRecorded < sampleCount) {
                short audioData[] = new short[shortChunkSize];
                int count = recorder.read(audioData, 0, shortChunkSize);
                samplesRecorded += count;

                try {
                    byte data[] = short2byte(audioData);
                    outputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "Done recording!");

            // Close the buffer
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to close output stream.");
            }

            isTriggered = false;
        }
    }

    /**
     * Record a circular audio buffer and rearrange the buffer correctly once the recording
     * has ended.
     */
    private void recordCircular() {
        circularBuffer = new short[sampleRate * circularRecordingLength * numChannels];
        int bufferPosition = 0;
        boolean bufferWrapped = false;

        while (isRecording && !isTriggered) {
            int bufferSize = shortChunkSize;
            if (bufferPosition + bufferSize >= circularBuffer.length)
                bufferSize = circularBuffer.length - bufferPosition;

            int count = recorder.read(circularBuffer, bufferPosition, bufferSize);
            bufferPosition += count;

            // Ensure we know when the buffer has been completely filled
            if (bufferPosition >= circularBuffer.length) {
                bufferWrapped = true;
                bufferPosition -= circularBuffer.length;
            }
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

        circularBuffer = rearrangedBuffer;
    }

    /**
     * Create the notification channel needed to post the foreground service notification.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Handle the service being stopped.
     */
    @Override
    public void onDestroy() {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;

        // Handle PN
        pn.unsubscribe().channels(Arrays.asList("trigger_test")).execute();
        pn = null;

        Log.d(TAG, "Recording stopped.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
