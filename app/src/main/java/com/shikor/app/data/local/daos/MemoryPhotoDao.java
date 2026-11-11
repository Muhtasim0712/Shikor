package com.shikor.app.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.MemoryPhotoEntity;

import java.util.List;

/**
 * MemoryPhotoDao - Data Access Object for MemoryPhotoEntity in Room (Phase 1).
 */
@Dao
public interface MemoryPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryPhotoEntity photo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryPhotoEntity> photos);

    @Update
    void update(MemoryPhotoEntity photo);

    @Delete
    void delete(MemoryPhotoEntity photo);

    @Query("SELECT * FROM memory_photos WHERE memoryId = :memoryId ORDER BY `index` ASC")
    LiveData<List<MemoryPhotoEntity>> getPhotosForMemory(String memoryId);

    @Query("SELECT * FROM memory_photos WHERE memoryId = :memoryId ORDER BY `index` ASC")
    List<MemoryPhotoEntity> getPhotosForMemorySync(String memoryId);

    @Query("DELETE FROM memory_photos WHERE memoryId = :memoryId")
    void deletePhotosForMemory(String memoryId);
}
