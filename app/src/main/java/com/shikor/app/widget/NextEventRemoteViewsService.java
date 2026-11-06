package com.shikor.app.widget;

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
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.shikor.app.R;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.provider.FamilyProvider;
import com.shikor.app.ui.person.PersonDetailActivity;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * NextEventRemoteViewsService - RemoteViewsService supplying RemoteViewsFactory
 * for the scrollable collection AppWidget.
 *
 * CONCEPT: RemoteViewsService, RemoteViewsFactory, and adapter-backed collection widgets.
 */
public class NextEventRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NextEventRemoteViewsFactory(getApplicationContext(), intent);
    }

    public static class NextEventRemoteViewsFactory implements RemoteViewsFactory {
        private static final String TAG = "NextEventRemoteViewsFactory";
        private final Context context;
        private final List<FamilyEventModel> events = new ArrayList<>();

        public NextEventRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            loadData();
        }

        @Override
        public void onDataSetChanged() {
            loadData();
        }

        private void loadData() {
            events.clear();
            Log.d(TAG, "Loading upcoming events for widget...");

            List<PersonEntity> people = new ArrayList<>();
            boolean loadedFromCursor = false;

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            int currentYear = todayCal.get(Calendar.YEAR);

            // 1. Try querying via ContentResolver (FamilyProvider)
            try (Cursor cursor = context.getContentResolver().query(FamilyProvider.EVENTS_UPCOMING_URI, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    Log.d(TAG, "ContentProvider returned " + cursor.getCount() + " rows.");
                    int idIdx = cursor.getColumnIndex("id");
                    int nameIdx = cursor.getColumnIndex("fullName");
                    int birthIdx = cursor.getColumnIndex("birthDate");
                    int deathIdx = cursor.getColumnIndex("deathDate");
                    int marriageIdx = cursor.getColumnIndex("marriageDate");
                    int deceasedIdx = cursor.getColumnIndex("isDeceased");
                    int photoIdx = cursor.getColumnIndex("photoPath");
                    int genderIdx = cursor.getColumnIndex("gender");

                    do {
                        String id = idIdx >= 0 ? cursor.getString(idIdx) : "";
                        String fullName = nameIdx >= 0 ? cursor.getString(nameIdx) : "Relative";
                        String birthDate = birthIdx >= 0 ? cursor.getString(birthIdx) : null;
                        String deathDate = deathIdx >= 0 ? cursor.getString(deathIdx) : null;
                        String marriageDate = marriageIdx >= 0 ? cursor.getString(marriageIdx) : null;
                        boolean isDeceased = deceasedIdx >= 0 && cursor.getInt(deceasedIdx) == 1;
                        String photoPath = photoIdx >= 0 ? cursor.getString(photoIdx) : null;
                        String gender = genderIdx >= 0 ? cursor.getString(genderIdx) : "M";

                        processPersonEvents(id, fullName, birthDate, deathDate, marriageDate, isDeceased, photoPath, gender, currentYear, todayCal);
                    } while (cursor.moveToNext());
                    loadedFromCursor = true;
                }
            } catch (Exception e) {
                Log.w(TAG, "ContentResolver query failed: " + e.getMessage());
            }

            // 2. Fallback to direct Room query if ContentResolver yielded nothing or failed
            if (!loadedFromCursor || events.isEmpty()) {
                try {
                    List<PersonEntity> dbPeople = AppDatabase.getInstance(context).personDao().getAllPeopleGlobalSync();
                    if (dbPeople != null && !dbPeople.isEmpty()) {
                        Log.d(TAG, "Direct Room query returned " + dbPeople.size() + " members.");
                        for (PersonEntity p : dbPeople) {
                            processPersonEvents(p.getId(), p.getFullName(), p.getBirthDate(), p.getDeathDate(), p.getMarriageDate(), p.isDeceased(), p.getPhotoPath(), p.getGender(), currentYear, todayCal);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Direct Room query failed: " + e.getMessage(), e);
                }
            }

            Collections.sort(events);
            Log.d(TAG, "Successfully computed " + events.size() + " upcoming widget events.");
        }

        private void processPersonEvents(String id, String fullName, String birthDate, String deathDate, String marriageDate, boolean isDeceased, String photoPath, String gender, int currentYear, Calendar todayCal) {
            if (!isDeceased && birthDate != null && !birthDate.trim().isEmpty()) {
                FamilyEventModel ev = calculateNextEvent(id, fullName, birthDate, "🎂 Birthday", currentYear, todayCal, photoPath, gender, false);
                if (ev != null) events.add(ev);
            }
            if (marriageDate != null && !marriageDate.trim().isEmpty()) {
                FamilyEventModel ev = calculateNextEvent(id, fullName, marriageDate, "💍 Anniversary", currentYear, todayCal, photoPath, gender, false);
                if (ev != null) events.add(ev);
            }
            if (isDeceased && deathDate != null && !deathDate.trim().isEmpty()) {
                FamilyEventModel ev = calculateNextEvent(id, fullName, deathDate, "🕊️ Remembrance", currentYear, todayCal, photoPath, gender, true);
                if (ev != null) events.add(ev);
            }
        }

        @Override
        public void onDestroy() {
            events.clear();
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= events.size()) return null;

            FamilyEventModel ev = events.get(position);
            Prefs prefs = Prefs.getInstance(context);
            boolean hideNames = prefs.isHideNamesInWidgetAndNotifications();

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item_event);
            String displayName = hideNames ? "Relative" : ev.personName;
            views.setTextViewText(R.id.tvItemName, displayName);

            String countdownText = ev.daysUntil == 0 ? "Today" : (ev.daysUntil == 1 ? "in 1 day" : "in " + ev.daysUntil + " days");
            views.setTextViewText(R.id.tvItemDetails, ev.eventType + " • " + ev.dateStr + " (" + countdownText + ")");

            if (ev.photoPath != null && !ev.photoPath.isEmpty() && new File(ev.photoPath).exists()) {
                try {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 2;
                    Bitmap bmp = BitmapFactory.decodeFile(ev.photoPath, opts);
                    if (bmp != null) {
                        views.setImageViewBitmap(R.id.ivItemAvatar, getCircularBitmap(bmp));
                    } else {
                        views.setImageViewResource(R.id.ivItemAvatar, android.R.drawable.ic_menu_my_calendar);
                    }
                } catch (Exception ignored) {
                    views.setImageViewResource(R.id.ivItemAvatar, android.R.drawable.ic_menu_my_calendar);
                }
            } else {
                views.setImageViewResource(R.id.ivItemAvatar, android.R.drawable.ic_menu_my_calendar);
            }

            // Fill-in intent for item click
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(PersonDetailActivity.EXTRA_PERSON_ID, ev.personId);
            views.setOnClickFillInIntent(R.id.ivItemAvatar, fillInIntent);
            views.setOnClickFillInIntent(R.id.tvItemName, fillInIntent);
            views.setOnClickFillInIntent(R.id.tvItemDetails, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private static FamilyEventModel calculateNextEvent(String id, String name, String inputDateStr, String eventType, int currentYear, Calendar todayCal, String photoPath, String gender, boolean isDeceased) {
        try {
            String trimmed = inputDateStr.trim();
            String[] parts = trimmed.split("[-/.]");
            if (parts.length < 2) return null;
            int month, day;
            if (parts[0].length() == 4) {
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            } else {
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
            }

            Calendar eventCal = Calendar.getInstance();
            eventCal.set(currentYear, month, day, 0, 0, 0);
            eventCal.set(Calendar.MILLISECOND, 0);

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
}
