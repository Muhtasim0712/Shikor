package com.shikor.app.util;

import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StatsCalculator - Computes family statistics (counts, generations, oldest/youngest,
 * lifespan, birthdays per month, gender split, and hometowns) from raw Room entities.
 *
 * CONCEPT: Data aggregation, date parsing, and statistics processing off the main thread.
 */
public class StatsCalculator {

    public static class FamilyStats {
        public int totalMembers;
        public int livingCount;
        public int deceasedCount;
        public int generationCount;
        public PersonEntity oldestMember;
        public PersonEntity youngestMember;
        public double averageLifespan;
        public int totalStories;
        public long totalAudioMinutes;
        public int totalPhotos;
        public int[] birthdaysPerMonth = new int[12]; // Jan - Dec
        public int maleCount;
        public int femaleCount;
        public int otherGenderCount;
        public Map<String, Integer> hometownsMap = new LinkedHashMap<>();
    }

    public static FamilyStats computeStats(List<PersonEntity> people, List<StoryEntity> stories, List<PhotoEntity> photos) {
        FamilyStats stats = new FamilyStats();
        if (people == null) people = Collections.emptyList();
        if (stories == null) stories = Collections.emptyList();
        if (photos == null) photos = Collections.emptyList();

        stats.totalMembers = people.size();
        stats.totalStories = stories.size();
        stats.totalPhotos = photos.size();

        long totalAudioMs = 0;
        for (StoryEntity s : stories) {
            totalAudioMs += s.getAudioDurationMs();
        }
        stats.totalAudioMinutes = totalAudioMs / 60000;

        Set<Integer> generations = new HashSet<>();
        long totalLifespanYears = 0;
        int lifespanCount = 0;

        long oldestTime = Long.MAX_VALUE;
        long youngestTime = Long.MIN_VALUE;

        for (PersonEntity p : people) {
            if (p.isDeceased()) {
                stats.deceasedCount++;
            } else {
                stats.livingCount++;
            }

            generations.add(p.getGeneration());

            // Gender split
            String gender = p.getGender();
            if ("female".equalsIgnoreCase(gender)) {
                stats.femaleCount++;
            } else if ("male".equalsIgnoreCase(gender)) {
                stats.maleCount++;
            } else {
                stats.otherGenderCount++;
            }

            // Hometown
            String ht = p.getHometown();
            if (ht != null && !ht.trim().isEmpty()) {
                ht = ht.trim();
                stats.hometownsMap.put(ht, stats.hometownsMap.getOrDefault(ht, 0) + 1);
            }

            // Birth month calculation
            long birthMillis = parseDateToMillis(p.getBirthDate());
            if (birthMillis > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(birthMillis);
                int month = cal.get(Calendar.MONTH); // 0-11
                if (month >= 0 && month < 12) {
                    stats.birthdaysPerMonth[month]++;
                }

                if (birthMillis < oldestTime) {
                    oldestTime = birthMillis;
                    stats.oldestMember = p;
                }
                if (birthMillis > youngestTime) {
                    youngestTime = birthMillis;
                    stats.youngestMember = p;
                }
            }

            // Lifespan calculation
            long deathMillis = parseDateToMillis(p.getDeathDate());
            if (birthMillis > 0 && p.isDeceased() && deathMillis > birthMillis) {
                long diffYears = (deathMillis - birthMillis) / (1000L * 60 * 60 * 24 * 365);
                totalLifespanYears += diffYears;
                lifespanCount++;
            } else if (birthMillis > 0 && !p.isDeceased()) {
                long diffYears = (System.currentTimeMillis() - birthMillis) / (1000L * 60 * 60 * 24 * 365);
                totalLifespanYears += diffYears;
                lifespanCount++;
            }
        }

        stats.generationCount = generations.size();
        if (lifespanCount > 0) {
            stats.averageLifespan = (double) totalLifespanYears / lifespanCount;
        }

        return stats;
    }

    private static long parseDateToMillis(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return 0;
        try {
            String[] parts = dateStr.trim().split("[-/.]");
            if (parts.length >= 3) {
                int y, m, d;
                if (parts[0].length() == 4) {
                    y = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]) - 1;
                    d = Integer.parseInt(parts[2]);
                } else {
                    d = Integer.parseInt(parts[0]);
                    m = Integer.parseInt(parts[1]) - 1;
                    y = Integer.parseInt(parts[2]);
                }
                Calendar cal = Calendar.getInstance();
                cal.set(y, m, d, 0, 0, 0);
                return cal.getTimeInMillis();
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
