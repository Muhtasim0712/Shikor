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
                AlarmScheduler.scheduleOnThisDay(context);
                com.shikor.app.widget.WidgetUpdater.scheduleDailyRefresh(context);

                androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build();
                androidx.work.PeriodicWorkRequest memoryWork = new androidx.work.PeriodicWorkRequest.Builder(
                        com.shikor.app.worker.MemoryCheckWorker.class,
                        15, java.util.concurrent.TimeUnit.MINUTES
                )
                .setConstraints(constraints)
                .build();
                androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "memory_check_periodic",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        memoryWork
                );

                Log.d(TAG, "Reminders, On This Day, and Memory check periodic work rescheduled successfully after reboot.");
            }
        }
    }
}
