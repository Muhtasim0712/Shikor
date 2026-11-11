package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * MemoryPhotoEntity - Represents a photo attached to a memory item in Room (Phase 1 new data layer).
 * CONCEPT: Foreign key with CASCADE delete from Memory to MemoryPhoto.
 */
@Entity(
    tableName = "memory_photos",
    foreignKeys = @ForeignKey(
        entity = MemoryEntity.class,
        parentColumns = "id",
        childColumns = "memoryId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("memoryId")
    }
)
public class MemoryPhotoEntity {

    public static final int STATE_NONE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_FAILED = 3;

    @PrimaryKey
    @NonNull
    private String id = "";

    @NonNull
    private String memoryId = "";

    private int index;

    @Nullable
    private String localPath;

    private long remoteVersion;
    private int downloadState;

    public MemoryPhotoEntity() {
    }

    public MemoryPhotoEntity(@NonNull String id, @NonNull String memoryId, int index,
                             @Nullable String localPath, long remoteVersion, int downloadState) {
        this.id = id;
        this.memoryId = memoryId;
        this.index = index;
        this.localPath = localPath;
        this.remoteVersion = remoteVersion;
        this.downloadState = downloadState;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getMemoryId() { return memoryId; }
    public void setMemoryId(@NonNull String memoryId) { this.memoryId = memoryId; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    @Nullable
    public String getLocalPath() { return localPath; }
    public void setLocalPath(@Nullable String localPath) { this.localPath = localPath; }

    public long getRemoteVersion() { return remoteVersion; }
    public void setRemoteVersion(long remoteVersion) { this.remoteVersion = remoteVersion; }

    public int getDownloadState() { return downloadState; }
    public void setDownloadState(int downloadState) { this.downloadState = downloadState; }
}
