package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * PhotoEntity - Represents a photo in the family timeline and person archive.
 * CONCEPT: Foreign key cascading delete and TypeConverter for tagged persons list
 */
@Entity(
    tableName = "photos",
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
public class PhotoEntity {

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

    private String path;
    private String caption;
    private int year;
    private List<String> taggedPersonIds = new ArrayList<>();

    private long createdAt;
    private long updatedAt;
    private int syncState;

    public PhotoEntity(@NonNull String id, @NonNull String personId, @NonNull String familyId,
                       String path, String caption, int year, List<String> taggedPersonIds,
                       long createdAt, long updatedAt, int syncState) {
        this.id = id;
        this.personId = personId;
        this.familyId = familyId;
        this.path = path;
        this.caption = caption;
        this.year = year;
        this.taggedPersonIds = taggedPersonIds != null ? taggedPersonIds : new ArrayList<>();
        this.createdAt = createdAt;
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

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public List<String> getTaggedPersonIds() { return taggedPersonIds; }
    public void setTaggedPersonIds(List<String> taggedPersonIds) { this.taggedPersonIds = taggedPersonIds; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getSyncState() { return syncState; }
    public void setSyncState(int syncState) { this.syncState = syncState; }
}
