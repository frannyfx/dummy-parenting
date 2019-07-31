package com.example.dummyparenting;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;

import java.util.Date;

@Database(entities = {Recording.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract RecordingDao recordingDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "dummy-db").build();
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public static class DateConverter {
        @TypeConverter
        public static Date toDate(Long dateLong){
            return dateLong == null ? null: new Date(dateLong);
        }

        @TypeConverter
        public static Long fromDate(Date date){
            return date == null ? null : date.getTime();
        }
    }
}