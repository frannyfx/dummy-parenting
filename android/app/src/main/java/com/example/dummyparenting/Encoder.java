package com.example.dummyparenting;

import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private static final String TAG = "encoder";

    // Vars
    private boolean isEncoding = false;
    private Thread encodingThread;

    // Encoder properties
    private AndroidLame encoder;
    private int sampleRate;
    private int outChannels;
    private int outBitrate;
    private int outSampleRate;
    private int chunkSize;

    // Output properties
    String path;

    // Chunks
    private List<short[]> chunksToEncode;

    public Encoder(int sampleRate, int outChannels, int outBitrate, int outSampleRate, int chunkSize) {
        this.sampleRate = sampleRate;
        this.outChannels = outChannels;
        this.outBitrate = outBitrate;
        this.outSampleRate = outSampleRate;
        this.chunkSize = chunkSize;

        // Initialise list of buffers
        chunksToEncode = new ArrayList<>();
    }

    public void start(String path) {
        if (isEncoding)
            return;

        // Initialise encoder
        encoder = new LameBuilder()
                .setInSampleRate(sampleRate)
                .setOutChannels(outChannels)
                .setOutBitrate(outBitrate)
                .setOutSampleRate(outSampleRate).build();

        this.path = path;
        isEncoding = true;
        chunksToEncode.clear();

        // Start encoding thread
        encodingThread = new Thread(new Runnable() {
            public void run() {
                encode();
            }
        }, "Encoding thread");

        encodingThread.start();
    }

    public void encode() {
        Log.d(TAG, "Encoding thread starting...");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);

            while (isEncoding || chunksToEncode.size() > 0) {
                // Make a list so we know which ones to delete
                List<short[]> encodedChunks = new ArrayList<>();

                // Loop through all the chunks
                for (int i = 0; i < chunksToEncode.size(); i++) {
                    short[] buffer = chunksToEncode.get(i);

                    byte[] mp3Buffer = new byte[(int)(7200 + buffer.length * 2 * 1.25)];
                    int bytesEncoded = encoder.encodeBufferInterLeaved(buffer, buffer.length / outChannels, mp3Buffer);
                    if (bytesEncoded < 0)
                        throw new IOException("Encoding failed!");

                    encodedChunks.add(buffer);
                    outputStream.write(mp3Buffer, 0, bytesEncoded);
                }

                // Remove encoded items
                chunksToEncode.removeAll(encodedChunks);
            }

            // Flush last bytes
            Log.d(TAG, "Flushing last bytes...");
            byte[] lastBytes = new byte[(int)(7200 + chunkSize * 2 * 1.25)];
            int numBytes = encoder.flush(lastBytes);
            if (numBytes > 0)
                outputStream.write(lastBytes, 0, numBytes);

            // Close the stream
            outputStream.close();
            Log.d(TAG, String.format("Finished encoding output. %d bytes flushed last", numBytes));
        } catch (IOException e) {
            Log.d(TAG, "Failed to write encoded output stream");
            e.printStackTrace();
        }

        Log.d(TAG, "Encoding thread closing!");
    }

    public void queueBuffer(short[] buffer) {
        chunksToEncode.add(buffer);
    }

    public void finish() {
        isEncoding = false;
    }
}
