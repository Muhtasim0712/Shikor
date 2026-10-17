package com.shikor.app.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.FamilyEntity;

/**
 * FamilyDao - Data Access Object for FamilyEntity in Room.
 */
@Dao
public interface FamilyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FamilyEntity family);

    @Update
    void update(FamilyEntity family);

    @Delete
    void delete(FamilyEntity family);

    @Query("SELECT * FROM families WHERE id = :familyId LIMIT 1")
    LiveData<FamilyEntity> getFamily(String familyId);

    @Query("SELECT * FROM families WHERE id = :familyId LIMIT 1")
    FamilyEntity getFamilySync(String familyId);
}
