package com.shikor.app.network;

import android.util.Log;

import androidx.annotation.Nullable;

import com.shikor.app.data.local.entities.HijriCacheEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * AladhanClient - Network client for the Aladhan Islamic Calendar API.
 *
 * CONCEPT: HttpURLConnection with connect/read timeouts, JSON streaming,
 * and conversion from Gregorian to Hijri calendar.
 */
public class AladhanClient {

    private static final String TAG = "AladhanClient";
    private static final String BASE_URL = "https://api.aladhan.com/v1/gToH";
    private static final int TIMEOUT_MS = 8000;

    /**
     * Converts a Gregorian date (YYYY-MM-DD or DD-MM-YYYY) to a HijriCacheEntity.
     * Must be called on a background thread.
     */
    @Nullable
    public static HijriCacheEntity convertGregorianToHijri(String gregorianDate) {
        if (gregorianDate == null || gregorianDate.trim().isEmpty()) return null;

        String formattedDate = normalizeToDdMmYyyy(gregorianDate.trim());
        if (formattedDate == null) return null;

        HttpURLConnection connection = null;
        try {
            String urlStr = BASE_URL + "?date=" + formattedDate;
            URL url = new URL(urlStr);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Shikor-Android-App");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return HijriParser.parse(gregorianDate, response.toString());
                }
            } else {
                Log.w(TAG, "Aladhan API error response code: " + responseCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Aladhan API network request failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * Normalizes "YYYY-MM-DD" or "YYYY/MM/DD" into "DD-MM-YYYY" as expected by Aladhan API.
     */
    @Nullable
    public static String normalizeToDdMmYyyy(String dateStr) {
        if (dateStr == null) return null;
        String[] parts = dateStr.replace("/", "-").replace(".", "-").split("-");
        if (parts.length != 3) return null;

        if (parts[0].length() == 4) {
            // YYYY-MM-DD -> DD-MM-YYYY
            return String.format("%02d-%02d-%s", Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), parts[0]);
        } else if (parts[2].length() == 4) {
            // DD-MM-YYYY
            return String.format("%02d-%02d-%s", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);
        }
        return dateStr;
    }
}
