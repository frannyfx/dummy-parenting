package com.example.dummyparenting;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    /**
     * Check whether a specific service is currently running.
     */
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }

    public static String getNiceDate(Date date) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy' at 'HH:mm");
        return df.format(date);
    }

    public static String getISODate(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public static void showTextInputDialog(Context context, TextInputDialogResultListener listener, int dialogId, String title, String message, String positiveButtonString, String negativeButtonString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        // Create input container
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);

        // Create input
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setLayoutParams(params);

        // Add input to container and set the view
        container.addView(input);
        builder.setView(container);

        // Setup the buttons
        builder.setPositiveButton(positiveButtonString, (DialogInterface dialog, int which) -> {
            listener.onTextInputDialogResult(dialogId, input.getText().toString());

        });

        builder.setNegativeButton(negativeButtonString, (DialogInterface dialog, int which) -> {
            dialog.cancel();
            listener.onTextInputDialogResult(dialogId, null);
        });

        builder.show();
    }

    public static void showYNDialog(Context context, YNDialogResultListener listener, int dialogId, String title, String message, String positiveButtonString, String negativeButtonString) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonString, (DialogInterface dialog, int which) -> {
                    listener.onYNDialogResult(dialogId, true);
                })
                .setNegativeButton(negativeButtonString, (DialogInterface dialog, int which) -> {
                    dialog.cancel();
                    listener.onYNDialogResult(dialogId, false);
                })
                .show();
    }

    public static void showErrorDialog(Context context, String title, String message, String positiveButtonString) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.drawable.icon_error)
                .setMessage(message)
                .setPositiveButton(positiveButtonString, null)
                .show();
    }


    public static void runInBackground(Runnable runnable) {
        Thread thread = new Thread(runnable, "Background thread");
        thread.start();
    }
}
