package com.shikor.app.data.local.daos;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.StoryEntity;

import java.util.List;

/**
 * StoryDao - Data Access Object for StoryEntity in Room.
 */
@Dao
public interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoryEntity story);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StoryEntity> stories);

    @Update
    void update(StoryEntity story);

    @Delete
    void delete(StoryEntity story);

    @Query("DELETE FROM stories WHERE id = :storyId")
    void deleteById(String storyId);

    @Query("SELECT * FROM stories WHERE personId = :personId ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> getStoriesForPerson(String personId);

    @Query("SELECT * FROM stories WHERE familyId = :familyId ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> getAllStoriesForFamily(String familyId);

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    LiveData<StoryEntity> getStoryById(String storyId);

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    StoryEntity getStoryByIdSync(String storyId);

    @Query("SELECT * FROM stories WHERE syncState = :pendingState")
    List<StoryEntity> getPendingSyncStories(int pendingState);

    // Cursor returning queries for FamilyProvider ContentProvider
    @Query("SELECT * FROM stories WHERE familyId = :familyId ORDER BY createdAt DESC")
    Cursor getStoriesCursor(String familyId);

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    Cursor getStoryCursor(String storyId);

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    Cursor getAllStoriesCursor();
}
