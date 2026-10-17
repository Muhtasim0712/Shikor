package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * HijriCacheEntity - Caches Gregorian to Hijri date conversion results for offline access.
 * CONCEPT: Offline caching for HTTP API results
 */
@Entity(tableName = "hijri_cache")
public class HijriCacheEntity {

    @PrimaryKey
    @NonNull
    private String gregorianDate; // "DD-MM-YYYY"

    private String hijriDay;
    private String hijriMonthEn;
    private String hijriMonthAr;
    private String hijriYear;
    private long cachedAt;

    public HijriCacheEntity(@NonNull String gregorianDate, String hijriDay, String hijriMonthEn,
                            String hijriMonthAr, String hijriYear, long cachedAt) {
        this.gregorianDate = gregorianDate;
        this.hijriDay = hijriDay;
        this.hijriMonthEn = hijriMonthEn;
        this.hijriMonthAr = hijriMonthAr;
        this.hijriYear = hijriYear;
        this.cachedAt = cachedAt;
    }

    @NonNull
    public String getGregorianDate() { return gregorianDate; }
    public void setGregorianDate(@NonNull String gregorianDate) { this.gregorianDate = gregorianDate; }

    public String getHijriDay() { return hijriDay; }
    public void setHijriDay(String hijriDay) { this.hijriDay = hijriDay; }

    public String getHijriMonthEn() { return hijriMonthEn; }
    public void setHijriMonthEn(String hijriMonthEn) { this.hijriMonthEn = hijriMonthEn; }

    public String getHijriMonthAr() { return hijriMonthAr; }
    public void setHijriMonthAr(String hijriMonthAr) { this.hijriMonthAr = hijriMonthAr; }

    public String getHijriYear() { return hijriYear; }
    public void setHijriYear(String hijriYear) { this.hijriYear = hijriYear; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }

    public String getFormatted() {
        return hijriDay + " " + hijriMonthEn + ", " + hijriYear + " AH";
    }
}
