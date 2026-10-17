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
    }

    public static ShikorApp getInstance() {
        return instance;
    }
}
