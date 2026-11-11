package com.shikor.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.MemoryPhotoDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.data.remote.model.RtdbMemoryModel;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MemoryRepository - Single entry point for UI data operations and real-time sync regarding the new 'Memories' feature (Phase 3).
 * CONCEPT: Repository Pattern, Room + Firebase Realtime Database ChildEventListener, AppExecutors background execution.
 */
public class MemoryRepository {

    private static final String TAG = "ShikorMemory";
    private static volatile MemoryRepository sInstance;

    private final Context context;
    private final MemoryDao memoryDao;
    private final MemoryPhotoDao memoryPhotoDao;
    private final AppExecutors executors;
    private final Prefs prefs;
    private final DatabaseReference rtdbRef;

    private DatabaseReference memoriesRtdbRef;
    private ChildEventListener memoriesChildListener;

    private MemoryRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.memoryDao = db.memoryDao();
        this.memoryPhotoDao = db.memoryPhotoDao();
        this.executors = AppExecutors.getInstance();
        this.prefs = Prefs.getInstance(this.context);
        this.rtdbRef = FirebaseDatabase.getInstance().getReference("families");
    }

    public static MemoryRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (MemoryRepository.class) {
                if (sInstance == null) {
                    sInstance = new MemoryRepository(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<MemoryEntity>> getMemoriesForFamily(String familyId) {
        if (familyId == null || familyId.isEmpty()) {
            return memoryDao.getAllMemoriesGlobal();
        }
        return memoryDao.getMemoriesForFamily(familyId);
    }

    public LiveData<MemoryEntity> getMemoryById(String id) {
        return memoryDao.getMemoryById(id);
    }

    public LiveData<List<MemoryPhotoEntity>> getPhotosForMemory(String memoryId) {
        return memoryPhotoDao.getPhotosForMemory(memoryId);
    }

    public void insertMemory(MemoryEntity memory, List<MemoryPhotoEntity> photos, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            try {
                memory.setUpdatedAt(System.currentTimeMillis());
                memory.setSyncState(MemoryEntity.STATE_LOCAL_PENDING);
                memoryDao.insert(memory);

                if (photos != null && !photos.isEmpty()) {
                    memoryPhotoDao.deletePhotosForMemory(memory.getId());
                    memoryPhotoDao.insertAll(photos);
                }

                Log.d(TAG, "Local save success for memory " + memory.getId() + " with " + memory.getPhotoCount() + " photos.");

                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onSuccess(null);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error inserting memory: " + e.getMessage(), e);
                executors.mainThread().execute(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }
        });
    }

    private void syncMemoryToRtdb(MemoryEntity memory) {
        String fId = memory.getFamilyId() != null && !memory.getFamilyId().trim().isEmpty()
                ? memory.getFamilyId().trim()
                : "default_family";

        RtdbMemoryModel model = new RtdbMemoryModel();
        model.setId(memory.getId());
        model.setTitle(memory.getTitle());
        model.setDescription(memory.getDescription());
        model.setEventDate(memory.getEventDate());
        model.setYear(memory.getYear());

        Map<String, Boolean> taggedMap = new HashMap<>();
        if (memory.getTaggedPersonIds() != null) {
            for (String pId : memory.getTaggedPersonIds()) {
                if (pId != null && !pId.trim().isEmpty()) {
                    taggedMap.put(pId.trim(), true);
                }
            }
        }
        model.setTaggedPersonIds(taggedMap);
        model.setPhotoCount(memory.getPhotoCount());
        model.setHasAudio(memory.isHasAudio());
        model.setAudioDurationMs(memory.getAudioDurationMs());
        model.setUploaderUid(memory.getUploaderUid());
        model.setUploaderName(memory.getUploaderName());
        model.setCreatedAt(ServerValue.TIMESTAMP);
        model.setUpdatedAt(memory.getUpdatedAt());
        model.setMediaVersion(memory.getUpdatedAt());

        rtdbRef.child(fId).child("memories").child(memory.getId())
                .setValue(model)
                .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                    memory.setSyncState(MemoryEntity.STATE_SYNCED);
                    memoryDao.update(memory);
                }))
                .addOnFailureListener(e -> Log.w(TAG, "RTDB memory sync postponed: " + e.getMessage()));
    }

    // --- PHASE 3: REAL-TIME CHILD EVENT LISTENER & LAZY DOWNLOAD ---

    public synchronized void attachMemoryListener(String familyId) {
        if (familyId == null || familyId.trim().isEmpty()) return;
        detachMemoryListener();

        String fId = familyId.trim();
        Log.d(TAG, "Attaching RTDB ChildEventListener for memories, family: " + fId);

        memoriesRtdbRef = rtdbRef.child(fId).child("memories");
        memoriesChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                handleMemorySnapshot(fId, snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                handleMemorySnapshot(fId, snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String memoryId = snapshot.getKey();
                if (memoryId != null) {
                    executors.diskIO().execute(() -> {
                        Log.d(TAG, "Remote memory removed: " + memoryId);
                        MemoryEntity existing = memoryDao.getMemoryByIdSync(memoryId);
                        if (existing != null) {
                            if (existing.getAudioPath() != null) {
                                File af = new File(existing.getAudioPath());
                                if (af.exists()) af.delete();
                            }
                            File photoDir = new File(context.getFilesDir(), "memories/" + memoryId);
                            if (photoDir.exists() && photoDir.isDirectory()) {
                                File[] files = photoDir.listFiles();
                                if (files != null) {
                                    for (File f : files) f.delete();
                                }
                                photoDir.delete();
                            }
                            memoryPhotoDao.deletePhotosForMemory(memoryId);
                            memoryDao.delete(existing);
                        }
                    });
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Memories ChildEventListener cancelled: " + error.getMessage());
            }
        };
        memoriesRtdbRef.addChildEventListener(memoriesChildListener);
    }

    public synchronized void detachMemoryListener() {
        if (memoriesRtdbRef != null && memoriesChildListener != null) {
            memoriesRtdbRef.removeEventListener(memoriesChildListener);
            memoriesRtdbRef = null;
            memoriesChildListener = null;
            Log.d(TAG, "Detached RTDB ChildEventListener for memories.");
        }
    }

    private void handleMemorySnapshot(String fId, DataSnapshot snapshot) {
        executors.diskIO().execute(() -> {
            try {
                RtdbMemoryModel model = snapshot.getValue(RtdbMemoryModel.class);
                if (model == null || model.getId() == null || model.getId().isEmpty()) return;

                String memoryId = model.getId();
                Log.d(TAG, "Memory metadata arrival: " + memoryId + " (" + model.getTitle() + ")");

                MemoryEntity existing = memoryDao.getMemoryByIdSync(memoryId);
                List<String> taggedIds = new ArrayList<>();
                if (model.getTaggedPersonIds() != null) {
                    for (Map.Entry<String, Boolean> entry : model.getTaggedPersonIds().entrySet()) {
                        if (Boolean.TRUE.equals(entry.getValue())) {
                            taggedIds.add(entry.getKey());
                        }
                    }
                }

                long remoteVersion = model.getMediaVersion() > 0 ? model.getMediaVersion() : model.getUpdatedAt();

                MemoryEntity memory;
                if (existing != null) {
                    // Non-destructive MERGE rule:
                    // Never overwrite valid local audioPath, syncState, or notified flag with null/empty remote.
                    // Only replace local media when mediaVersion is newer.
                    String audioPath = existing.getAudioPath();
                    if (model.isHasAudio() && (audioPath == null || audioPath.trim().isEmpty())) {
                        audioPath = null;
                    }

                    memory = new MemoryEntity(
                            memoryId,
                            fId,
                            model.getTitle() != null ? model.getTitle() : existing.getTitle(),
                            model.getDescription() != null ? model.getDescription() : existing.getDescription(),
                            model.getEventDate() > 0 ? model.getEventDate() : existing.getEventDate(),
                            model.getYear() > 0 ? model.getYear() : existing.getYear(),
                            taggedIds.isEmpty() ? existing.getTaggedPersonIds() : taggedIds,
                            model.getPhotoCount() > 0 ? model.getPhotoCount() : existing.getPhotoCount(),
                            model.isHasAudio(),
                            model.getAudioDurationMs() > 0 ? model.getAudioDurationMs() : existing.getAudioDurationMs(),
                            audioPath,
                            model.getUploaderUid() != null ? model.getUploaderUid() : existing.getUploaderUid(),
                            model.getUploaderName() != null ? model.getUploaderName() : existing.getUploaderName(),
                            existing.getCreatedAt() > 0 ? existing.getCreatedAt() : System.currentTimeMillis(),
                            model.getUpdatedAt() > 0 ? model.getUpdatedAt() : existing.getUpdatedAt(),
                            existing.getSyncState(),
                            existing.getFailureReason(),
                            existing.isNotified()
                    );
                    Log.d(TAG, "Merge decision: Merged remote metadata into existing memory " + memoryId);
                } else {
                    memory = new MemoryEntity(
                            memoryId,
                            fId,
                            model.getTitle(),
                            model.getDescription(),
                            model.getEventDate(),
                            model.getYear(),
                            taggedIds,
                            model.getPhotoCount(),
                            model.isHasAudio(),
                            model.getAudioDurationMs(),
                            null,
                            model.getUploaderUid(),
                            model.getUploaderName(),
                            System.currentTimeMillis(),
                            model.getUpdatedAt(),
                            MemoryEntity.STATE_SYNCED,
                            null,
                            false
                    );
                    Log.d(TAG, "Merge decision: Inserted new remote memory " + memoryId);
                }

                memoryDao.insert(memory);

                // Check notification eligibility
                if (prefs.isNewMemoryAlertsEnabled()) {
                    com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    String currentUid = user != null ? user.getUid() : "";
                    long baseline = prefs.getFamilyBaseline(fId);
                    if (baseline == 0) {
                        prefs.setFamilyBaseline(fId, System.currentTimeMillis());
                        baseline = prefs.getFamilyBaseline(fId);
                    }

                    if ((currentUid.isEmpty() || !currentUid.equals(memory.getUploaderUid())) &&
                            memory.getCreatedAt() > baseline &&
                            (existing == null || !existing.isNotified())) {

                        com.shikor.app.util.NotificationHelper.showNewMemoryNotification(context, memory, prefs.isHideNamesMemoriesNotif());
                        memory.setNotified(true);
                        memoryDao.update(memory);
                        Log.d(TAG, "Dispatched notification for new memory: " + memoryId);
                    }
                }

                if (memory.getPhotoCount() > 0) {
                    checkAndDownloadPhotos(fId, memoryId, remoteVersion);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling memory snapshot: " + e.getMessage(), e);
            }
        });
    }

    private void checkAndDownloadPhotos(String fId, String memoryId, long remoteVersion) {
        List<MemoryPhotoEntity> localPhotos = memoryPhotoDao.getPhotosForMemorySync(memoryId);

        for (int i = 0; i < 4; i++) {
            if (i >= localPhotos.size()) {
                MemoryPhotoEntity p = new MemoryPhotoEntity(
                        memoryId + "_p_" + i,
                        memoryId,
                        i,
                        null,
                        0,
                        MemoryPhotoEntity.STATE_NONE
                );
                memoryPhotoDao.insert(p);
                localPhotos.add(p);
            }
        }

        for (MemoryPhotoEntity photo : localPhotos) {
            File file = photo.getLocalPath() != null ? new File(photo.getLocalPath()) : null;
            boolean needsDownload = file == null || !file.exists() || photo.getRemoteVersion() < remoteVersion;

            if (needsDownload) {
                downloadMemoryPhoto(fId, memoryId, photo.getIndex(), remoteVersion);
            }
        }
    }

    private void downloadMemoryPhoto(String fId, String memoryId, int index, long remoteVersion) {
        Log.d(TAG, "Download start for memory photo " + memoryId + " [idx " + index + "]");
        DatabaseReference photoRef = rtdbRef.child(fId).child("memoryMedia").child(memoryId).child("photos").child(String.valueOf(index));

        photoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executors.diskIO().execute(() -> {
                    try {
                        String base64 = snapshot.exists() ? snapshot.getValue(String.class) : null;
                        if (base64 != null && !base64.trim().isEmpty()) {
                            File dir = new File(context.getFilesDir(), "memories/" + memoryId);
                            if (!dir.exists()) dir.mkdirs();

                            File destFile = new File(dir, "photo_" + index + ".jpg");
                            String cleanBase64 = base64.trim();
                            if (cleanBase64.contains(",")) {
                                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                            }

                            byte[] decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
                            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                                fos.write(decodedBytes);
                            }

                            MemoryPhotoEntity p = new MemoryPhotoEntity(
                                    memoryId + "_p_" + index,
                                    memoryId,
                                    index,
                                    destFile.getAbsolutePath(),
                                    remoteVersion,
                                    MemoryPhotoEntity.STATE_DONE
                            );
                            memoryPhotoDao.insert(p);

                            // Touch memory entity in Room so LiveData<List<MemoryEntity>> fires and MemoriesAdapter re-renders!
                            MemoryEntity existing = memoryDao.getMemoryByIdSync(memoryId);
                            if (existing != null) {
                                memoryDao.update(existing);
                            }

                            Log.d(TAG, "Decode success & file written for memory photo " + memoryId + " [idx " + index + "]: " + destFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Download/decode failure for memory photo " + memoryId + " [idx " + index + "]: " + e.getMessage(), e);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Photo download cancelled for memory " + memoryId + " [idx " + index + "]: " + error.getMessage());
            }
        });
    }

    public void forceRefreshSync(String familyId) {
        if (familyId != null && !familyId.isEmpty()) {
            attachMemoryListener(familyId);
        }
    }

    public void repairMissingPhotos(Context context) {
        executors.diskIO().execute(() -> {
            try {
                List<MemoryEntity> allMemories = memoryDao.getAllMemoriesGlobalSync();
                if (allMemories == null || allMemories.isEmpty()) return;

                int count = 0;
                for (MemoryEntity memory : allMemories) {
                    if (memory.getPhotoCount() > 0) {
                        String memoryId = memory.getId();
                        String fId = memory.getFamilyId() != null && !memory.getFamilyId().trim().isEmpty()
                                ? memory.getFamilyId().trim()
                                : "default_family";

                        for (int i = 0; i < memory.getPhotoCount(); i++) {
                            File file = new File(context.getFilesDir(), "memories/" + memoryId + "/photo_" + i + ".jpg");
                            if (!file.exists()) {
                                Log.d(TAG, "Repairing missing photo index " + i + " for memory " + memoryId);
                                downloadMemoryPhoto(fId, memoryId, i, memory.getUpdatedAt());
                                count++;
                            } else {
                                MemoryPhotoEntity pe = new MemoryPhotoEntity(
                                        memoryId + "_p_" + i,
                                        memoryId,
                                        i,
                                        file.getAbsolutePath(),
                                        memory.getUpdatedAt(),
                                        MemoryPhotoEntity.STATE_DONE
                                );
                                memoryPhotoDao.insert(pe);
                            }
                        }
                    }
                }
                Log.d(TAG, "One-time photo repair check completed. Repaired/requested " + count + " photo downloads.");
            } catch (Exception e) {
                Log.e(TAG, "Error during photo repair: " + e.getMessage(), e);
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
