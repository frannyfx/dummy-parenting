package com.example.dummyparenting;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TimeSlot {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "slot_id")
    public int slotId;

    @ColumnInfo(name = "start_time")
    public int startTime;

    @ColumnInfo(name = "end_time")
    public int endTime;

    public TimeSlot() {}

    public TimeSlot(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
