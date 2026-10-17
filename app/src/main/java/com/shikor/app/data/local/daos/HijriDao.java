package com.shikor.app.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shikor.app.data.local.entities.HijriCacheEntity;

/**
 * HijriDao - Data Access Object for HijriCacheEntity.
 */
@Dao
public interface HijriDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HijriCacheEntity cache);

    @Query("SELECT * FROM hijri_cache WHERE gregorianDate = :gregorianDate LIMIT 1")
    HijriCacheEntity getCachedDate(String gregorianDate);
}
