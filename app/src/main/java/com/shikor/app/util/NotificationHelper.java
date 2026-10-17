package com.shikor.app.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.shikor.app.R;
import com.shikor.app.receiver.ReminderActionReceiver;
import com.shikor.app.ui.person.PersonDetailActivity;

/**
 * NotificationHelper - Configures system notification channels and dispatches
 * rich family reminder notifications with interactive actions (Wish/Call, Snooze, Dismiss).
 *
 * CONCEPT: Notification Channels, NotificationCompat.Builder, and Notification Actions with PendingIntents.
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static final String CHANNEL_REMINDERS_ID = "shikor_reminders";
    public static final String CHANNEL_RECORDING_ID = "shikor_audio_recording";

    /**
     * Creates required Notification Channels for Android 8.0 (API 26) and above.
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
            remindersChannel.setLightColor(ContextCompat.getColor(context, R.color.tertiary));
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

    /**
     * Builds and shows an interactive reminder notification with Action buttons.
     */
    public static void showReminderNotification(Context context, String personId, String personName,
                                                String title, String message, String phoneNumber,
                                                int notificationId, String eventType) {
        createNotificationChannels(context);

        // Content Intent: Opens PersonDetailActivity on notification tap
        Intent contentIntent = new Intent(context, PersonDetailActivity.class);
        contentIntent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, personId);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        // Action 1: Wish / Call (if phone number is available)
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber.trim()));
            PendingIntent callPendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId + 100,
                    callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(android.R.drawable.ic_menu_call, "Wish / Call", callPendingIntent);
        }

        // Action 2: Snooze (15 Minutes)
        Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class);
        snoozeIntent.setAction(ReminderActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_PERSON_ID, personId);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_PERSON_NAME, personName);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_TITLE, title);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_MESSAGE, message);
        snoozeIntent.putExtra(ReminderActionReceiver.EXTRA_PHONE_NUMBER, phoneNumber);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 200,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze", snoozePendingIntent);

        // Action 3: Dismiss
        Intent dismissIntent = new Intent(context, ReminderActionReceiver.class);
        dismissIntent.setAction(ReminderActionReceiver.ACTION_DISMISS);
        dismissIntent.putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 300,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }
}
