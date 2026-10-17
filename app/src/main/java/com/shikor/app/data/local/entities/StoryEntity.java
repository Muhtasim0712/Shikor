package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * StoryEntity - Represents a memory/story attached to a person.
 * CONCEPT: Foreign key with CASCADE delete from Person to Story
 */
@Entity(
    tableName = "stories",
    foreignKeys = @ForeignKey(
        entity = PersonEntity.class,
        parentColumns = "id",
        childColumns = "personId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("personId"),
        @Index("familyId")
    }
)
public class StoryEntity {

    public static final int SYNC_STATE_SYNCED = 0;
    public static final int SYNC_STATE_PENDING = 1;
    public static final int SYNC_STATE_DELETED = 2;

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String personId;

    @NonNull
    private String familyId;

    private String title;
    private String body;

    @Nullable
    private String audioPath;
    private long audioDurationMs;

    @Nullable
    private String photoPath;

    private long createdAt;
    private String authorUid;
    private long updatedAt;
    private int syncState;

    public StoryEntity(@NonNull String id, @NonNull String personId, @NonNull String familyId,
                       String title, String body, @Nullable String audioPath, long audioDurationMs,
                       @Nullable String photoPath, long createdAt, String authorUid,
                       long updatedAt, int syncState) {
        this.id = id;
        this.personId = personId;
        this.familyId = familyId;
        this.title = title;
        this.body = body;
        this.audioPath = audioPath;
        this.audioDurationMs = audioDurationMs;
        this.photoPath = photoPath;
        this.createdAt = createdAt;
        this.authorUid = authorUid;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getPersonId() { return personId; }
    public void setPersonId(@NonNull String personId) { this.personId = personId; }

    @NonNull
    public String getFamilyId() { return familyId; }
    public void setFamilyId(@NonNull String familyId) { this.familyId = familyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    @Nullable
    public String getAudioPath() { return audioPath; }
    public void setAudioPath(@Nullable String audioPath) { this.audioPath = audioPath; }

    public long getAudioDurationMs() { return audioDurationMs; }
    public void setAudioDurationMs(long audioDurationMs) { this.audioDurationMs = audioDurationMs; }

    @Nullable
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(@Nullable String photoPath) { this.photoPath = photoPath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getAuthorUid() { return authorUid; }
    public void setAuthorUid(String authorUid) { this.authorUid = authorUid; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getSyncState() { return syncState; }
    public void setSyncState(int syncState) { this.syncState = syncState; }

    public boolean hasAudio() {
        return audioPath != null && !audioPath.trim().isEmpty();
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.trim().isEmpty();
    }
}
