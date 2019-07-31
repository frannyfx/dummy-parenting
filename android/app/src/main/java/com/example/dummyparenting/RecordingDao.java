package com.example.dummyparenting;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecordingDao {
    @Query("SELECT * FROM recording ORDER BY recording_date DESC")
    LiveData<List<Recording>> getByDate();

    @Query("SELECT * FROM recording WHERE recording_id = :id")
    Recording getById(int id);

    @Insert
    void insertAll(Recording... recordings);

    @Delete
    void delete(Recording recording);
}