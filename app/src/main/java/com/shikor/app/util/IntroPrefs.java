package com.shikor.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.shikor.app.ui.auth.SplashIntroController;

/**
 * CONCEPT: SharedPreferences. Separate small preference file so the existing Prefs class is not
 * touched. Stores the 'Show intro animation' setting and whether the full intro was already seen.
 */
public final class IntroPrefs {

    private static final String FILE = "shikor_intro_prefs";
    private static final String KEY_ENABLED = "intro_enabled";
    private static final String KEY_FULL_SEEN = "intro_full_seen";

    private IntroPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static void markFullSeen(Context context) {
        prefs(context).edit().putBoolean(KEY_FULL_SEEN, true).apply();
    }

    /**
     * Decides which intro to play.
     *
     * @param signedIn true when a user session already exists (use the app's existing session check)
     * @return the mode to play, or null when the intro is switched off in Settings
     */
    @Nullable
    public static SplashIntroController.Mode chooseMode(Context context, boolean signedIn) {
        if (!isEnabled(context)) return null;
        if (SplashIntroController.animationsDisabled(context)) {
            return SplashIntroController.Mode.REDUCED;
        }
        // Always return FULL so the intro animation plays every time the app opens
        return SplashIntroController.Mode.FULL;
    }
}
