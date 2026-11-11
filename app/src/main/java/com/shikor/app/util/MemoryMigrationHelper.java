package com.shikor.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.MemoryPhotoDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.data.remote.model.RtdbMemoryModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MemoryMigrationHelper - One-time, idempotent migration helper converting legacy stories into MemoryEntity (Phase 6).
 * CONCEPT: Data migration, background execution, idempotency via SharedPreferences flag.
 */
public final class MemoryMigrationHelper {

    private static final String TAG = "ShikorMemory";
    private static final String PREF_NAME = "shikor_prefs";
    private static final String PREF_MIGRATED = "key_stories_migrated_to_memories";

    private MemoryMigrationHelper() {}

    public static void migrateStoriesToMemories(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sp = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sp.getBoolean(PREF_MIGRATED, false)) {
            return; // Already migrated
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            Log.d(TAG, "Starting one-time migration of legacy stories to new Memories layer...");
            StoryDao storyDao = AppDatabase.getInstance(appContext).storyDao();
            MemoryDao memoryDao = AppDatabase.getInstance(appContext).memoryDao();
            MemoryPhotoDao memoryPhotoDao = AppDatabase.getInstance(appContext).memoryPhotoDao();

            List<StoryEntity> oldStories = storyDao.getAllStoriesGlobalSync();
            if (oldStories == null || oldStories.isEmpty()) {
                sp.edit().putBoolean(PREF_MIGRATED, true).apply();
                Log.d(TAG, "No legacy stories found for migration.");
                return;
            }

            int migratedCount = 0;
            int failedCount = 0;

            DatabaseReference rtdbRef = FirebaseDatabase.getInstance().getReference("families");

            for (StoryEntity story : oldStories) {
                try {
                    String memoryId = story.getId();
                    String familyId = story.getFamilyId() != null && !story.getFamilyId().trim().isEmpty()
                            ? story.getFamilyId().trim()
                            : "default_family";

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(story.getCreatedAt() > 0 ? story.getCreatedAt() : System.currentTimeMillis());
                    int year = cal.get(Calendar.YEAR);

                    List<String> tagged = new ArrayList<>();
                    if (story.getPersonId() != null && !story.getPersonId().isEmpty()) {
                        tagged.add(story.getPersonId());
                    }

                    int photoCount = story.hasPhoto() ? 1 : 0;
                    MemoryEntity memory = new MemoryEntity(
                            memoryId,
                            familyId,
                            story.getTitle() != null ? story.getTitle() : "Migrated Memory",
                            story.getBody() != null ? story.getBody() : "",
                            story.getCreatedAt(),
                            year,
                            tagged,
                            photoCount,
                            story.hasAudio(),
                            story.getAudioDurationMs(),
                            story.getAudioPath(),
                            story.getAuthorUid() != null ? story.getAuthorUid() : "migration",
                            "Family Member",
                            story.getCreatedAt(),
                            story.getUpdatedAt(),
                            MemoryEntity.STATE_LOCAL_PENDING,
                            null,
                            true
                    );

                    memoryDao.insert(memory);

                    if (story.hasPhoto() && story.getPhotoPath() != null) {
                        File srcFile = new File(story.getPhotoPath());
                        if (srcFile.exists()) {
                            File dir = new File(appContext.getFilesDir(), "memories/" + memoryId);
                            if (!dir.exists()) dir.mkdirs();
                            File destFile = new File(dir, "photo_0.jpg");

                            try (FileInputStream fis = new FileInputStream(srcFile);
                                 FileOutputStream fos = new FileOutputStream(destFile)) {
                                byte[] buf = new byte[8192];
                                int len;
                                while ((len = fis.read(buf)) > 0) {
                                    fos.write(buf, 0, len);
                                }
                            }

                            MemoryPhotoEntity pe = new MemoryPhotoEntity(
                                    memoryId + "_p_0",
                                    memoryId,
                                    0,
                                    destFile.getAbsolutePath(),
                                    memory.getUpdatedAt(),
                                    MemoryPhotoEntity.STATE_DONE
                            );
                            memoryPhotoDao.insert(pe);

                            String base64Photo = ImageEncoder.fileToBase64(destFile.getAbsolutePath());
                            if (base64Photo != null && !base64Photo.isEmpty()) {
                                rtdbRef.child(familyId).child("memoryMedia").child(memoryId).child("photos").child("0").setValue(base64Photo);
                            }
                        }
                    }

                    RtdbMemoryModel model = new RtdbMemoryModel();
                    model.setId(memoryId);
                    model.setTitle(memory.getTitle());
                    model.setDescription(memory.getDescription());
                    model.setEventDate(memory.getEventDate());
                    model.setYear(memory.getYear());
                    Map<String, Boolean> taggedMap = new HashMap<>();
                    for (String pId : tagged) taggedMap.put(pId, true);
                    model.setTaggedPersonIds(taggedMap);
                    model.setPhotoCount(memory.getPhotoCount());
                    model.setHasAudio(memory.isHasAudio());
                    model.setAudioDurationMs(memory.getAudioDurationMs());
                    model.setUploaderUid(memory.getUploaderUid());
                    model.setUploaderName(memory.getUploaderName());
                    model.setCreatedAt(ServerValue.TIMESTAMP);
                    model.setUpdatedAt(memory.getUpdatedAt());
                    model.setMediaVersion(memory.getUpdatedAt());

                    rtdbRef.child(familyId).child("memories").child(memoryId).setValue(model);

                    migratedCount++;
                } catch (Exception e) {
                    failedCount++;
                    Log.e(TAG, "Failed to migrate story " + story.getId() + ": " + e.getMessage(), e);
                }
            }

            sp.edit().putBoolean(PREF_MIGRATED, true).apply();
            Log.d(TAG, "Migration completed. Migrated: " + migratedCount + ", Failed: " + failedCount);
        });
    }
}
