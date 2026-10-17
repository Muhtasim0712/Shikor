package com.shikor.app.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.entities.PersonEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * FamilyImporter - Imports and merges family member records from CSV and vCard (.vcf) files.
 *
 * CONCEPT: Multi-format contact importer (CSV & vCard 3.0), intelligent deduplication,
 * and bulk database ingestion via Room.
 */
public class FamilyImporter {

    private static final String TAG = "FamilyImporter";

    public static class ImportResult {
        public final boolean success;
        public final int totalProcessed;
        public final int insertedCount;
        public final int updatedCount;
        public final String message;

        public ImportResult(boolean success, int totalProcessed, int insertedCount, int updatedCount, String message) {
            this.success = success;
            this.totalProcessed = totalProcessed;
            this.insertedCount = insertedCount;
            this.updatedCount = updatedCount;
            this.message = message;
        }
    }

    private static class ParsedPerson {
        String fullName;
        String nickName;
        String phoneNumber;
        String birthDate;
        String deathDate;
        boolean isDeceased;
        String hometown;
        String occupation;
        String gender = "other";
        String biography;
    }

    /**
     * Imports members from a chosen file URI (CSV or vCard).
     * Must be called on a background thread.
     */
    @NonNull
    public static ImportResult importFromUri(@NonNull Context context, @NonNull Uri uri) {
        List<ParsedPerson> parsedList = new ArrayList<>();
        boolean isVCard = false;

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            if (inputStream == null) {
                return new ImportResult(false, 0, 0, 0, "Could not open selected file.");
            }

            // Read all lines into memory
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                    if (line.toUpperCase(Locale.US).startsWith("BEGIN:VCARD")) {
                        isVCard = true;
                    }
                }
            }

            if (lines.isEmpty()) {
                return new ImportResult(false, 0, 0, 0, "Selected file is empty.");
            }

            if (isVCard) {
                parsedList = parseVCardLines(lines);
            } else {
                parsedList = parseCsvLines(lines);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed reading import file: " + e.getMessage(), e);
            return new ImportResult(false, 0, 0, 0, "Error reading file: " + e.getLocalizedMessage());
        }

        if (parsedList.isEmpty()) {
            return new ImportResult(false, 0, 0, 0, "No valid family contact records found in file.");
        }

        // Save / Merge into Room database
        return mergeAndSavePeople(context, parsedList);
    }

    /**
     * Parses vCard (vCard 2.1 / 3.0 / 4.0) lines into ParsedPerson objects.
     */
    private static List<ParsedPerson> parseVCardLines(List<String> lines) {
        List<ParsedPerson> list = new ArrayList<>();
        ParsedPerson current = null;

        for (String line : lines) {
            String upper = line.toUpperCase(Locale.US);
            if (upper.startsWith("BEGIN:VCARD")) {
                current = new ParsedPerson();
            } else if (upper.startsWith("END:VCARD")) {
                if (current != null && (current.fullName != null || current.phoneNumber != null)) {
                    list.add(current);
                }
                current = null;
            } else if (current != null) {
                if (upper.startsWith("FN:") || upper.startsWith("FN;")) {
                    current.fullName = extractValue(line);
                } else if (upper.startsWith("N:") || upper.startsWith("N;")) {
                    if (current.fullName == null || current.fullName.isEmpty()) {
                        String nVal = extractValue(line);
                        String[] parts = nVal.split(";");
                        StringBuilder sb = new StringBuilder();
                        for (int i = parts.length - 1; i >= 0; i--) {
                            String p = parts[i].trim();
                            if (!p.isEmpty()) {
                                if (sb.length() > 0) sb.append(" ");
                                sb.append(p);
                            }
                        }
                        current.fullName = sb.toString();
                    }
                } else if (upper.startsWith("TEL:") || upper.startsWith("TEL;")) {
                    if (current.phoneNumber == null) {
                        current.phoneNumber = cleanPhoneNumber(extractValue(line));
                    }
                } else if (upper.startsWith("BDAY:") || upper.startsWith("BDAY;")) {
                    current.birthDate = normalizeDate(extractValue(line));
                } else if (upper.startsWith("NOTE:") || upper.startsWith("NOTE;")) {
                    String note = extractValue(line);
                    if (current.biography == null) {
                        current.biography = note;
                    }
                    if (note.toLowerCase(Locale.US).contains("hometown:")) {
                        int idx = note.toLowerCase(Locale.US).indexOf("hometown:");
                        current.hometown = note.substring(idx + 9).trim();
                    }
                } else if (upper.startsWith("GENDER:") || upper.startsWith("X-GENDER:")) {
                    String g = extractValue(line).toLowerCase(Locale.US);
                    if (g.startsWith("m")) current.gender = "male";
                    else if (g.startsWith("f")) current.gender = "female";
                }
            }
        }

        return list;
    }

    /**
     * Parses CSV lines into ParsedPerson objects, supporting flexible column headers.
     */
    private static List<ParsedPerson> parseCsvLines(List<String> lines) {
        List<ParsedPerson> list = new ArrayList<>();
        if (lines.isEmpty()) return list;

        List<String> headerTokens = parseCsvRow(lines.get(0));
        Map<String, Integer> headerMap = new HashMap<>();
        boolean hasHeader = false;

        for (int i = 0; i < headerTokens.size(); i++) {
            String token = headerTokens.get(i).trim().toLowerCase(Locale.US).replace(" ", "").replace("_", "");
            if (token.contains("name") || token.contains("phone") || token.contains("birth") || token.contains("town") || token.contains("status")) {
                hasHeader = true;
                headerMap.put(token, i);
            }
        }

        int startIndex = hasHeader ? 1 : 0;

        for (int i = startIndex; i < lines.size(); i++) {
            List<String> row = parseCsvRow(lines.get(i));
            if (row.isEmpty()) continue;

            ParsedPerson person = new ParsedPerson();

            if (hasHeader) {
                person.fullName = getColumnValue(row, headerMap, "fullname", "name");
                person.phoneNumber = cleanPhoneNumber(getColumnValue(row, headerMap, "phonenumber", "phone", "mobile", "cell"));
                person.birthDate = normalizeDate(getColumnValue(row, headerMap, "birthdate", "birth", "birthday", "bday", "dob"));
                person.deathDate = normalizeDate(getColumnValue(row, headerMap, "deathdate", "death"));
                person.hometown = getColumnValue(row, headerMap, "hometown", "town", "city");
                person.occupation = getColumnValue(row, headerMap, "occupation", "job", "profession");
                person.biography = getColumnValue(row, headerMap, "biography", "bio", "notes", "note");

                String status = getColumnValue(row, headerMap, "status", "deceased");
                if (status != null && (status.toLowerCase(Locale.US).contains("deceased") || status.toLowerCase(Locale.US).contains("dead") || status.equals("1") || status.equalsIgnoreCase("true"))) {
                    person.isDeceased = true;
                }

                String gender = getColumnValue(row, headerMap, "gender", "sex");
                if (gender != null) {
                    String g = gender.toLowerCase(Locale.US);
                    if (g.startsWith("m")) person.gender = "male";
                    else if (g.startsWith("f")) person.gender = "female";
                }
            } else {
                // Positional fallback matching ExportActivity's CSV:
                // Full Name, Phone Number, Birth Date, Hometown, Status
                if (row.size() > 0) person.fullName = row.get(0).trim();
                if (row.size() > 1) person.phoneNumber = cleanPhoneNumber(row.get(1));
                if (row.size() > 2) person.birthDate = normalizeDate(row.get(2));
                if (row.size() > 3) person.hometown = row.get(3).trim();
                if (row.size() > 4) {
                    String status = row.get(4).trim().toLowerCase(Locale.US);
                    if (status.contains("deceased") || status.contains("dead")) {
                        person.isDeceased = true;
                    }
                }
            }

            if ((person.fullName != null && !person.fullName.isEmpty()) ||
                (person.phoneNumber != null && !person.phoneNumber.isEmpty())) {
                list.add(person);
            }
        }

        return list;
    }

    private static String getColumnValue(List<String> row, Map<String, Integer> headerMap, String... candidateKeys) {
        for (String key : candidateKeys) {
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                if (entry.getKey().contains(key)) {
                    int col = entry.getValue();
                    if (col >= 0 && col < row.size()) {
                        String val = row.get(col).trim();
                        if (!val.isEmpty()) return val;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Splits a single CSV row handling double quotes and escaped commas.
     */
    private static List<String> parseCsvRow(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }

    private static String extractValue(String line) {
        int idx = line.indexOf(':');
        if (idx >= 0 && idx < line.length() - 1) {
            return line.substring(idx + 1).trim();
        }
        return "";
    }

    private static String cleanPhoneNumber(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("[^0-9+]", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return null;
        rawDate = rawDate.trim().replace("/", "-").replace(".", "-");
        // Convert formats like YYYYMMDD to YYYY-MM-DD
        if (rawDate.length() == 8 && !rawDate.contains("-")) {
            return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8);
        }
        return rawDate;
    }

    /**
     * Saves parsed persons to Room, merging with existing family members.
     */
    private static ImportResult mergeAndSavePeople(Context context, List<ParsedPerson> parsedList) {
        AppDatabase db = AppDatabase.getInstance(context);
        Prefs prefs = Prefs.getInstance(context);

        String familyId = prefs.getFamilyId();
        if (familyId == null || familyId.isEmpty()) {
            familyId = "default_family";
            prefs.setFamily(familyId, "My Family", prefs.getFamilyCode() != null ? prefs.getFamilyCode() : "123456");
        }

        String userEmail = prefs.getUserEmail();
        if (userEmail == null) userEmail = "admin@shikor.app";

        List<PersonEntity> existingList = db.personDao().getAllPeopleSync(familyId);
        if (existingList == null) existingList = new ArrayList<>();

        int insertedCount = 0;
        int updatedCount = 0;

        for (ParsedPerson p : parsedList) {
            String name = p.fullName;
            if (name == null || name.trim().isEmpty()) {
                if (p.phoneNumber != null && !p.phoneNumber.isEmpty()) {
                    name = "Relative (" + p.phoneNumber + ")";
                } else {
                    continue;
                }
            }
            name = name.trim();

            PersonEntity matched = null;
            for (PersonEntity e : existingList) {
                // Match by exact case-insensitive full name OR by non-empty phone number
                if (e.getFullName() != null && e.getFullName().equalsIgnoreCase(name)) {
                    matched = e;
                    break;
                }
                if (p.phoneNumber != null && !p.phoneNumber.isEmpty() &&
                    e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty() &&
                    e.getPhoneNumber().equals(p.phoneNumber)) {
                    matched = e;
                    break;
                }
            }

            if (matched != null) {
                // Update missing fields
                boolean changed = false;
                if ((matched.getPhoneNumber() == null || matched.getPhoneNumber().isEmpty()) && p.phoneNumber != null) {
                    matched.setPhoneNumber(p.phoneNumber);
                    changed = true;
                }
                if ((matched.getBirthDate() == null || matched.getBirthDate().isEmpty()) && p.birthDate != null) {
                    matched.setBirthDate(p.birthDate);
                    changed = true;
                }
                if ((matched.getHometown() == null || matched.getHometown().isEmpty()) && p.hometown != null) {
                    matched.setHometown(p.hometown);
                    changed = true;
                }
                if ((matched.getOccupation() == null || matched.getOccupation().isEmpty()) && p.occupation != null) {
                    matched.setOccupation(p.occupation);
                    changed = true;
                }
                if (p.isDeceased && !matched.isDeceased()) {
                    matched.setDeceased(true);
                    if (p.deathDate != null) matched.setDeathDate(p.deathDate);
                    changed = true;
                }

                if (changed) {
                    matched.setUpdatedAt(System.currentTimeMillis());
                    matched.setSyncState(PersonEntity.SYNC_STATE_PENDING);
                    db.personDao().update(matched);
                    updatedCount++;
                }
            } else {
                // Insert new PersonEntity
                PersonEntity newPerson = new PersonEntity(
                        UUID.randomUUID().toString(),
                        familyId,
                        name,
                        p.nickName != null ? p.nickName : "",
                        p.gender != null ? p.gender : "other",
                        p.birthDate != null ? p.birthDate : "",
                        p.deathDate,
                        p.isDeceased,
                        p.hometown != null ? p.hometown : "",
                        p.occupation != null ? p.occupation : "",
                        p.phoneNumber != null ? p.phoneNumber : "",
                        p.biography != null ? p.biography : "",
                        null, // photoPath
                        null, // fatherId
                        null, // motherId
                        null, // spouseId
                        null, // marriageDate
                        userEmail,
                        0,    // generation
                        System.currentTimeMillis(),
                        PersonEntity.SYNC_STATE_PENDING
                );

                db.personDao().insert(newPerson);
                existingList.add(newPerson);
                insertedCount++;
            }
        }

        String msg = "Imported " + insertedCount + " new relative(s) and updated " + updatedCount + " existing member(s).";
        return new ImportResult(true, parsedList.size(), insertedCount, updatedCount, msg);
    }
}
