package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * FamilyEntity - Represents a family group workspace in Room.
 * CONCEPT: Room Database Entity with primary key and sync state
 */
@Entity(tableName = "families")
public class FamilyEntity {

    public static final int SYNC_STATE_SYNCED = 0;
    public static final int SYNC_STATE_PENDING = 1;

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String code;
    private String adminUid;
    private long updatedAt;
    private int syncState;

    public FamilyEntity(@NonNull String id, String name, String code, String adminUid, long updatedAt, int syncState) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.adminUid = adminUid;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getSyncState() {
        return syncState;
    }

    public void setSyncState(int syncState) {
        this.syncState = syncState;
    }
}
