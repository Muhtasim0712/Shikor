package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.Prefs;

/**
 * BootReceiver - Reschedules daily reminder alarms upon device restart.
 *
 * CONCEPT: Manifest-declared BroadcastReceiver for BOOT_COMPLETED to survive system reboots.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            Log.d(TAG, "Device rebooted, verifying reminder alarm schedule...");
            if (Prefs.getInstance(context).isNotificationsEnabled()) {
                AlarmScheduler.scheduleDailyReminder(context);
                Log.d(TAG, "Daily reminders rescheduled successfully after reboot.");
            }
        }
    }
}
