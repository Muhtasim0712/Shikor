package com.shikor.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.shikor.app.receiver.ReminderReceiver;

import java.util.Calendar;

/**
 * AlarmScheduler - Schedules daily exact alarms for family reminders (Birthdays, Anniversaries, Remembrance)
 * and handles custom snooze alarms using AlarmManager.
 *
 * CONCEPT: AlarmManager exact alarm scheduling with setExactAndAllowWhileIdle, Doze survival, and reschedule on boot.
 */
public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    public static final int DAILY_REMINDER_REQUEST_CODE = 1001;
    public static final int SNOOZE_REMINDER_REQUEST_CODE_BASE = 5000;

    /**
     * Schedules the daily family reminder check at the user's preferred time (from Prefs).
     */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Prefs prefs = Prefs.getInstance(context);
        if (!prefs.isNotificationsEnabled()) {
            cancelDailyReminder(context);
            Log.d(TAG, "Reminders disabled in settings, daily alarm cancelled.");
            return;
        }

        int hour = prefs.getReminderHour();
        int minute = prefs.getReminderMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the scheduled time has already passed today (with a 5-second buffer), advance to tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis() + 5000) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_DAILY_REMINDER_CHECK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = calendar.getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG, "Daily reminder scheduled for: " + calendar.getTime());
        } catch (SecurityException e) {
            Log.w(TAG, "Exact alarm permission missing, falling back to setAndAllowWhileIdle: " + e.getMessage());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    /**
     * Checks if the app currently has permission to schedule exact alarms.
     */
    public static boolean canScheduleExact(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * Calculates the next daily reminder trigger time in milliseconds.
     */
    public static long getNextDailyReminderMillis(Context context) {
        Prefs prefs = Prefs.getInstance(context);
        int hour = prefs.getReminderHour();
        int minute = prefs.getReminderMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis() + 5000) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }

    /**
     * Cancels the active daily reminder alarm.
     */
    public static void cancelDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_DAILY_REMINDER_CHECK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Daily reminder alarm cancelled.");
        }
    }

    /**
     * Schedules a snooze reminder for a specific event.
     */
    public static void scheduleSnooze(Context context, String personId, String personName,
                                      String title, String message, String phoneNumber,
                                      int notificationId, long delayMs) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_SNOOZE_TRIGGER);
        intent.putExtra("is_snooze", true);
        intent.putExtra("person_id", personId);
        intent.putExtra("person_name", personName);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("phone_number", phoneNumber);
        intent.putExtra("notification_id", notificationId);

        int requestCode = SNOOZE_REMINDER_REQUEST_CODE_BASE + (notificationId % 1000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = System.currentTimeMillis() + delayMs;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG, "Snooze alarm set for " + (delayMs / 60000) + " minutes from now.");
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    /**
     * Immediately triggers a reminder check (used for user testing in settings).
     */
    public static void triggerImmediateCheck(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_IMMEDIATE_TEST);
        context.sendBroadcast(intent);
    }
}
