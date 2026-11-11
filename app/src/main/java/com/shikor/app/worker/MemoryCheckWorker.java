package com.shikor.app.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.MemoryDao;
import com.shikor.app.data.local.entities.MemoryEntity;
import com.shikor.app.data.remote.model.RtdbMemoryModel;
import com.shikor.app.util.NotificationHelper;
import com.shikor.app.util.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * MemoryCheckWorker - WorkManager PeriodicWorkRequest background worker checking for new family memories (Phase 5).
 * CONCEPT: Periodic background work, offline notification polling, batched family memory alerts.
 */
public class MemoryCheckWorker extends Worker {

    private static final String TAG = "ShikorMemory";

    private final Context context;
    private final Prefs prefs;
    private final MemoryDao memoryDao;

    public MemoryCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
        this.prefs = Prefs.getInstance(this.context);
        this.memoryDao = AppDatabase.getInstance(this.context).memoryDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!prefs.isNewMemoryAlertsEnabled()) {
            return Result.success();
        }

        String familyId = prefs.getFamilyId();
        if (familyId == null || familyId.trim().isEmpty()) {
            return Result.success();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = user != null ? user.getUid() : "";

        long lastChecked = prefs.getLastCheckedTimestamp(familyId);
        if (lastChecked == 0) {
            long now = System.currentTimeMillis();
            prefs.setFamilyBaseline(familyId, now);
            prefs.setLastCheckedTimestamp(familyId, now);
            return Result.success();
        }

        CountDownLatch latch = new CountDownLatch(1);
        final List<RtdbMemoryModel> fetchedMemories = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("families").child(familyId).child("memories")
                .orderByChild("createdAt")
                .startAt(lastChecked + 1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    RtdbMemoryModel model = child.getValue(RtdbMemoryModel.class);
                                    if (model != null && model.getId() != null) {
                                        fetchedMemories.add(model);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing memory check snapshot: " + e.getMessage(), e);
                        } finally {
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "MemoryCheckWorker cancelled: " + error.getMessage());
                        latch.countDown();
                    }
                });

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.retry();
        }

        long maxCreatedAt = lastChecked;
        List<MemoryEntity> eligibleToNotify = new ArrayList<>();

        for (RtdbMemoryModel model : fetchedMemories) {
            if (model.getCreatedAt() instanceof Number) {
                long cAt = ((Number) model.getCreatedAt()).longValue();
                if (cAt > maxCreatedAt) {
                    maxCreatedAt = cAt;
                }
                // Never notify own uploads
                if (currentUid.isEmpty() || !currentUid.equals(model.getUploaderUid())) {
                    MemoryEntity existing = memoryDao.getMemoryByIdSync(model.getId());
                    if (existing == null || !existing.isNotified()) {
                        // Build temp entity for notification
                        List<String> tagged = new ArrayList<>();
                        if (model.getTaggedPersonIds() != null) {
                            for (Map.Entry<String, Boolean> entry : model.getTaggedPersonIds().entrySet()) {
                                if (Boolean.TRUE.equals(entry.getValue())) tagged.add(entry.getKey());
                            }
                        }
                        MemoryEntity temp = new MemoryEntity(
                                model.getId(),
                                familyId,
                                model.getTitle(),
                                model.getDescription(),
                                model.getEventDate(),
                                model.getYear(),
                                tagged,
                                model.getPhotoCount(),
                                model.isHasAudio(),
                                model.getAudioDurationMs(),
                                null,
                                model.getUploaderUid(),
                                model.getUploaderName(),
                                cAt,
                                model.getUpdatedAt(),
                                MemoryEntity.STATE_SYNCED,
                                null,
                                false
                        );
                        eligibleToNotify.add(temp);
                    }
                }
            }
        }

        // Post notifications
        if (!eligibleToNotify.isEmpty()) {
            boolean hideNames = prefs.isHideNamesInWidgetAndNotifications();
            if (eligibleToNotify.size() == 1) {
                MemoryEntity m = eligibleToNotify.get(0);
                NotificationHelper.showNewMemoryNotification(context, m, hideNames);
                markNotified(m.getId());
            } else {
                // Group summary notification if several memories arrive together
                NotificationHelper.showGroupedMemoryNotification(context, eligibleToNotify.size(), hideNames);
                for (MemoryEntity m : eligibleToNotify) {
                    markNotified(m.getId());
                }
            }
        }

        prefs.setLastCheckedTimestamp(familyId, maxCreatedAt);
        return Result.success();
    }

    private void markNotified(String memoryId) {
        MemoryEntity m = memoryDao.getMemoryByIdSync(memoryId);
        if (m != null) {
            m.setNotified(true);
            memoryDao.update(m);
        }
    }
}
