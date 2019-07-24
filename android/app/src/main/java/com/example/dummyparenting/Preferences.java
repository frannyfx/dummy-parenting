package com.example.dummyparenting;

import android.content.Context;
import android.content.SharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Preferences {
    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public static boolean getBackgroundRecordingEnabled(Context context) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        return sharedPref.getBoolean(context.getString(R.string.background_recording_key), context.getResources().getBoolean(R.bool.background_recording_default));
    }

    public static void setBackgroundRecordingEnabled(Context context, boolean value) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.background_recording_key), value);
        editor.commit();

        EventBus.getDefault().post(new SettingsChangedEvent());
    }

    public static boolean getScheduleEnabled(Context context) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        return sharedPref.getBoolean(context.getString(R.string.schedule_key), context.getResources().getBoolean(R.bool.schedule_default));
    }

    public static void setScheduleEnabled(Context context, boolean value) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.schedule_key), value);
        editor.commit();

        EventBus.getDefault().post(new SettingsChangedEvent());
    }

    public static int getCircularRecordingLength(Context context) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        return sharedPref.getInt(context.getString(R.string.circular_recording_length_key), context.getResources().getInteger(R.integer.circular_recording_length_default));
    }

    public static void setCircularRecordingLength(Context context, int value) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.circular_recording_length_key), value);
        editor.commit();

        EventBus.getDefault().post(new SettingsChangedEvent());
    }

    public static int getPostTriggerRecordingLength(Context context) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        return sharedPref.getInt(context.getString(R.string.post_trigger_recording_length_key), context.getResources().getInteger(R.integer.post_trigger_recording_length_default));
    }

    public static void setPostTriggerRecordingLength(Context context, int value) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.post_trigger_recording_length_key), value);
        editor.commit();

        EventBus.getDefault().post(new SettingsChangedEvent());
    }

    public static Set<String> getTriggersList(Context context) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        return sharedPref.getStringSet(context.getString(R.string.triggers_list_key), Collections.<String>emptySet());
    }

    public static void setTriggersList(Context context, List<String> triggers) {
        SharedPreferences sharedPref = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(context.getString(R.string.triggers_list_key), new HashSet<String>(triggers));
        editor.commit();

        EventBus.getDefault().post(new SettingsChangedEvent());
    }
}
