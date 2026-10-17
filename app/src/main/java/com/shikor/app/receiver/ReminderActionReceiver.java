package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.shikor.app.util.AlarmScheduler;

/**
 * ReminderActionReceiver - Handles interactive notification actions ('Snooze' and 'Dismiss').
 *
 * CONCEPT: BroadcastReceiver handling interactive Notification actions and Alarm rescheduling.
 */
public class ReminderActionReceiver extends BroadcastReceiver {

    public static final String ACTION_SNOOZE = "com.shikor.app.ACTION_SNOOZE";
    public static final String ACTION_DISMISS = "com.shikor.app.ACTION_DISMISS";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_PERSON_ID = "person_id";
    public static final String EXTRA_PERSON_NAME = "person_name";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId);
        }

        if (ACTION_SNOOZE.equals(action)) {
            String personId = intent.getStringExtra(EXTRA_PERSON_ID);
            String personName = intent.getStringExtra(EXTRA_PERSON_NAME);
            String title = intent.getStringExtra(EXTRA_TITLE);
            String message = intent.getStringExtra(EXTRA_MESSAGE);
            String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);

            // 15-minute snooze (900,000 ms)
            long snoozeDelayMs = 15 * 60 * 1000L;
            AlarmScheduler.scheduleSnooze(
                    context, personId, personName, title, message, phoneNumber, notificationId, snoozeDelayMs
            );

            Toast.makeText(context, "Reminder snoozed for 15 minutes", Toast.LENGTH_SHORT).show();
        }
    }
}
