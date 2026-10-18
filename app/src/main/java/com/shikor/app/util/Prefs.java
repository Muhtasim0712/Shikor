package com.shikor.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Prefs - Encapsulates SharedPreferences for user session and app settings.
 */
public class Prefs {

    private static final String PREF_NAME = "shikor_prefs";
    private static final String KEY_FAMILY_ID = "key_family_id";
    private static final String KEY_FAMILY_NAME = "key_family_name";
    private static final String KEY_FAMILY_CODE = "key_family_code";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_USER_EMAIL = "key_user_email";
    private static final String KEY_REMINDER_HOUR = "key_reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "key_reminder_minute";
    private static final String KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled";
    private static final String KEY_APP_LANGUAGE = "key_app_language";
    private static final String KEY_TREE_VIEW_MODE = "key_tree_view_mode";

    public static final String VIEW_MODE_NATURAL_TREE = "natural_tree";
    public static final String VIEW_MODE_CLASSIC_CHART = "classic_chart";

    private static Prefs sInstance;
    private final SharedPreferences preferences;

    private Prefs(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized Prefs getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Prefs(context);
        }
        return sInstance;
    }

    public void setFamily(String familyId, String familyName, String familyCode) {
        preferences.edit()
                .putString(KEY_FAMILY_ID, familyId)
                .putString(KEY_FAMILY_NAME, familyName)
                .putString(KEY_FAMILY_CODE, familyCode)
                .apply();
    }

    public String getFamilyId() {
        return preferences.getString(KEY_FAMILY_ID, null);
    }

    public String getFamilyName() {
        return preferences.getString(KEY_FAMILY_NAME, "");
    }

    public String getFamilyCode() {
        return preferences.getString(KEY_FAMILY_CODE, "");
    }

    public void setUser(String uid, String name, String email) {
        preferences.edit()
                .putString(KEY_USER_ID, uid)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public int getReminderHour() {
        return preferences.getInt(KEY_REMINDER_HOUR, 9); // Default 9:00 AM
    }

    public int getReminderMinute() {
        return preferences.getInt(KEY_REMINDER_MINUTE, 0);
    }

    public void setReminderTime(int hour, int minute) {
        preferences.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();
    }

    public boolean isNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public String getLanguage() {
        return preferences.getString(KEY_APP_LANGUAGE, "en");
    }

    public void setLanguage(String lang) {
        preferences.edit().putString(KEY_APP_LANGUAGE, lang).apply();
    }

    public String getTreeViewMode() {
        return preferences.getString(KEY_TREE_VIEW_MODE, VIEW_MODE_NATURAL_TREE);
    }

    public void setTreeViewMode(String mode) {
        preferences.edit().putString(KEY_TREE_VIEW_MODE, mode).apply();
    }

    private static final String KEY_THEME_MODE = "key_theme_mode";
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    public String getThemeMode() {
        return preferences.getString(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setThemeMode(String theme) {
        preferences.edit().putString(KEY_THEME_MODE, theme).apply();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
