package com.shikor.app.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * LocaleHelper - Wraps Context with user's selected language (en / bn)
 * ensuring localization takes effect across all Android versions.
 */
public class LocaleHelper {

    public static Context wrap(Context context, String language) {
        if (language == null || language.trim().isEmpty()) {
            language = "en";
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }
}
