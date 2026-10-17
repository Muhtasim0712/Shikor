package com.shikor.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentSnapshot;
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

import java.util.ArrayList;
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

    public LiveData<StoryEntity> getStoryById(String storyId) {
        return storyDao.getStoryById(storyId);
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

            syncPhotoToFirestore(photo);

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    private void syncPhotoToFirestore(PhotoEntity photo) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", photo.getId());
        map.put("personId", photo.getPersonId());
        map.put("familyId", photo.getFamilyId());
        map.put("path", photo.getPath());
        map.put("caption", photo.getCaption());
        map.put("year", photo.getYear());
        map.put("taggedPersonIds", photo.getTaggedPersonIds());
        map.put("createdAt", photo.getCreatedAt());
        map.put("updatedAt", photo.getUpdatedAt());

        firestore.collection("families")
                .document(photo.getFamilyId())
                .collection("photos")
                .document(photo.getId())
                .set(map, SetOptions.merge())
                .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                    photo.setSyncState(PhotoEntity.SYNC_STATE_SYNCED);
                    photoDao.update(photo);
                }))
                .addOnFailureListener(e -> Log.w(TAG, "Photo sync to Firestore postponed (offline): " + e.getMessage()));
    }

    public void deletePhoto(PhotoEntity photo, RepositoryCallback<Void> callback) {
        executors.diskIO().execute(() -> {
            photoDao.delete(photo);

            firestore.collection("families")
                    .document(photo.getFamilyId())
                    .collection("photos")
                    .document(photo.getId())
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete photo in Firestore", e));

            executors.mainThread().execute(() -> {
                if (callback != null) callback.onSuccess(null);
            });
        });
    }

    // --- CLOUD SYNC-DOWN (FIRESTORE -> ROOM) ---

    /**
     * Pulls family metadata, members, stories, and timeline photos from Cloud Firestore
     * and inserts them into the local Room database.
     */
    public void syncDownFamilyData(String familyId, RepositoryCallback<Void> callback) {
        if (familyId == null || familyId.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid family ID");
            return;
        }

        // 1. Fetch Family metadata
        firestore.collection("families").document(familyId).get()
                .addOnSuccessListener(familyDoc -> {
                    if (familyDoc.exists()) {
                        String name = familyDoc.getString("name");
                        String code = familyDoc.getString("code");
                        String adminUid = familyDoc.getString("adminUid");
                        Long updatedLong = familyDoc.getLong("updatedAt");
                        long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

                        FamilyEntity familyEntity = new FamilyEntity(
                                familyId, name, code, adminUid, updatedAt, FamilyEntity.SYNC_STATE_SYNCED
                        );
                        executors.diskIO().execute(() -> familyDao.insert(familyEntity));
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Family metadata sync-down failed: " + e.getMessage()));

        // 2. Fetch People Collection
        firestore.collection("families")
                .document(familyId)
                .collection("people")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<PersonEntity> people = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PersonEntity person = parsePersonFromDoc(doc, familyId);
                        if (person != null) {
                            people.add(person);
                        }
                    }

                    executors.diskIO().execute(() -> {
                        if (!people.isEmpty()) {
                            personDao.insertAll(people);
                            Log.d(TAG, "Successfully synced down " + people.size() + " family members from Firestore.");
                        }

                        // 3. Once people are safely in Room, sync stories & photos (to satisfy foreign key constraints)
                        syncDownStories(familyId);
                        syncDownPhotos(familyId);

                        executors.mainThread().execute(() -> {
                            if (callback != null) callback.onSuccess(null);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync family members from Firestore: " + e.getMessage(), e);
                    if (callback != null) callback.onError(e.getLocalizedMessage());
                });
    }

    private void syncDownStories(String familyId) {
        firestore.collection("families")
                .document(familyId)
                .collection("stories")
                .get()
                .addOnSuccessListener(querySnapshot -> executors.diskIO().execute(() -> {
                    List<StoryEntity> stories = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        StoryEntity story = parseStoryFromDoc(doc, familyId);
                        if (story != null && personDao.getPersonByIdSync(story.getPersonId()) != null) {
                            stories.add(story);
                        }
                    }
                    if (!stories.isEmpty()) {
                        storyDao.insertAll(stories);
                        Log.d(TAG, "Successfully synced down " + stories.size() + " stories from Firestore.");
                    }
                }))
                .addOnFailureListener(e -> Log.w(TAG, "Stories sync-down postponed: " + e.getMessage()));
    }

    private void syncDownPhotos(String familyId) {
        firestore.collection("families")
                .document(familyId)
                .collection("photos")
                .get()
                .addOnSuccessListener(querySnapshot -> executors.diskIO().execute(() -> {
                    List<PhotoEntity> photos = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        PhotoEntity photo = parsePhotoFromDoc(doc, familyId);
                        if (photo != null && personDao.getPersonByIdSync(photo.getPersonId()) != null) {
                            photos.add(photo);
                        }
                    }
                    if (!photos.isEmpty()) {
                        photoDao.insertAll(photos);
                        Log.d(TAG, "Successfully synced down " + photos.size() + " photos from Firestore.");
                    }
                }))
                .addOnFailureListener(e -> Log.w(TAG, "Photos sync-down postponed: " + e.getMessage()));
    }

    private PersonEntity parsePersonFromDoc(DocumentSnapshot doc, String fallbackFamilyId) {
        try {
            String id = doc.getString("id") != null ? doc.getString("id") : doc.getId();
            String familyId = doc.getString("familyId") != null ? doc.getString("familyId") : fallbackFamilyId;
            String fullName = doc.getString("fullName") != null ? doc.getString("fullName") : "";
            String nickName = doc.getString("nickName");
            String gender = doc.getString("gender") != null ? doc.getString("gender") : "other";
            String birthDate = doc.getString("birthDate");
            String deathDate = doc.getString("deathDate");
            Boolean isDeceasedBool = doc.getBoolean("isDeceased");
            boolean isDeceased = isDeceasedBool != null ? isDeceasedBool : false;
            String hometown = doc.getString("hometown");
            String occupation = doc.getString("occupation");
            String phoneNumber = doc.getString("phoneNumber");
            String biography = doc.getString("biography");
            String photoPath = doc.getString("photoPath");
            String fatherId = doc.getString("fatherId");
            String motherId = doc.getString("motherId");
            String spouseId = doc.getString("spouseId");
            String marriageDate = doc.getString("marriageDate");
            String createdByUid = doc.getString("createdByUid");
            Long genLong = doc.getLong("generation");
            int generation = genLong != null ? genLong.intValue() : 0;
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            return new PersonEntity(id, familyId, fullName, nickName, gender, birthDate, deathDate,
                    isDeceased, hometown, occupation, phoneNumber, biography, photoPath, fatherId,
                    motherId, spouseId, marriageDate, createdByUid, generation, updatedAt,
                    PersonEntity.SYNC_STATE_SYNCED);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Person from Firestore doc: " + doc.getId(), e);
            return null;
        }
    }

    private StoryEntity parseStoryFromDoc(DocumentSnapshot doc, String fallbackFamilyId) {
        try {
            String id = doc.getString("id") != null ? doc.getString("id") : doc.getId();
            String personId = doc.getString("personId");
            if (personId == null || personId.isEmpty()) return null;
            String familyId = doc.getString("familyId") != null ? doc.getString("familyId") : fallbackFamilyId;
            String title = doc.getString("title") != null ? doc.getString("title") : "";
            String body = doc.getString("body");
            String audioPath = doc.getString("audioPath");
            Long durLong = doc.getLong("audioDurationMs");
            long duration = durLong != null ? durLong : 0L;
            String photoPath = doc.getString("photoPath");
            Long createdLong = doc.getLong("createdAt");
            long createdAt = createdLong != null ? createdLong : System.currentTimeMillis();
            String authorUid = doc.getString("authorUid");
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            return new StoryEntity(id, personId, familyId, title, body, audioPath, duration,
                    photoPath, createdAt, authorUid, updatedAt, StoryEntity.SYNC_STATE_SYNCED);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Story from Firestore doc: " + doc.getId(), e);
            return null;
        }
    }

    private PhotoEntity parsePhotoFromDoc(DocumentSnapshot doc, String fallbackFamilyId) {
        try {
            String id = doc.getString("id") != null ? doc.getString("id") : doc.getId();
            String personId = doc.getString("personId");
            if (personId == null || personId.isEmpty()) return null;
            String familyId = doc.getString("familyId") != null ? doc.getString("familyId") : fallbackFamilyId;
            String path = doc.getString("path") != null ? doc.getString("path") : "";
            String caption = doc.getString("caption");
            Long yearLong = doc.getLong("year");
            int year = yearLong != null ? yearLong.intValue() : 0;
            @SuppressWarnings("unchecked")
            List<String> taggedIds = (List<String>) doc.get("taggedPersonIds");
            if (taggedIds == null) taggedIds = new ArrayList<>();
            Long createdLong = doc.getLong("createdAt");
            long createdAt = createdLong != null ? createdLong : System.currentTimeMillis();
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            return new PhotoEntity(id, personId, familyId, path, caption, year, taggedIds,
                    createdAt, updatedAt, PhotoEntity.SYNC_STATE_SYNCED);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Photo from Firestore doc: " + doc.getId(), e);
            return null;
        }
    }
}
