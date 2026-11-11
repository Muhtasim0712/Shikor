package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.util.AlarmScheduler;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.NotificationHelper;
import com.shikor.app.util.Prefs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * OnThisDayReceiver - BroadcastReceiver triggered daily to resurface past family memories
 * ("On this day, N years ago") using goAsync and Room queries (Phase 6).
 *
 * CONCEPT: BroadcastReceiver with goAsync, AlarmManager background task execution, and Notification dispatch.
 */
public class OnThisDayReceiver extends BroadcastReceiver {

    private static final String TAG = "OnThisDayReceiver";
    public static final String ACTION_ON_THIS_DAY_CHECK = "com.shikor.app.ACTION_ON_THIS_DAY_CHECK";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        Prefs prefs = Prefs.getInstance(context);
        if (!prefs.isOnThisDayEnabled() || !prefs.isNotificationsEnabled()) {
            AlarmScheduler.cancelOnThisDay(context);
            return;
        }

        final PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                String monthDay = new SimpleDateFormat("MM-dd", Locale.US).format(cal.getTime());

                Calendar startOfDay = Calendar.getInstance();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);
                long startOfTodayMillis = startOfDay.getTimeInMillis();

                MemoryDao memoryDao = AppDatabase.getInstance(appContext).memoryDao();
                PersonDao personDao = AppDatabase.getInstance(appContext).personDao();

                List<MemoryEntity> matchingMemories = memoryDao.getMemoriesOnThisDay(monthDay, startOfTodayMillis);
                if (matchingMemories != null && !matchingMemories.isEmpty()) {
                    MemoryEntity latest = matchingMemories.get(0);
                    String personName = "Family Member";
                    if (latest.getTaggedPersonIds() != null && !latest.getTaggedPersonIds().isEmpty()) {
                        PersonEntity person = personDao.getPersonByIdSync(latest.getTaggedPersonIds().get(0));
                        if (person != null) personName = person.getFullName();
                    }

                    Calendar memCal = Calendar.getInstance();
                    memCal.setTimeInMillis(latest.getEventDate() > 0 ? latest.getEventDate() : latest.getCreatedAt());
                    int memYear = memCal.get(Calendar.YEAR);
                    int currentYear = cal.get(Calendar.YEAR);
                    int yearsAgo = currentYear - memYear;
                    if (yearsAgo < 1) yearsAgo = 1;

                    int extraCount = matchingMemories.size() - 1;

                    NotificationHelper.showOnThisDayMemoryNotification(
                            appContext, latest, personName, yearsAgo, extraCount, 3001
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing On This Day memory reminder: " + e.getMessage(), e);
            } finally {
                AlarmScheduler.scheduleOnThisDay(appContext);
                pendingResult.finish();
            }
        });
    }
}
