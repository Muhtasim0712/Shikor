package com.shikor.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.shikor.app.R;
import com.shikor.app.ui.main.MainActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.util.Prefs;

/**
 * NextEventWidgetProvider - Home screen AppWidget provider displaying a scrollable list
 * of upcoming family events (Birthdays, Anniversaries, Remembrance days).
 *
 * CONCEPT: AppWidgetProvider, RemoteViews, RemoteViewsService collection adapter, and PendingIntent templates.
 */
public class NextEventWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_REFRESH = "com.shikor.app.ACTION_WIDGET_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && (ACTION_WIDGET_REFRESH.equals(intent.getAction()) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction()))) {
            Context appContext = context.getApplicationContext();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
            ComponentName provider = new ComponentName(appContext, NextEventWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetListView);
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(appContext, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        WidgetUpdater.scheduleDailyRefresh(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetUpdater.cancelDailyRefresh(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Prefs prefs = Prefs.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_next_event);

        // Set click intent for Reload button
        Intent refreshIntent = new Intent(context, NextEventWidgetProvider.class);
        refreshIntent.setAction(ACTION_WIDGET_REFRESH);
        PendingIntent refreshPi = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 99, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.ivWidgetReload, refreshPi);

        // Check if user is signed in
        if (prefs.getUserId() == null) {
            views.setViewVisibility(R.id.tvWidgetEmpty, View.VISIBLE);
            views.setTextViewText(R.id.tvWidgetEmpty, context.getString(R.string.widget_signed_out));
            views.setViewVisibility(R.id.widgetListView, View.GONE);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.tvWidgetEmpty, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        views.setViewVisibility(R.id.tvWidgetEmpty, View.GONE);
        views.setViewVisibility(R.id.widgetListView, View.VISIBLE);

        // Set up RemoteViewsService intent for the collection adapter
        Intent serviceIntent = new Intent(context, NextEventRemoteViewsService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.widgetListView, serviceIntent);
        views.setEmptyView(R.id.widgetListView, R.id.tvWidgetEmpty);

        // Set pending intent template for list items
        Intent clickIntent = new Intent(context, PersonDetailActivity.class);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
