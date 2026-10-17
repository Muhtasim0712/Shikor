package com.shikor.app.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.PhotoEntity;

import java.util.List;

/**
 * PhotoDao - Data Access Object for PhotoEntity in Room.
 */
@Dao
public interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhotoEntity photo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PhotoEntity> photos);

    @Update
    void update(PhotoEntity photo);

    @Delete
    void delete(PhotoEntity photo);

    @Query("DELETE FROM photos WHERE id = :photoId")
    void deleteById(String photoId);

    @Query("SELECT * FROM photos WHERE familyId = :familyId ORDER BY year DESC, createdAt DESC")
    LiveData<List<PhotoEntity>> getTimelinePhotos(String familyId);

    @Query("SELECT * FROM photos WHERE personId = :personId ORDER BY year DESC, createdAt DESC")
    LiveData<List<PhotoEntity>> getPhotosForPerson(String personId);

    @Query("SELECT * FROM photos WHERE id = :photoId LIMIT 1")
    LiveData<PhotoEntity> getPhotoById(String photoId);

    @Query("SELECT * FROM photos WHERE syncState = :pendingState")
    List<PhotoEntity> getPendingSyncPhotos(int pendingState);
}
