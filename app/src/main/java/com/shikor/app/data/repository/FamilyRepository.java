package com.shikor.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.FamilyDao;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.PhotoDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.FamilyEntity;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.util.AppExecutors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FamilyRepository - Single repository mediating between Room and Cloud Firestore.
 *
 * CONCEPT: Repository Pattern with Room as Single Source of Truth and Firestore as sync target.
 */
public class FamilyRepository {

    private static final String TAG = "FamilyRepository";
    private static FamilyRepository sInstance;

    private final AppDatabase database;
    private final PersonDao personDao;
    private final StoryDao storyDao;
    private final PhotoDao photoDao;
    private final FamilyDao familyDao;
    private final AppExecutors executors;
    private final FirebaseFirestore firestore;

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private FamilyRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.personDao = database.personDao();
        this.storyDao = database.storyDao();
        this.photoDao = database.photoDao();
        this.familyDao = database.familyDao();
        this.executors = AppExecutors.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FamilyRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FamilyRepository(context);
        }
        return sInstance;
    }

    // --- PERSON QUERIES ---

    public LiveData<List<PersonEntity>> getAllPeople(String familyId) {
        return personDao.getAllPeople(familyId);
    }

    public LiveData<PersonEntity> getPersonById(String personId) {
        return personDao.getPersonById(personId);
    }

    public LiveData<List<PersonEntity>> searchPeople(String familyId, String query) {
        return personDao.searchPeople(familyId, query);
    }

    public LiveData<List<PersonEntity>> getPeopleByStatus(String familyId, boolean isDeceased) {
        return personDao.getPeopleByStatus(familyId, isDeceased);
    }

    public LiveData<List<PersonEntity>> getPeopleByGeneration(String familyId, int generation) {
        return personDao.getPeopleByGeneration(familyId, generation);
    }

    public LiveData<List<PersonEntity>> getPeopleByHometown(String familyId, String hometown) {
        return personDao.getPeopleByHometown(familyId, hometown);
    }

    public LiveData<List<PersonEntity>> getChildren(String familyId, String parentId) {
        return personDao.getChildren(familyId, parentId);
    }

    public LiveData<List<PersonEntity>> getSiblings(String familyId, String personId, String fatherId, String motherId) {
        return personDao.getSiblings(familyId, personId, fatherId, motherId);
    }

    // --- PERSON CRUD WITH OFFLINE-FIRST FIRESTORE SYNC ---

    public void insertPerson(PersonEntity person, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            person.setUpdatedAt(System.currentTimeMillis());
            person.setSyncState(PersonEntity.SYNC_STATE_PENDING);
            personDao.insert(person);

            // Sync to Firestore in background
            syncPersonToFirestore(person);

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    public void updatePerson(PersonEntity person, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            person.setUpdatedAt(System.currentTimeMillis());
            person.setSyncState(PersonEntity.SYNC_STATE_PENDING);
            personDao.update(person);

            // Sync to Firestore
            syncPersonToFirestore(person);

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    public void deletePerson(PersonEntity person, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            personDao.delete(person);

            // Delete from Firestore
            firestore.collection("families")
                    .document(person.getFamilyId())
                    .collection("people")
                    .document(person.getId())
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete person in Firestore", e));

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    private void syncPersonToFirestore(PersonEntity person) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", person.getId());
        map.put("familyId", person.getFamilyId());
        map.put("fullName", person.getFullName());
        map.put("nickName", person.getNickName());
        map.put("gender", person.getGender());
        map.put("birthDate", person.getBirthDate());
        map.put("deathDate", person.getDeathDate());
        map.put("isDeceased", person.isDeceased());
        map.put("hometown", person.getHometown());
        map.put("occupation", person.getOccupation());
        map.put("phoneNumber", person.getPhoneNumber());
        map.put("biography", person.getBiography());
        map.put("photoPath", person.getPhotoPath());
        map.put("fatherId", person.getFatherId());
        map.put("motherId", person.getMotherId());
        map.put("spouseId", person.getSpouseId());
        map.put("marriageDate", person.getMarriageDate());
        map.put("createdByUid", person.getCreatedByUid());
        map.put("generation", person.getGeneration());
        map.put("updatedAt", person.getUpdatedAt());

        firestore.collection("families")
                .document(person.getFamilyId())
                .collection("people")
                .document(person.getId())
                .set(map, SetOptions.merge())
                .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                    person.setSyncState(PersonEntity.SYNC_STATE_SYNCED);
                    personDao.update(person);
                }))
                .addOnFailureListener(e -> Log.w(TAG, "Person sync to Firestore postponed (offline): " + e.getMessage()));
    }

    // --- STORY QUERIES & CRUD ---

    public LiveData<List<StoryEntity>> getStoriesForPerson(String personId) {
        return storyDao.getStoriesForPerson(personId);
    }

    public LiveData<List<StoryEntity>> getAllStoriesForFamily(String familyId) {
        return storyDao.getAllStoriesForFamily(familyId);
    }

    public void insertStory(StoryEntity story, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            story.setUpdatedAt(System.currentTimeMillis());
            story.setSyncState(StoryEntity.SYNC_STATE_PENDING);
            storyDao.insert(story);

            syncStoryToFirestore(story);

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    public void deleteStory(StoryEntity story, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            storyDao.delete(story);

            firestore.collection("families")
                    .document(story.getFamilyId())
                    .collection("stories")
                    .document(story.getId())
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete story in Firestore", e));

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    private void syncStoryToFirestore(StoryEntity story) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", story.getId());
        map.put("personId", story.getPersonId());
        map.put("familyId", story.getFamilyId());
        map.put("title", story.getTitle());
        map.put("body", story.getBody());
        map.put("audioPath", story.getAudioPath());
        map.put("audioDurationMs", story.getAudioDurationMs());
        map.put("photoPath", story.getPhotoPath());
        map.put("createdAt", story.getCreatedAt());
        map.put("authorUid", story.getAuthorUid());
        map.put("updatedAt", story.getUpdatedAt());

        firestore.collection("families")
                .document(story.getFamilyId())
                .collection("stories")
                .document(story.getId())
                .set(map, SetOptions.merge())
                .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                    story.setSyncState(StoryEntity.SYNC_STATE_SYNCED);
                    storyDao.update(story);
                }))
                .addOnFailureListener(e -> Log.w(TAG, "Story sync to Firestore postponed (offline): " + e.getMessage()));
    }

    // --- PHOTO QUERIES & CRUD ---

    public LiveData<List<PhotoEntity>> getTimelinePhotos(String familyId) {
        return photoDao.getTimelinePhotos(familyId);
    }

    public void insertPhoto(PhotoEntity photo, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            photo.setUpdatedAt(System.currentTimeMillis());
            photo.setSyncState(PhotoEntity.SYNC_STATE_PENDING);
            photoDao.insert(photo);

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }
}
