package com.shikor.app.data.local.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * PersonEntity - Represents a family member profile in Room.
 * CONCEPT: Room Entity with multiple indices for fast relationship traversal
 */
@Entity(
    tableName = "people",
    indices = {
        @Index("familyId"),
        @Index("fatherId"),
        @Index("motherId"),
        @Index("spouseId")
    }
)
public class PersonEntity {

    public static final int SYNC_STATE_SYNCED = 0;
    public static final int SYNC_STATE_PENDING = 1;
    public static final int SYNC_STATE_DELETED = 2;

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String familyId;
    private String fullName;
    private String nickName;
    private String gender; // "male", "female", "other"
    private String birthDate; // "YYYY-MM-DD"
    @Nullable
    private String deathDate; // "YYYY-MM-DD" or null
    private boolean isDeceased;
    private String hometown;
    private String occupation;
    private String phoneNumber;
    private String biography;
    private String photoPath;

    @Nullable
    private String fatherId;
    @Nullable
    private String motherId;
    @Nullable
    private String spouseId;
    @Nullable
    private String marriageDate;

    private String createdByUid;
    private int generation; // 0=root, -1=parents, 1=children
    private long updatedAt;
    private int syncState;

    public PersonEntity(@NonNull String id, @NonNull String familyId, String fullName, String nickName,
                        String gender, String birthDate, @Nullable String deathDate, boolean isDeceased,
                        String hometown, String occupation, String phoneNumber, String biography,
                        String photoPath, @Nullable String fatherId, @Nullable String motherId,
                        @Nullable String spouseId, @Nullable String marriageDate, String createdByUid,
                        int generation, long updatedAt, int syncState) {
        this.id = id;
        this.familyId = familyId;
        this.fullName = fullName;
        this.nickName = nickName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.isDeceased = isDeceased;
        this.hometown = hometown;
        this.occupation = occupation;
        this.phoneNumber = phoneNumber;
        this.biography = biography;
        this.photoPath = photoPath;
        this.fatherId = fatherId;
        this.motherId = motherId;
        this.spouseId = spouseId;
        this.marriageDate = marriageDate;
        this.createdByUid = createdByUid;
        this.generation = generation;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getFamilyId() { return familyId; }
    public void setFamilyId(@NonNull String familyId) { this.familyId = familyId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    @Nullable
    public String getDeathDate() { return deathDate; }
    public void setDeathDate(@Nullable String deathDate) { this.deathDate = deathDate; }

    public boolean isDeceased() { return isDeceased; }
    public void setDeceased(boolean deceased) { isDeceased = deceased; }

    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    @Nullable
    public String getFatherId() { return fatherId; }
    public void setFatherId(@Nullable String fatherId) { this.fatherId = fatherId; }

    @Nullable
    public String getMotherId() { return motherId; }
    public void setMotherId(@Nullable String motherId) { this.motherId = motherId; }

    @Nullable
    public String getSpouseId() { return spouseId; }
    public void setSpouseId(@Nullable String spouseId) { this.spouseId = spouseId; }

    @Nullable
    public String getMarriageDate() { return marriageDate; }
    public void setMarriageDate(@Nullable String marriageDate) { this.marriageDate = marriageDate; }

    public String getCreatedByUid() { return createdByUid; }
    public void setCreatedByUid(String createdByUid) { this.createdByUid = createdByUid; }

    public int getGeneration() { return generation; }
    public void setGeneration(int generation) { this.generation = generation; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getSyncState() { return syncState; }
    public void setSyncState(int syncState) { this.syncState = syncState; }

    /**
     * Returns the formatted lifespan (e.g. "1940 – 2018" or "b. 1995")
     */
    public String getLifeSpan() {
        String birthYear = (birthDate != null && birthDate.length() >= 4) ? birthDate.substring(0, 4) : "";
        if (isDeceased) {
            String deathYear = (deathDate != null && deathDate.length() >= 4) ? deathDate.substring(0, 4) : "?";
            return birthYear.isEmpty() ? ("d. " + deathYear) : (birthYear + " – " + deathYear);
        } else {
            return birthYear.isEmpty() ? "" : ("b. " + birthYear);
        }
    }

    /**
     * Extracts 1-2 letters from fullName as fallback avatar initials.
     */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonEntity that = (PersonEntity) o;
        return isDeceased == that.isDeceased &&
                generation == that.generation &&
                updatedAt == that.updatedAt &&
                syncState == that.syncState &&
                id.equals(that.id) &&
                Objects.equals(fullName, that.fullName) &&
                Objects.equals(photoPath, that.photoPath) &&
                Objects.equals(birthDate, that.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, updatedAt);
    }
}
