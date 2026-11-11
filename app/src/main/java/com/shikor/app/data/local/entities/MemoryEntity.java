package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.shikor.app.data.local.converters.StringListConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryEntity - Represents a family memory item in the Room database (Phase 1 new data layer).
 * CONCEPT: Room Entity, TypeConverters, indices, sync states.
 */
@Entity(
    tableName = "memories",
    indices = {
        @Index("familyId"),
        @Index("eventDate"),
        @Index("year")
    }
)
public class MemoryEntity {

    public static final int STATE_LOCAL_PENDING = 0;
    public static final int STATE_UPLOADING = 1;
    public static final int STATE_SYNCED = 2;
    public static final int STATE_FAILED = 3;

    @PrimaryKey
    @NonNull
    private String id = "";

    @NonNull
    private String familyId = "";

    private String title = "";
    private String description = "";

    private long eventDate;
    private int year;

    @TypeConverters(StringListConverter.class)
    private List<String> taggedPersonIds = new ArrayList<>();

    private int photoCount;
    private boolean hasAudio;
    private long audioDurationMs;

    @Nullable
    private String audioPath;

    private String uploaderUid = "";
    private String uploaderName = "";

    private long createdAt;
    private long updatedAt;
    private int syncState;

    @Nullable
    private String failureReason;

    private boolean notified;

    public MemoryEntity() {
    }

    public MemoryEntity(@NonNull String id, @NonNull String familyId, String title, String description,
                        long eventDate, int year, List<String> taggedPersonIds, int photoCount,
                        boolean hasAudio, long audioDurationMs, @Nullable String audioPath,
                        String uploaderUid, String uploaderName, long createdAt, long updatedAt,
                        int syncState, @Nullable String failureReason, boolean notified) {
        this.id = id;
        this.familyId = familyId;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.year = year;
        this.taggedPersonIds = taggedPersonIds != null ? taggedPersonIds : new ArrayList<>();
        this.photoCount = photoCount;
        this.hasAudio = hasAudio;
        this.audioDurationMs = audioDurationMs;
        this.audioPath = audioPath;
        this.uploaderUid = uploaderUid;
        this.uploaderName = uploaderName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
        this.failureReason = failureReason;
        this.notified = notified;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getFamilyId() { return familyId; }
    public void setFamilyId(@NonNull String familyId) { this.familyId = familyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { this.eventDate = eventDate; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public List<String> getTaggedPersonIds() { return taggedPersonIds; }
    public void setTaggedPersonIds(List<String> taggedPersonIds) { this.taggedPersonIds = taggedPersonIds != null ? taggedPersonIds : new ArrayList<>(); }

    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }

    public boolean isHasAudio() { return hasAudio; }
    public void setHasAudio(boolean hasAudio) { this.hasAudio = hasAudio; }

    public long getAudioDurationMs() { return audioDurationMs; }
    public void setAudioDurationMs(long audioDurationMs) { this.audioDurationMs = audioDurationMs; }

    @Nullable
    public String getAudioPath() { return audioPath; }
    public void setAudioPath(@Nullable String audioPath) { this.audioPath = audioPath; }

    public String getUploaderUid() { return uploaderUid; }
    public void setUploaderUid(String uploaderUid) { this.uploaderUid = uploaderUid; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getSyncState() { return syncState; }
    public void setSyncState(int syncState) { this.syncState = syncState; }

    @Nullable
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(@Nullable String failureReason) { this.failureReason = failureReason; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
}
