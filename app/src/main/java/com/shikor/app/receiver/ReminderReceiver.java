package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.shikor.app.provider.FamilyProvider;
import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ReminderReceiver - BroadcastReceiver invoked by AlarmManager to check for
 * today's birthdays, wedding anniversaries, and remembrance days.
 *
 * CONCEPT: Consumes FamilyProvider (ContentProvider) via ContentResolver to query family members.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    public static final String ACTION_DAILY_REMINDER_CHECK = "com.shikor.app.ACTION_DAILY_REMINDER_CHECK";
    public static final String ACTION_SNOOZE_TRIGGER = "com.shikor.app.ACTION_SNOOZE_TRIGGER";
    public static final String ACTION_IMMEDIATE_TEST = "com.shikor.app.ACTION_IMMEDIATE_TEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        Log.d(TAG, "ReminderReceiver received action: " + action);

        // Check if this was a snoozed reminder firing
        if (intent.getBooleanExtra("is_snooze", false)) {
            String personId = intent.getStringExtra("person_id");
            String personName = intent.getStringExtra("person_name");
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            String phoneNumber = intent.getStringExtra("phone_number");
            int notificationId = intent.getIntExtra("notification_id", (int) System.currentTimeMillis());

            NotificationHelper.showReminderNotification(
                    context, personId, personName, title, message, phoneNumber, notificationId, "snooze"
            );
            return;
        }

        // Daily or Test reminder check: Query family members via FamilyProvider
        boolean isTest = ACTION_IMMEDIATE_TEST.equals(action);
        checkRemindersViaContentProvider(context, isTest);

        // Reschedule next daily check
        AlarmScheduler.scheduleDailyReminder(context);
    }

    private void checkRemindersViaContentProvider(Context context, boolean isTest) {
        Calendar todayCalendar = Calendar.getInstance();
        int todayYear = todayCalendar.get(Calendar.YEAR);
        int notificationCount = 0;
        int notificationIdBase = 2000;

        String firstPersonId = null;
        String firstPersonName = null;
        String firstPersonPhone = null;

        try (Cursor cursor = context.getContentResolver().query(FamilyProvider.CONTENT_URI, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex("id");
                int nameIdx = cursor.getColumnIndex("fullName");
                int birthIdx = cursor.getColumnIndex("birthDate");
                int deathIdx = cursor.getColumnIndex("deathDate");
                int marriageIdx = cursor.getColumnIndex("marriageDate");
                int deceasedIdx = cursor.getColumnIndex("isDeceased");
                int phoneIdx = cursor.getColumnIndex("phoneNumber");

                do {
                    String id = idIdx >= 0 ? cursor.getString(idIdx) : "";
                    String fullName = nameIdx >= 0 ? cursor.getString(nameIdx) : "Relative";
                    String birthDate = birthIdx >= 0 ? cursor.getString(birthIdx) : null;
                    String deathDate = deathIdx >= 0 ? cursor.getString(deathIdx) : null;
                    String marriageDate = marriageIdx >= 0 ? cursor.getString(marriageIdx) : null;
                    boolean isDeceased = deceasedIdx >= 0 && cursor.getInt(deceasedIdx) == 1;
                    String phoneNumber = phoneIdx >= 0 ? cursor.getString(phoneIdx) : "";

                    if (firstPersonId == null && !id.isEmpty()) {
                        firstPersonId = id;
                        firstPersonName = fullName;
                        firstPersonPhone = phoneNumber;
                    }

                    // 1. Birthday Check (Living relatives)
                    if (!isDeceased && isSameMonthAndDay(birthDate, todayCalendar)) {
                        int birthYear = parseYear(birthDate);
                        int age = birthYear > 0 ? (todayYear - birthYear) : -1;
                        String title = "🎂 Birthday: " + fullName;
                        String message = "Today is " + fullName + "'s " + (age > 0 ? (age + "th ") : "") + "birthday! Tap to send warm wishes.";
                        NotificationHelper.showReminderNotification(
                                context, id, fullName, title, message, phoneNumber, notificationIdBase++, "birthday"
                        );
                        notificationCount++;
                    }

                    // 2. Wedding Anniversary Check
                    if (isSameMonthAndDay(marriageDate, todayCalendar)) {
                        String title = "💍 Wedding Anniversary: " + fullName;
                        String message = "Happy wedding anniversary to " + fullName + "! Wish them continued love and happiness.";
                        NotificationHelper.showReminderNotification(
                                context, id, fullName, title, message, phoneNumber, notificationIdBase++, "anniversary"
                        );
                        notificationCount++;
                    }

                    // 3. Remembrance Day (Death Anniversary)
                    if (isDeceased && isSameMonthAndDay(deathDate, todayCalendar)) {
                        int deathYear = parseYear(deathDate);
                        int years = deathYear > 0 ? (todayYear - deathYear) : -1;
                        String title = "🕊️ Remembrance: " + fullName;
                        String message = "Today marks " + (years > 0 ? (years + " years of remembrance for ") : "the memory of ") + fullName + ". In loving prayer.";
                        NotificationHelper.showReminderNotification(
                                context, id, fullName, title, message, phoneNumber, notificationIdBase++, "remembrance"
                        );
                        notificationCount++;
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying FamilyProvider for reminders: " + e.getMessage(), e);
        }

        // If no milestones happened to fall on today's exact date:
        if (notificationCount == 0) {
            String testId = firstPersonId != null ? firstPersonId : "daily-digest";
            String testName = firstPersonName != null ? firstPersonName : "Family";
            String testPhone = firstPersonPhone != null ? firstPersonPhone : "";

            if (isTest) {
                NotificationHelper.showReminderNotification(
                        context,
                        testId,
                        testName,
                        "🔔 Shikor Reminder Test",
                        "Reminders are active! Today's test reminder for " + testName + ". Tap 'Wish / Call' or 'Snooze' to test actions.",
                        testPhone,
                        9999,
                        "test"
                );
            } else {
                // Regular scheduled alarm fired at user's preferred alert time!
                NotificationHelper.showReminderNotification(
                        context,
                        testId,
                        testName,
                        "🌿 Shikor Daily Digest",
                        "No family birthdays or anniversaries today. Have a wonderful, peaceful day with your loved ones!",
                        testPhone,
                        2000,
                        "daily_digest"
                );
            }
        }
    }

    private boolean isSameMonthAndDay(String dateStr, Calendar today) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        dateStr = dateStr.trim();
        String[] parts = dateStr.split("[-/.]");
        if (parts.length == 3) {
            try {
                int month, day;
                if (parts[0].length() == 4) {
                    // yyyy-MM-dd
                    month = Integer.parseInt(parts[1]);
                    day = Integer.parseInt(parts[2]);
                } else {
                    // dd-MM-yyyy
                    day = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]);
                }
                return month == (today.get(Calendar.MONTH) + 1) && day == today.get(Calendar.DAY_OF_MONTH);
            } catch (Exception ignored) {}
        }
        return false;
    }

    private int parseYear(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return -1;
        String[] parts = dateStr.trim().split("[-/.]");
        if (parts.length > 0) {
            for (String p : parts) {
                if (p.length() == 4) {
                    try {
                        return Integer.parseInt(p);
                    } catch (Exception ignored) {}
                }
            }
        }
        return -1;
    }
}
