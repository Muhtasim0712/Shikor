package com.shikor.app.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * WidgetUpdater - Helper utility to request updates for NextEventWidgetProvider
 * and schedule daily midnight refresh alarms.
 *
 * CONCEPT: AppWidgetProvider coordination, AlarmManager periodic scheduling, and RemoteViews refresh.
 */
public class WidgetUpdater {

    private static final String TAG = "WidgetUpdater";
    private static final int WIDGET_REFRESH_REQUEST_CODE = 4001;

    public static void requestUpdate(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
            ComponentName provider = new ComponentName(appContext, NextEventWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                Intent intent = new Intent(appContext, NextEventWidgetProvider.class);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                appContext.sendBroadcast(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting widget update: " + e.getMessage(), e);
        }
    }

    public static void scheduleDailyRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 5); // 00:05 AM
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, NextEventWidgetProvider.class);
        intent.setAction(NextEventWidgetProvider.ACTION_WIDGET_REFRESH);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                WIDGET_REFRESH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Log.d(TAG, "Daily widget refresh alarm scheduled for: " + calendar.getTime());
        } catch (Exception e) {
            Log.w(TAG, "Failed to schedule widget refresh alarm: " + e.getMessage());
        }
    }

    public static void cancelDailyRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, NextEventWidgetProvider.class);
        intent.setAction(NextEventWidgetProvider.ACTION_WIDGET_REFRESH);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                WIDGET_REFRESH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Daily widget refresh alarm cancelled.");
        }
    }
}
