package com.example.dummyparenting;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class TimeSlotDao {
    @Query("SELECT * FROM timeslot ORDER BY start_time ASC")
    abstract LiveData<List<TimeSlot>> getByTime();

    @Query("SELECT * FROM timeslot WHERE slot_id = :id")
    abstract LiveData<TimeSlot> getById(int id);

    @Insert
    abstract void insertAll(TimeSlot... timeSlots);

    @Delete
    abstract void delete(TimeSlot timeSlot);

    @Update
    abstract void update(TimeSlot... timeSlots);
}
