package com.shikor.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * NotificationHelper - Configures system notification channels required by Shikor.
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static final String CHANNEL_REMINDERS_ID = "shikor_reminders";
    public static final String CHANNEL_RECORDING_ID = "shikor_audio_recording";

    /**
     * CONCEPT: Notification channels setup for Android 8.0 (API 26) and above
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                Log.w(TAG, "NotificationManager not available");
                return;
            }

            // Reminders Channel (Birthdays, Anniversaries, Remembrance/Milad)
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS_ID,
                    "Family Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription("Notifications for birthdays, anniversaries, and remembrance days");
            remindersChannel.enableLights(true);
            remindersChannel.setLightColor(androidx.core.content.ContextCompat.getColor(context, com.shikor.app.R.color.tertiary));
            remindersChannel.enableVibration(true);
            manager.createNotificationChannel(remindersChannel);

            // Audio Recording Foreground Service Channel
            NotificationChannel recordingChannel = new NotificationChannel(
                    CHANNEL_RECORDING_ID,
                    "Audio Recording Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            recordingChannel.setDescription("Ongoing status notification while recording elder stories");
            recordingChannel.setShowBadge(false);
            manager.createNotificationChannel(recordingChannel);

            Log.d(TAG, "Notification channels registered successfully");
        }
    }
}
