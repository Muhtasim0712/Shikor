package com.shikor.app.data.local.converters;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StringListConverter - Room TypeConverter for converting List<String> to comma-delimited text.
 * CONCEPT: Room TypeConverters
 */
public class StringListConverter {

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = data.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
