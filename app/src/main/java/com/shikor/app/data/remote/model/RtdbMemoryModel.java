package com.shikor.app.data.remote.model;

import java.util.HashMap;
import java.util.Map;

/**
 * RtdbMemoryModel - Plain model class for Firebase Realtime Database memory metadata (Phase 1).
 * CONCEPT: Plain Java Model, public no-arg constructor, getters/setters matching exact DB keys.
 */
public class RtdbMemoryModel {

    private String id = "";
    private String title = "";
    private String description = "";
    private long eventDate;
    private int year;
    private Map<String, Boolean> taggedPersonIds = new HashMap<>();
    private int photoCount;
    private boolean hasAudio;
    private long audioDurationMs;
    private String uploaderUid = "";
    private String uploaderName = "";
    private Object createdAt;
    private long updatedAt;
    private long mediaVersion;

    public RtdbMemoryModel() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getEventDate() { return eventDate; }
    public void setEventDate(long eventDate) { this.eventDate = eventDate; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Map<String, Boolean> getTaggedPersonIds() { return taggedPersonIds; }
    public void setTaggedPersonIds(Map<String, Boolean> taggedPersonIds) { this.taggedPersonIds = taggedPersonIds != null ? taggedPersonIds : new HashMap<>(); }

    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }

    public boolean isHasAudio() { return hasAudio; }
    public void setHasAudio(boolean hasAudio) { this.hasAudio = hasAudio; }

    public long getAudioDurationMs() { return audioDurationMs; }
    public void setAudioDurationMs(long audioDurationMs) { this.audioDurationMs = audioDurationMs; }

    public String getUploaderUid() { return uploaderUid; }
    public void setUploaderUid(String uploaderUid) { this.uploaderUid = uploaderUid; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getMediaVersion() { return mediaVersion; }
    public void setMediaVersion(long mediaVersion) { this.mediaVersion = mediaVersion; }
}
