package com.shikor.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.shikor.app.R;
import com.shikor.app.provider.FamilyProvider;
import com.shikor.app.ui.main.MainActivity;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * NextEventWidgetProvider - Home screen AppWidget provider displaying upcoming
 * family events (Birthdays, Anniversaries, Remembrance days).
 *
 * CONCEPT: AppWidgetProvider, RemoteViews, ContentResolver queries, PendingIntents, and background goAsync processing.
 */
public class NextEventWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "NextEventWidgetProvider";
    public static final String ACTION_WIDGET_REFRESH = "com.shikor.app.ACTION_WIDGET_REFRESH";

    private static class FamilyEventModel implements Comparable<FamilyEventModel> {
        String personId;
        String personName;
        String eventType;
        long daysUntil;
        String dateStr;
        String photoPath;
        String gender;
        boolean isDeceased;

        @Override
        public int compareTo(FamilyEventModel other) {
            return Long.compare(this.daysUntil, other.daysUntil);
        }
    }

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

        // Check if user is signed in
        if (prefs.getUserId() == null) {
            views.setViewVisibility(R.id.tvWidgetEmpty, View.VISIBLE);
            views.setTextViewText(R.id.tvWidgetEmpty, context.getString(R.string.widget_signed_out));
            views.setViewVisibility(R.id.layoutWidgetEvent1, View.GONE);
            views.setViewVisibility(R.id.layoutWidgetEvent2, View.GONE);
            views.setViewVisibility(R.id.tvWidgetCountdown, View.GONE);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.tvWidgetEmpty, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<FamilyEventModel> events = new ArrayList<>();
            try (Cursor cursor = context.getContentResolver().query(FamilyProvider.EVENTS_UPCOMING_URI, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idIdx = cursor.getColumnIndex("id");
                    int nameIdx = cursor.getColumnIndex("fullName");
                    int birthIdx = cursor.getColumnIndex("birthDate");
                    int deathIdx = cursor.getColumnIndex("deathDate");
                    int marriageIdx = cursor.getColumnIndex("marriageDate");
                    int deceasedIdx = cursor.getColumnIndex("isDeceased");
                    int photoIdx = cursor.getColumnIndex("photoPath");
                    int genderIdx = cursor.getColumnIndex("gender");

                    Calendar todayCal = Calendar.getInstance();
                    todayCal.set(Calendar.HOUR_OF_DAY, 0);
                    todayCal.set(Calendar.MINUTE, 0);
                    todayCal.set(Calendar.SECOND, 0);
                    todayCal.set(Calendar.MILLISECOND, 0);
                    int currentYear = todayCal.get(Calendar.YEAR);

                    do {
                        String id = idIdx >= 0 ? cursor.getString(idIdx) : "";
                        String fullName = nameIdx >= 0 ? cursor.getString(nameIdx) : "Relative";
                        String birthDate = birthIdx >= 0 ? cursor.getString(birthIdx) : null;
                        String deathDate = deathIdx >= 0 ? cursor.getString(deathIdx) : null;
                        String marriageDate = marriageIdx >= 0 ? cursor.getString(marriageIdx) : null;
                        boolean isDeceased = deceasedIdx >= 0 && cursor.getInt(deceasedIdx) == 1;
                        String photoPath = photoIdx >= 0 ? cursor.getString(photoIdx) : null;
                        String gender = genderIdx >= 0 ? cursor.getString(genderIdx) : "M";

                        // 1. Birthday (if living)
                        if (!isDeceased && birthDate != null && !birthDate.trim().isEmpty()) {
                            FamilyEventModel ev = calculateNextEvent(id, fullName, birthDate, "🎂 Birthday", currentYear, todayCal, photoPath, gender, false);
                            if (ev != null) events.add(ev);
                        }

                        // 2. Anniversary (if marriage date exists)
                        if (marriageDate != null && !marriageDate.trim().isEmpty()) {
                            FamilyEventModel ev = calculateNextEvent(id, fullName, marriageDate, "💍 Anniversary", currentYear, todayCal, photoPath, gender, false);
                            if (ev != null) events.add(ev);
                        }

                        // 3. Remembrance (if deceased and death date exists)
                        if (isDeceased && deathDate != null && !deathDate.trim().isEmpty()) {
                            FamilyEventModel ev = calculateNextEvent(id, fullName, deathDate, "🕊️ Remembrance", currentYear, todayCal, photoPath, gender, true);
                            if (ev != null) events.add(ev);
                        }

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying events for widget: " + e.getMessage(), e);
            }

            Collections.sort(events);

            AppExecutors.getInstance().mainThread().execute(() -> {
                boolean hideNames = prefs.isHideNamesInWidgetAndNotifications();

                if (events.isEmpty()) {
                    views.setViewVisibility(R.id.tvWidgetEmpty, View.VISIBLE);
                    views.setTextViewText(R.id.tvWidgetEmpty, context.getString(R.string.widget_no_events));
                    views.setViewVisibility(R.id.layoutWidgetEvent1, View.GONE);
                    views.setViewVisibility(R.id.layoutWidgetEvent2, View.GONE);
                    views.setViewVisibility(R.id.tvWidgetCountdown, View.GONE);

                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.tvWidgetEmpty, pi);
                } else {
                    views.setViewVisibility(R.id.tvWidgetEmpty, View.GONE);
                    views.setViewVisibility(R.id.layoutWidgetEvent1, View.VISIBLE);
                    views.setViewVisibility(R.id.tvWidgetCountdown, View.VISIBLE);

                    // Event 1
                    FamilyEventModel ev1 = events.get(0);
                    String name1 = hideNames ? "Relative" : ev1.personName;
                    views.setTextViewText(R.id.tvWidgetName1, name1);
                    views.setTextViewText(R.id.tvWidgetDetails1, ev1.eventType + " • " + ev1.dateStr);

                    String countdownText = ev1.daysUntil == 0 ? "Today" : (ev1.daysUntil == 1 ? "in 1 day" : "in " + ev1.daysUntil + " days");
                    views.setTextViewText(R.id.tvWidgetCountdown, countdownText);

                    // Avatar 1
                    if (ev1.photoPath != null && !ev1.photoPath.isEmpty() && new File(ev1.photoPath).exists()) {
                        try {
                            Bitmap bmp = BitmapFactory.decodeFile(ev1.photoPath);
                            if (bmp != null) {
                                views.setImageViewBitmap(R.id.ivWidgetAvatar1, getCircularBitmap(bmp));
                            } else {
                                views.setImageViewResource(R.id.ivWidgetAvatar1, android.R.drawable.ic_menu_my_calendar);
                            }
                        } catch (Exception ignored) {
                            views.setImageViewResource(R.id.ivWidgetAvatar1, android.R.drawable.ic_menu_my_calendar);
                        }
                    } else {
                        views.setImageViewResource(R.id.ivWidgetAvatar1, android.R.drawable.ic_menu_my_calendar);
                    }

                    // Intent for Event 1
                    Intent intent1 = new Intent(context, PersonDetailActivity.class);
                    intent1.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, ev1.personId);
                    PendingIntent pi1 = PendingIntent.getActivity(context, appWidgetId * 10 + 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(R.id.layoutWidgetEvent1, pi1);

                    // Event 2 (if available)
                    if (events.size() > 1) {
                        views.setViewVisibility(R.id.layoutWidgetEvent2, View.VISIBLE);
                        FamilyEventModel ev2 = events.get(1);
                        String name2 = hideNames ? "Relative" : ev2.personName;
                        views.setTextViewText(R.id.tvWidgetName2, name2);
                        String countdown2 = ev2.daysUntil == 0 ? "Today" : (ev2.daysUntil == 1 ? "in 1 day" : "in " + ev2.daysUntil + " days");
                        views.setTextViewText(R.id.tvWidgetDetails2, ev2.eventType + " • " + countdown2);

                        if (ev2.photoPath != null && !ev2.photoPath.isEmpty() && new File(ev2.photoPath).exists()) {
                            try {
                                Bitmap bmp2 = BitmapFactory.decodeFile(ev2.photoPath);
                                if (bmp2 != null) {
                                    views.setImageViewBitmap(R.id.ivWidgetAvatar2, getCircularBitmap(bmp2));
                                } else {
                                    views.setImageViewResource(R.id.ivWidgetAvatar2, android.R.drawable.ic_menu_my_calendar);
                                }
                            } catch (Exception ignored) {
                                views.setImageViewResource(R.id.ivWidgetAvatar2, android.R.drawable.ic_menu_my_calendar);
                            }
                        } else {
                            views.setImageViewResource(R.id.ivWidgetAvatar2, android.R.drawable.ic_menu_my_calendar);
                        }

                        Intent intent2 = new Intent(context, PersonDetailActivity.class);
                        intent2.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, ev2.personId);
                        PendingIntent pi2 = PendingIntent.getActivity(context, appWidgetId * 10 + 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        views.setOnClickPendingIntent(R.id.layoutWidgetEvent2, pi2);
                    } else {
                        views.setViewVisibility(R.id.layoutWidgetEvent2, View.GONE);
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            });
        });
    }

    private static FamilyEventModel calculateNextEvent(String id, String name, String inputDateStr, String eventType, int currentYear, Calendar todayCal, String photoPath, String gender, boolean isDeceased) {
        try {
            String trimmed = inputDateStr.trim();
            String[] parts = trimmed.split("[-/.]");
            if (parts.length < 2) return null;
            int month, day;
            if (parts[0].length() == 4) {
                // yyyy-MM-dd
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            } else {
                // dd-MM-yyyy
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
            }

            Calendar eventCal = Calendar.getInstance();
            eventCal.set(currentYear, month, day, 0, 0, 0);
            eventCal.set(Calendar.MILLISECOND, 0);

            // Handle leap year Feb 29 on non-leap year
            boolean isLeap = (currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0);
            if (month == Calendar.FEBRUARY && day == 29 && !isLeap) {
                eventCal.set(currentYear, Calendar.FEBRUARY, 28, 0, 0, 0);
            }

            if (eventCal.before(todayCal)) {
                int nextYear = currentYear + 1;
                boolean isNextLeap = (nextYear % 4 == 0 && nextYear % 100 != 0) || (nextYear % 400 == 0);
                eventCal.set(nextYear, month, day, 0, 0, 0);
                if (month == Calendar.FEBRUARY && day == 29 && !isNextLeap) {
                    eventCal.set(nextYear, Calendar.FEBRUARY, 28, 0, 0, 0);
                }
            }

            long diffMs = eventCal.getTimeInMillis() - todayCal.getTimeInMillis();
            long days = diffMs / (1000 * 60 * 60 * 24);

            FamilyEventModel ev = new FamilyEventModel();
            ev.personId = id;
            ev.personName = name;
            ev.eventType = eventType;
            ev.daysUntil = days;
            ev.dateStr = new SimpleDateFormat("MMM d", Locale.getDefault()).format(eventCal.getTime());
            ev.photoPath = photoPath;
            ev.gender = gender;
            ev.isDeceased = isDeceased;
            return ev;
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, diameter, diameter);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (diameter - width) / 2f, (diameter - height) / 2f, paint);
        return output;
    }
}
