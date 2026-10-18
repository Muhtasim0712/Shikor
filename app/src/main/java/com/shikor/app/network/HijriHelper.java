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
 * HijriHelper - Coordinates between Room offline cache (HijriDao), AladhanClient,
 * and an offline astronomical calculation fallback.
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
     * If network fails or offline, computes using astronomical offline algorithm and caches.
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

            // 2. Cache miss -> Attempt Aladhan API
            Log.d(TAG, "Hijri date cache miss for " + dateKey + ", fetching from Aladhan API...");
            HijriCacheEntity fetched = AladhanClient.convertGregorianToHijri(dateKey);

            // 3. Fallback: If network API is offline or failed, compute offline
            if (fetched == null) {
                Log.d(TAG, "Aladhan API unavailable, calculating Hijri date offline for " + dateKey);
                fetched = calculateHijriOffline(dateKey);
            }

            if (fetched != null) {
                hijriDao.insert(fetched);
                Log.d(TAG, "Cached resolved Hijri date: " + fetched.getFormatted());
            }

            final HijriCacheEntity result = fetched;
            executors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    /**
     * Fallback offline astronomical conversion from Gregorian to Hijri.
     * Uses the standard Umm al-Qura / Tabular Islamic calendar algorithm based on Julian Day Number.
     */
    @Nullable
    public static HijriCacheEntity calculateHijriOffline(String gregorianDate) {
        if (gregorianDate == null || gregorianDate.trim().isEmpty()) return null;
        try {
            String[] parts = gregorianDate.trim().split("[-/.]");
            if (parts.length != 3) return null;

            int year, month, day;
            if (parts[0].length() == 4) {
                // yyyy-MM-dd
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            } else {
                // dd-MM-yyyy
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            }

            // Julian Day Number calculation
            int m = month;
            int y = year;
            if (m < 3) {
                y -= 1;
                m += 12;
            }
            int a = y / 100;
            int b = 2 - a + (a / 4);
            long jd = (long) (365.25 * (y + 4716)) + (int) (30.6001 * (m + 1)) + day + b - 1524;

            // Convert JDN to Hijri
            long l = jd - 1948440 + 10632;
            long n = (l - 1) / 10631;
            l = l - 10631 * n + 354;
            long j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238);
            l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29;
            int hijriMonth = (int) ((24 * l) / 709);
            int hijriDay = (int) (l - (709 * hijriMonth) / 24);
            int hijriYear = (int) (30 * n + j - 30);

            String[] monthNames = {
                    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
                    "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                    "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
            };

            String monthEn = (hijriMonth >= 1 && hijriMonth <= 12) ? monthNames[hijriMonth - 1] : "Hijri";

            return new HijriCacheEntity(
                    gregorianDate,
                    String.valueOf(hijriDay),
                    monthEn,
                    "",
                    String.valueOf(hijriYear),
                    System.currentTimeMillis()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error calculating offline Hijri date: " + e.getMessage());
            return null;
        }
    }
}
