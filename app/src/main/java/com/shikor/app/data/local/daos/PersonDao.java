package com.shikor.app.data.local.daos;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shikor.app.data.local.entities.PersonEntity;

import java.util.List;

/**
 * PersonDao - Data Access Object for PersonEntity in Room.
 * CONCEPT: Room DAOs with LiveData, search (LIKE), relationship queries, and Cursor support.
 */
@Dao
public interface PersonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PersonEntity person);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PersonEntity> people);

    @Update
    void update(PersonEntity person);

    @Delete
    void delete(PersonEntity person);

    @Query("DELETE FROM people WHERE id = :personId")
    void deleteById(String personId);

    // LiveData Queries for reactive UI updates
    @Query("SELECT * FROM people WHERE familyId = :familyId ORDER BY generation ASC, birthDate ASC")
    LiveData<List<PersonEntity>> getAllPeople(String familyId);

    @Query("SELECT * FROM people WHERE familyId = :familyId ORDER BY generation ASC, birthDate ASC")
    List<PersonEntity> getAllPeopleSync(String familyId);

    @Query("SELECT * FROM people WHERE id = :personId LIMIT 1")
    LiveData<PersonEntity> getPersonById(String personId);

    @Query("SELECT * FROM people WHERE id = :personId LIMIT 1")
    PersonEntity getPersonByIdSync(String personId);

    // CONCEPT: Search using SQL LIKE operator
    @Query("SELECT * FROM people WHERE familyId = :familyId AND " +
           "(fullName LIKE '%' || :query || '%' OR nickName LIKE '%' || :query || '%' OR hometown LIKE '%' || :query || '%') " +
           "ORDER BY fullName ASC")
    LiveData<List<PersonEntity>> searchPeople(String familyId, String query);

    // Filter by living status
    @Query("SELECT * FROM people WHERE familyId = :familyId AND isDeceased = :isDeceased ORDER BY fullName ASC")
    LiveData<List<PersonEntity>> getPeopleByStatus(String familyId, boolean isDeceased);

    // Filter by generation
    @Query("SELECT * FROM people WHERE familyId = :familyId AND generation = :generation ORDER BY fullName ASC")
    LiveData<List<PersonEntity>> getPeopleByGeneration(String familyId, int generation);

    // Filter by hometown
    @Query("SELECT * FROM people WHERE familyId = :familyId AND hometown = :hometown ORDER BY fullName ASC")
    LiveData<List<PersonEntity>> getPeopleByHometown(String familyId, String hometown);

    // CONCEPT: Relationship Queries for Children and Siblings
    @Query("SELECT * FROM people WHERE (fatherId = :parentId OR motherId = :parentId) AND familyId = :familyId ORDER BY birthDate ASC")
    LiveData<List<PersonEntity>> getChildren(String familyId, String parentId);

    @Query("SELECT * FROM people WHERE (fatherId = :parentId OR motherId = :parentId) AND familyId = :familyId ORDER BY birthDate ASC")
    List<PersonEntity> getChildrenSync(String familyId, String parentId);

    @Query("SELECT * FROM people WHERE id != :personId AND familyId = :familyId AND " +
           "((:fatherId IS NOT NULL AND fatherId = :fatherId) OR (:motherId IS NOT NULL AND motherId = :motherId)) " +
           "ORDER BY birthDate ASC")
    LiveData<List<PersonEntity>> getSiblings(String familyId, String personId, String fatherId, String motherId);

    @Query("SELECT * FROM people WHERE id != :personId AND familyId = :familyId AND " +
           "((:fatherId IS NOT NULL AND fatherId = :fatherId) OR (:motherId IS NOT NULL AND motherId = :motherId)) " +
           "ORDER BY birthDate ASC")
    List<PersonEntity> getSiblingsSync(String familyId, String personId, String fatherId, String motherId);

    // Sync queries
    @Query("SELECT * FROM people WHERE syncState = :pendingState")
    List<PersonEntity> getPendingSyncPeople(int pendingState);

    // CONCEPT: Cursor returning queries for FamilyProvider ContentProvider
    @Query("SELECT * FROM people WHERE familyId = :familyId ORDER BY fullName ASC")
    Cursor getPeopleCursor(String familyId);

    @Query("SELECT * FROM people WHERE id = :personId LIMIT 1")
    Cursor getPersonCursor(String personId);

    @Query("SELECT * FROM people ORDER BY fullName ASC")
    Cursor getAllPeopleCursor();
}
