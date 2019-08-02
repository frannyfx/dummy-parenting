package com.example.dummyparenting;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.io.File;
import java.util.List;

@Dao
public abstract class RecordingDao {
    public static final String TAG = "recording_dao";

    @Query("SELECT * FROM recording ORDER BY recording_date DESC")
    abstract LiveData<List<Recording>> getByDate();

    @Query("SELECT * FROM recording WHERE recording_id = :id")
    abstract LiveData<Recording> getById(int id);

    @Insert
    abstract void insertAll(Recording... recordings);

    @Delete
    abstract void delete(Recording recording);

    @Update
    abstract void update(Recording... users);

    /**
     * Delete the recording along with its file
     */
    public void deleteWithFile(Recording recording) {
        // Delete file
        File file = new File(recording.filePath);
        Log.d(TAG, String.format("File deleted: %b", file.delete()));
        delete(recording);
    }
}