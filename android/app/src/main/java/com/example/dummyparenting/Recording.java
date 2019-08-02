package com.example.dummyparenting;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;

@Entity
public class Recording implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recording_id")
    public int recordingId;

    @ColumnInfo(name = "recording_title")
    public String recordingTitle;

    @ColumnInfo(name = "recording_length")
    public int recordingLength;

    @TypeConverters(AppDatabase.DateConverter.class)
    @ColumnInfo(name = "recording_date")
    public Date recordingDate;

    @ColumnInfo(name = "file_path")
    public String filePath;

    public Recording() {

    }

    public Recording (int recordingLength, Date recordingDate, String filePath) {
        this.recordingLength = recordingLength;
        this.recordingDate = recordingDate;
        this.filePath = filePath;
    }

    public String getTitle() {
        if (recordingTitle == null)
            return "Recording #" + recordingId;

        return recordingTitle;
    }
}
