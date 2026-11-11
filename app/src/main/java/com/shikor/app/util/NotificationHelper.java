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
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.receiver.MemoryNotificationReceiver;
import com.shikor.app.receiver.ReminderActionReceiver;
import com.shikor.app.ui.person.PersonDetailActivity;

import java.io.File;

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
    public static final String CHANNEL_MEMORIES_ID = "shikor_memories";

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

            // On This Day Memories Channel
            NotificationChannel memoriesChannel = new NotificationChannel(
                    CHANNEL_MEMORIES_ID,
                    "Memory Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            memoriesChannel.setDescription("Notifications for On This Day family memories");
            memoriesChannel.enableLights(true);
            memoriesChannel.setLightColor(ContextCompat.getColor(context, R.color.tertiary));
            memoriesChannel.enableVibration(true);
            manager.createNotificationChannel(memoriesChannel);

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
                .setSmallIcon(R.drawable.ic_stat_shikor)
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

    /**
     * Builds and shows an "On this day" memory notification with story details and Open family action.
     */
    public static void showOnThisDayNotification(Context context, com.shikor.app.data.local.entities.StoryEntity story,
                                                 String personName, int yearsAgo, int extraCount, int notificationId) {
        createNotificationChannels(context);

        Intent contentIntent = new Intent(context, com.shikor.app.ui.story.StoryDetailActivity.class);
        contentIntent.putExtra(com.shikor.app.ui.story.StoryDetailActivity.EXTRA_STORY_ID, story.getId());
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent mainIntent = new Intent(context, com.shikor.app.ui.main.MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 500,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = context.getString(R.string.notification_on_this_day_title, yearsAgo);
        String body = story.getBody() != null ? story.getBody() : "";
        String firstLine = body.isEmpty() ? (story.getTitle() != null ? story.getTitle() : "") : body.split("\n")[0];
        String contentText = firstLine + " (" + personName + ")";

        StringBuilder bigText = new StringBuilder();
        if (story.getTitle() != null && !story.getTitle().isEmpty()) {
            bigText.append(story.getTitle()).append("\n\n");
        }
        bigText.append(body);
        if (extraCount > 0) {
            bigText.append("\n\n").append(context.getString(R.string.notification_on_this_day_more_fmt, extraCount));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MEMORIES_ID)
                .setSmallIcon(R.drawable.ic_stat_shikor)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .addAction(android.R.drawable.ic_menu_agenda, context.getString(R.string.notification_action_open_family), mainPendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }

    public static final String CHANNEL_FAMILY_MEMORIES_ID = "family_memories";

    public static void createFamilyMemoriesChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_FAMILY_MEMORIES_ID,
                        "Family Memories",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications when family members add new memories");
                channel.enableLights(true);
                channel.setLightColor(ContextCompat.getColor(context, R.color.primary));
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNewMemoryNotification(Context context, MemoryEntity memory, boolean hideNames) {
        createFamilyMemoriesChannel(context);
        int notificationId = Math.abs(memory.getId().hashCode());

        Intent detailIntent = new Intent(context, com.shikor.app.ui.memory.MemoryDetailActivity.class);
        detailIntent.putExtra(com.shikor.app.ui.memory.MemoryDetailActivity.EXTRA_MEMORY_ID, memory.getId());
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent viewIntent = new Intent(context, MemoryNotificationReceiver.class);
        viewIntent.setAction(MemoryNotificationReceiver.ACTION_VIEW_MEMORY);
        viewIntent.putExtra(MemoryNotificationReceiver.EXTRA_MEMORY_ID, memory.getId());
        viewIntent.putExtra(MemoryNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent viewPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 100,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = hideNames ? "New memory in your family" : ("New memory from " + (memory.getUploaderName() != null ? memory.getUploaderName() : "Family Member"));
        String text = memory.getTitle() != null ? memory.getTitle() : "Tap to view memory";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_FAMILY_MEMORIES_ID)
                .setSmallIcon(R.drawable.ic_stat_shikor)
                .setContentTitle(title)
                .setContentText(text)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setGroup("family_memories_group")
                .addAction(android.R.drawable.ic_menu_view, "View", viewPendingIntent);

        File photoDir = new File(context.getFilesDir(), "memories/" + memory.getId());
        File thumb = new File(photoDir, "photo_0.jpg");
        if (thumb.exists()) {
            try {
                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(thumb.getAbsolutePath());
                if (bmp != null) {
                    builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bmp).bigLargeIcon((android.graphics.Bitmap) null));
                }
            } catch (Exception ignored) {}
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }

    public static void showGroupedMemoryNotification(Context context, int count, boolean hideNames) {
        createFamilyMemoriesChannel(context);
        int summaryId = 999999;

        Intent mainIntent = new Intent(context, com.shikor.app.ui.main.MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mainPending = PendingIntent.getActivity(
                context,
                summaryId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = hideNames ? "New memories in your family" : (count + " new memories from your family");

        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context, CHANNEL_FAMILY_MEMORIES_ID)
                .setSmallIcon(R.drawable.ic_stat_shikor)
                .setContentTitle(title)
                .setContentText("Tap to view all family memories")
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(mainPending)
                .setAutoCancel(true)
                .setGroup("family_memories_group")
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        try {
            NotificationManagerCompat.from(context).notify(summaryId, summaryBuilder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }

    public static void showOnThisDayMemoryNotification(Context context, MemoryEntity memory,
                                                     String personName, int yearsAgo, int extraCount, int notificationId) {
        createFamilyMemoriesChannel(context);

        Intent contentIntent = new Intent(context, com.shikor.app.ui.memory.MemoryDetailActivity.class);
        contentIntent.putExtra(com.shikor.app.ui.memory.MemoryDetailActivity.EXTRA_MEMORY_ID, memory.getId());
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = context.getString(R.string.notification_on_this_day_title, yearsAgo);
        String body = memory.getDescription() != null ? memory.getDescription() : "";
        String contentText = body.isEmpty() ? (memory.getTitle() != null ? memory.getTitle() : "") : body;
        if (personName != null && !personName.isEmpty()) {
            contentText += " (" + personName + ")";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_FAMILY_MEMORIES_ID)
                .setSmallIcon(R.drawable.ic_stat_shikor)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }
}
