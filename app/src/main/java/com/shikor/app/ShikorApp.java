package com.shikor.app;

import android.app.Application;
import android.util.Log;

import com.shikor.app.util.NotificationHelper;

/**
 * ShikorApp - Central Application class for Shikor.
 * Handles app-wide initialization including notification channels.
 */
public class ShikorApp extends Application {

    private static final String TAG = "ShikorApp";
    private static ShikorApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Shikor Application Initialized");

        // CONCEPT: Notification channels created once at application start
        NotificationHelper.createNotificationChannels(this);

        // CONCEPT: Restore user selected Theme (System, Light, Dark)
        String themeMode = com.shikor.app.util.Prefs.getInstance(this).getThemeMode();
        if (com.shikor.app.util.Prefs.THEME_LIGHT.equals(themeMode)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        } else if (com.shikor.app.util.Prefs.THEME_DARK.equals(themeMode)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static ShikorApp getInstance() {
        return instance;
    }
}
