package com.shikor.app.network;

import android.util.Log;

import androidx.annotation.Nullable;

import com.shikor.app.data.local.entities.HijriCacheEntity;

import org.json.JSONObject;

/**
 * HijriParser - Parses Aladhan API JSON responses into HijriCacheEntity.
 *
 * CONCEPT: Native JSON parsing with org.json.JSONObject for Islamic calendar conversion.
 */
public class HijriParser {

    private static final String TAG = "HijriParser";

    @Nullable
    public static HijriCacheEntity parse(String gregorianDateKey, String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) return null;

        try {
            JSONObject root = new JSONObject(jsonResponse);
            int code = root.optInt("code", 0);
            if (code != 200 && !root.optString("status", "").equalsIgnoreCase("OK")) {
                Log.w(TAG, "Aladhan API returned non-200 code: " + code);
                return null;
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null) return null;

            JSONObject hijri = data.optJSONObject("hijri");
            if (hijri == null) return null;

            String day = hijri.optString("day", "");
            String year = hijri.optString("year", "");

            JSONObject monthObj = hijri.optJSONObject("month");
            String monthEn = monthObj != null ? monthObj.optString("en", "") : "";
            String monthAr = monthObj != null ? monthObj.optString("ar", "") : "";

            return new HijriCacheEntity(
                    gregorianDateKey,
                    day,
                    monthEn,
                    monthAr,
                    year,
                    System.currentTimeMillis()
            );

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse Aladhan response: " + e.getMessage(), e);
            return null;
        }
    }
}
