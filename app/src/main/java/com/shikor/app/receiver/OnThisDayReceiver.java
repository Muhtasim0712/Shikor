package com.shikor.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.StoryEntity;
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
 * ("On this day, N years ago") using goAsync and Room queries.
 *
 * CONCEPT: BroadcastReceiver with goAsync, AlarmManager background task execution, and Content/Notification dispatch.
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

                StoryDao storyDao = AppDatabase.getInstance(appContext).storyDao();
                PersonDao personDao = AppDatabase.getInstance(appContext).personDao();

                List<StoryEntity> matchingStories = storyDao.getStoriesOnThisDay(monthDay, startOfTodayMillis);
                if (matchingStories != null && !matchingStories.isEmpty()) {
                    StoryEntity latestStory = matchingStories.get(0);
                    PersonEntity person = personDao.getPersonByIdSync(latestStory.getPersonId());
                    String personName = person != null ? person.getFullName() : "Family Member";

                    Calendar storyCal = Calendar.getInstance();
                    storyCal.setTimeInMillis(latestStory.getCreatedAt());
                    int storyYear = storyCal.get(Calendar.YEAR);
                    int currentYear = cal.get(Calendar.YEAR);
                    int yearsAgo = currentYear - storyYear;
                    if (yearsAgo < 1) yearsAgo = 1;

                    int extraCount = matchingStories.size() - 1;

                    NotificationHelper.showOnThisDayNotification(
                            appContext, latestStory, personName, yearsAgo, extraCount, 3001
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing On This Day memory reminder: " + e.getMessage(), e);
            } finally {
                // Schedule next day's alarm
                AlarmScheduler.scheduleOnThisDay(appContext);
                pendingResult.finish();
            }
        });
    }
}
