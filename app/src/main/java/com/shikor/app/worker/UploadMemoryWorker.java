package com.shikor.app.worker;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.daos.MemoryPhotoDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.local.entities.MemoryPhotoEntity;
import com.shikor.app.util.MemoryImageCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UploadMemoryWorker - Background WorkManager worker uploading memory photos, audio, and metadata
 * to Firebase Realtime Database with atomic write ordering (media first, metadata last).
 * CONCEPT: WorkManager Worker, background execution, robust retries, RTDB atomic writes.
 */
public class UploadMemoryWorker extends Worker {

    private static final String TAG = "ShikorMemory";
    public static final String KEY_MEMORY_ID = "memory_id";

    private final Context context;
    private final MemoryDao memoryDao;
    private final MemoryPhotoDao memoryPhotoDao;
    private final DatabaseReference rtdbRef;

    public UploadMemoryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.memoryDao = db.memoryDao();
        this.memoryPhotoDao = db.memoryPhotoDao();
        this.rtdbRef = FirebaseDatabase.getInstance().getReference("families");
    }

    @NonNull
    @Override
    public Result doWork() {
        String memoryId = getInputData().getString(KEY_MEMORY_ID);
        if (memoryId == null || memoryId.isEmpty()) {
            return Result.failure();
        }

        MemoryEntity memory = memoryDao.getMemoryByIdSync(memoryId);
        if (memory == null) {
            return Result.success();
        }

        try {
            Log.d(TAG, "Upload start for memory " + memoryId);
            memory.setSyncState(MemoryEntity.STATE_UPLOADING);
            memoryDao.update(memory);

            String fId = memory.getFamilyId() != null && !memory.getFamilyId().trim().isEmpty()
                    ? memory.getFamilyId().trim()
                    : "default_family";

            DatabaseReference memoryMediaRef = rtdbRef.child(fId).child("memoryMedia").child(memoryId);
            DatabaseReference memoriesMetaRef = rtdbRef.child(fId).child("memories").child(memoryId);

            // 1. Write media nodes FIRST (Photos and Audio)
            List<MemoryPhotoEntity> photos = memoryPhotoDao.getPhotosForMemorySync(memoryId);
            if ((photos == null || photos.isEmpty()) && memory.getPhotoCount() > 0) {
                File photoDir = new File(context.getFilesDir(), "memories/" + memoryId);
                for (int i = 0; i < memory.getPhotoCount(); i++) {
                    File pFile = new File(photoDir, "photo_" + i + ".jpg");
                    if (pFile.exists()) {
                        MemoryPhotoEntity pe = new MemoryPhotoEntity(
                                memoryId + "_p_" + i,
                                memoryId,
                                i,
                                pFile.getAbsolutePath(),
                                memory.getUpdatedAt(),
                                MemoryPhotoEntity.STATE_DONE
                        );
                        memoryPhotoDao.insert(pe);
                    }
                }
                photos = memoryPhotoDao.getPhotosForMemorySync(memoryId);
            }

            if (photos != null && !photos.isEmpty()) {
                DatabaseReference photosRef = memoryMediaRef.child("photos");
                for (int i = 0; i < photos.size(); i++) {
                    MemoryPhotoEntity photo = photos.get(i);
                    String localPath = photo.getLocalPath();
                    if (localPath != null && !localPath.isEmpty()) {
                        MemoryImageCompressor.ProcessedPhoto processed = MemoryImageCompressor.compressAndEncode(context, localPath, memoryId, photo.getIndex());
                        if (processed != null) {
                            Log.d(TAG, "Uploading photo index " + photo.getIndex() + " for memory " + memoryId + " (" + processed.sizeKb + " KB)...");
                            Tasks.await(photosRef.child(String.valueOf(photo.getIndex())).setValue(processed.base64Data));
                            Log.d(TAG, "Media write success: photo index " + photo.getIndex() + " for memory " + memoryId);
                            photo.setLocalPath(processed.localPath);
                            photo.setDownloadState(MemoryPhotoEntity.STATE_DONE);
                            photo.setRemoteVersion(memory.getUpdatedAt());
                            memoryPhotoDao.update(photo);
                        } else {
                            throw new IllegalStateException("Failed to compress photo index " + photo.getIndex());
                        }
                    }
                }
            }

            // Audio (optional)
            if (memory.isHasAudio() && memory.getAudioPath() != null && !memory.getAudioPath().isEmpty()) {
                File audioFile = new File(memory.getAudioPath());
                if (audioFile.exists()) {
                    byte[] audioBytes = new byte[(int) audioFile.length()];
                    try (FileInputStream fis = new FileInputStream(audioFile)) {
                        int read = fis.read(audioBytes);
                        if (read > 0) {
                            String audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP);
                            Tasks.await(memoryMediaRef.child("audio").setValue(audioBase64));
                            Log.d(TAG, "Media write success: Audio uploaded for memory " + memoryId + " (" + (audioBytes.length / 1024) + " KB)");
                        }
                    }
                }
            }

            // 2. Write metadata node LAST
            Map<String, Object> metaMap = new HashMap<>();
            metaMap.put("id", memory.getId());
            metaMap.put("title", memory.getTitle());
            metaMap.put("description", memory.getDescription());
            metaMap.put("eventDate", memory.getEventDate());
            metaMap.put("year", memory.getYear());

            Map<String, Boolean> taggedMap = new HashMap<>();
            if (memory.getTaggedPersonIds() != null) {
                for (String pId : memory.getTaggedPersonIds()) {
                    if (pId != null && !pId.trim().isEmpty()) {
                        taggedMap.put(pId.trim(), true);
                    }
                }
            }
            metaMap.put("taggedPersonIds", taggedMap);
            metaMap.put("photoCount", memory.getPhotoCount());
            metaMap.put("hasAudio", memory.isHasAudio());
            metaMap.put("audioDurationMs", memory.getAudioDurationMs());
            metaMap.put("uploaderUid", memory.getUploaderUid());
            metaMap.put("uploaderName", memory.getUploaderName());
            metaMap.put("createdAt", ServerValue.TIMESTAMP);
            metaMap.put("updatedAt", memory.getUpdatedAt());
            metaMap.put("mediaVersion", memory.getUpdatedAt());

            Tasks.await(memoriesMetaRef.setValue(metaMap));
            Log.d(TAG, "Metadata write success for memory " + memoryId);

            memory.setSyncState(MemoryEntity.STATE_SYNCED);
            memory.setFailureReason(null);
            memoryDao.update(memory);

            Log.d(TAG, "Upload completed successfully for memory " + memoryId);
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Upload failed for memory " + memoryId + ": " + e.getMessage(), e);
            memory.setSyncState(MemoryEntity.STATE_FAILED);
            memory.setFailureReason(e.getMessage());
            memoryDao.update(memory);
            return Result.retry();
        }
    }
}
