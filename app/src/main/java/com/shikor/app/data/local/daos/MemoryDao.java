package com.shikor.app.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.MemoryEntity;

import java.util.List;

/**
 * MemoryDao - Data Access Object for MemoryEntity in Room (Phase 1).
 */
@Dao
public interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryEntity memory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryEntity> memories);

    @Update
    void update(MemoryEntity memory);

    @Delete
    void delete(MemoryEntity memory);

    @Query("DELETE FROM memories WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM memories WHERE familyId = :familyId ORDER BY eventDate DESC")
    LiveData<List<MemoryEntity>> getMemoriesForFamily(String familyId);

    @Query("SELECT * FROM memories WHERE familyId = :familyId ORDER BY eventDate DESC")
    List<MemoryEntity> getMemoriesForFamilySync(String familyId);

    @Query("SELECT * FROM memories ORDER BY eventDate DESC")
    LiveData<List<MemoryEntity>> getAllMemoriesGlobal();

    @Query("SELECT * FROM memories ORDER BY eventDate DESC")
    List<MemoryEntity> getAllMemoriesGlobalSync();

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    LiveData<MemoryEntity> getMemoryById(String id);

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    MemoryEntity getMemoryByIdSync(String id);

    @Query("SELECT * FROM memories WHERE syncState = :state")
    List<MemoryEntity> getMemoriesBySyncState(int state);

    @Query("SELECT * FROM memories WHERE strftime('%m-%d', eventDate / 1000, 'unixepoch', 'localtime') = :monthDay AND eventDate < :startOfTodayMillis ORDER BY eventDate DESC")
    List<MemoryEntity> getMemoriesOnThisDay(String monthDay, long startOfTodayMillis);

    @Query("SELECT COUNT(*) FROM memories")
    int getMemoryCountSync();

    @Query("SELECT SUM(audioDurationMs) FROM memories")
    long getTotalAudioMinutesSync();
}
