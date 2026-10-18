package com.shikor.app.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.HijriDao;
import com.shikor.app.data.local.entities.HijriCacheEntity;
import com.shikor.app.util.AppExecutors;

/**
 * HijriHelper - Coordinates between Room offline cache (HijriDao) and AladhanClient.
 *
 * CONCEPT: Cache-aside pattern for offline-first Islamic date conversions.
 */
public class HijriHelper {

    private static final String TAG = "HijriHelper";

    public interface HijriCallback {
        void onResult(@Nullable HijriCacheEntity entity);
    }

    /**
     * Resolves Hijri date for a given Gregorian date (YYYY-MM-DD or DD-MM-YYYY).
     * Automatically queries Room cache first; if cache miss, queries AladhanClient and caches.
     */
    public static void resolveHijriDate(@NonNull Context context,
                                        @Nullable String gregorianDate,
                                        @NonNull HijriCallback callback) {
        if (gregorianDate == null || gregorianDate.trim().isEmpty()) {
            callback.onResult(null);
            return;
        }

        final String dateKey = gregorianDate.trim();
        AppExecutors executors = AppExecutors.getInstance();
        HijriDao hijriDao = AppDatabase.getInstance(context).hijriDao();

        executors.diskIO().execute(() -> {
            // 1. Check Room Cache
            HijriCacheEntity cached = hijriDao.getCachedDate(dateKey);
            if (cached != null) {
                Log.d(TAG, "Hijri date cache hit for " + dateKey + ": " + cached.getFormatted());
                executors.mainThread().execute(() -> callback.onResult(cached));
                return;
            }

            // 2. Cache miss -> Fetch from Aladhan API
            Log.d(TAG, "Hijri date cache miss for " + dateKey + ", fetching from Aladhan API...");
            HijriCacheEntity fetched = AladhanClient.convertGregorianToHijri(dateKey);
            if (fetched != null) {
                hijriDao.insert(fetched);
                Log.d(TAG, "Cached fetched Hijri date: " + fetched.getFormatted());
            }

            executors.mainThread().execute(() -> callback.onResult(fetched));
        });
    }
}
