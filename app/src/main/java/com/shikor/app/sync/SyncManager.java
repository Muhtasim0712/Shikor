package com.shikor.app.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.data.local.daos.PersonDao;
import com.shikor.app.data.local.daos.PhotoDao;
import com.shikor.app.data.local.daos.StoryDao;
import com.shikor.app.data.local.entities.PersonEntity;
import com.shikor.app.data.local.entities.PhotoEntity;
import com.shikor.app.data.local.entities.StoryEntity;
import com.shikor.app.data.repository.FamilyRepository;
import com.shikor.app.util.AppExecutors;
import com.shikor.app.util.Prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SyncManager - Central coordinator for network state monitoring (NetworkCallback),
 * Firestore real-time snapshot listeners, bidirectional sync, and offline resilience.
 *
 * CONCEPT: ConnectivityManager.NetworkCallback, Firestore snapshot listeners,
 * offline-first Room reconciliation, and reactive sync state.
 */
public class SyncManager {

    private static final String TAG = "SyncManager";
    private static volatile SyncManager sInstance;

    public enum SyncState {
        SYNCED,
        SYNCING,
        OFFLINE,
        PENDING_CHANGES,
        ERROR
    }

    public static class SyncStatus {
        public final SyncState state;
        public final int pendingCount;
        public final long lastSyncedTimestamp;
        public final String statusMessage;

        public SyncStatus(SyncState state, int pendingCount, long lastSyncedTimestamp, String statusMessage) {
            this.state = state;
            this.pendingCount = pendingCount;
            this.lastSyncedTimestamp = lastSyncedTimestamp;
            this.statusMessage = statusMessage;
        }
    }

    private final Context appContext;
    private final AppDatabase database;
    private final PersonDao personDao;
    private final StoryDao storyDao;
    private final PhotoDao photoDao;
    private final FirebaseFirestore firestore;
    private final AppExecutors executors;
    private final Prefs prefs;

    private final MutableLiveData<SyncStatus> syncStatusLiveData = new MutableLiveData<>();
    private final AtomicBoolean isOnline = new AtomicBoolean(false);
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private long lastSyncedTimestamp = 0;

    private ListenerRegistration peopleListener;
    private ListenerRegistration storiesListener;
    private ListenerRegistration photosListener;

    private SyncManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(appContext);
        this.personDao = database.personDao();
        this.storyDao = database.storyDao();
        this.photoDao = database.photoDao();
        this.firestore = FirebaseFirestore.getInstance();
        this.executors = AppExecutors.getInstance();
        this.prefs = Prefs.getInstance(appContext);

        initNetworkMonitoring();
    }

    public static SyncManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SyncManager.class) {
                if (sInstance == null) {
                    sInstance = new SyncManager(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<SyncStatus> getSyncStatusLiveData() {
        return syncStatusLiveData;
    }

    public boolean isNetworkOnline() {
        return isOnline.get();
    }

    /**
     * Initializes NetworkCallback with ConnectivityManager to react to network changes.
     */
    private void initNetworkMonitoring() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        // Initial connection check
        boolean initiallyConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network activeNet = connectivityManager.getActiveNetwork();
            if (activeNet != null) {
                NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNet);
                initiallyConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        } else {
            android.net.NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            initiallyConnected = info != null && info.isConnected();
        }

        isOnline.set(initiallyConnected);
        updateStatus(initiallyConnected ? SyncState.SYNCED : SyncState.OFFLINE, 0,
                initiallyConnected ? "Connected and ready" : "Offline mode active");

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    Log.d(TAG, "Network became available");
                    isOnline.set(true);

                    // Post Syncing state immediately so user visibly sees reconnection transition
                    updateStatus(SyncState.SYNCING, 0, "Connected. Syncing data...");

                    // Re-attach real-time listeners and push local pending changes
                    String familyId = prefs.getFamilyId();
                    if (familyId != null && !familyId.isEmpty()) {
                        attachSnapshotListeners(familyId);
                    }
                    syncPendingChanges();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "Network connection lost");
                    isOnline.set(false);
                    detachSnapshotListeners();
                    checkPendingCountAndUpdateOffline();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error registering NetworkCallback: " + e.getMessage(), e);
        }
    }

    /**
     * Attaches Firestore real-time snapshot listeners for people, stories, and photos.
     */
    public synchronized void attachSnapshotListeners(String familyId) {
        if (familyId == null || familyId.trim().isEmpty() || !isOnline.get()) return;

        detachSnapshotListeners();
        Log.d(TAG, "Attaching real-time Firestore snapshot listeners for family: " + familyId);

        // 1. Real-time People listener
        peopleListener = firestore.collection("families")
                .document(familyId)
                .collection("people")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "People snapshot listener encountered error: " + e.getMessage());
                        return;
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        executors.diskIO().execute(() -> {
                            List<PersonEntity> incoming = new ArrayList<>();
                            for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                                    PersonEntity p = parsePersonFromDoc(dc.getDocument(), familyId);
                                    if (p != null) incoming.add(p);
                                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                    personDao.deleteById(dc.getDocument().getId());
                                }
                            }
                            if (!incoming.isEmpty()) {
                                personDao.insertAll(incoming);
                                Log.d(TAG, "Realtime sync applied: " + incoming.size() + " members updated.");
                            }
                        });
                    }
                });

        // 2. Real-time Stories listener
        storiesListener = firestore.collection("families")
                .document(familyId)
                .collection("stories")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) return;
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        executors.diskIO().execute(() -> {
                            List<StoryEntity> incomingStories = new ArrayList<>();
                            for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                                    StoryEntity s = parseStoryFromDoc(dc.getDocument(), familyId);
                                    if (s != null && personDao.getPersonByIdSync(s.getPersonId()) != null) {
                                        incomingStories.add(s);
                                    }
                                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                    storyDao.deleteById(dc.getDocument().getId());
                                }
                            }
                            if (!incomingStories.isEmpty()) {
                                storyDao.insertAll(incomingStories);
                            }
                        });
                    }
                });

        // 3. Real-time Photos listener
        photosListener = firestore.collection("families")
                .document(familyId)
                .collection("photos")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) return;
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        executors.diskIO().execute(() -> {
                            List<PhotoEntity> incomingPhotos = new ArrayList<>();
                            for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                                    PhotoEntity p = parsePhotoFromDoc(dc.getDocument(), familyId);
                                    if (p != null && personDao.getPersonByIdSync(p.getPersonId()) != null) {
                                        incomingPhotos.add(p);
                                    }
                                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                    photoDao.deleteById(dc.getDocument().getId());
                                }
                            }
                            if (!incomingPhotos.isEmpty()) {
                                photoDao.insertAll(incomingPhotos);
                            }
                        });
                    }
                });
    }

    /**
     * Detaches all active snapshot listeners to release network resources.
     */
    public synchronized void detachSnapshotListeners() {
        if (peopleListener != null) {
            peopleListener.remove();
            peopleListener = null;
        }
        if (storiesListener != null) {
            storiesListener.remove();
            storiesListener = null;
        }
        if (photosListener != null) {
            photosListener.remove();
            photosListener = null;
        }
        Log.d(TAG, "Detached Firestore snapshot listeners.");
    }

    /**
     * Uploads pending local edits (Room entities with SYNC_STATE_PENDING) to Firestore.
     */
    public void syncPendingChanges() {
        if (!isOnline.get()) {
            checkPendingCountAndUpdateOffline();
            return;
        }
        if (isSyncing.getAndSet(true)) return;

        final String familyId = prefs.getFamilyId();
        if (familyId == null || familyId.isEmpty()) {
            isSyncing.set(false);
            return;
        }

        updateStatus(SyncState.SYNCING, 0, "Syncing changes with cloud...");

        executors.diskIO().execute(() -> {
            try {
                // Ensure the "Syncing..." state is visible to user
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                List<PersonEntity> pendingPeople = personDao.getPendingSyncPeople(PersonEntity.SYNC_STATE_PENDING);
                List<StoryEntity> pendingStories = storyDao.getPendingSyncStories(StoryEntity.SYNC_STATE_PENDING);
                List<PhotoEntity> pendingPhotos = photoDao.getPendingSyncPhotos(PhotoEntity.SYNC_STATE_PENDING);

                int totalPending = pendingPeople.size() + pendingStories.size() + pendingPhotos.size();
                Log.d(TAG, "Found " + totalPending + " pending items to sync to Firestore.");

                if (totalPending == 0) {
                    lastSyncedTimestamp = System.currentTimeMillis();
                    isSyncing.set(false);
                    updateStatus(SyncState.SYNCED, 0, "All changes saved to cloud");
                    return;
                }

                // 1. Sync People
                for (PersonEntity p : pendingPeople) {
                    Map<String, Object> map = personToMap(p);
                    firestore.collection("families")
                            .document(familyId)
                            .collection("people")
                            .document(p.getId())
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                                p.setSyncState(PersonEntity.SYNC_STATE_SYNCED);
                                personDao.update(p);
                            }));
                }

                // 2. Sync Stories
                for (StoryEntity s : pendingStories) {
                    Map<String, Object> map = storyToMap(s);
                    firestore.collection("families")
                            .document(familyId)
                            .collection("stories")
                            .document(s.getId())
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                                s.setSyncState(StoryEntity.SYNC_STATE_SYNCED);
                                storyDao.update(s);
                            }));
                }

                // 3. Sync Photos
                for (PhotoEntity ph : pendingPhotos) {
                    Map<String, Object> map = photoToMap(ph);
                    firestore.collection("families")
                            .document(familyId)
                            .collection("photos")
                            .document(ph.getId())
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> executors.diskIO().execute(() -> {
                                ph.setSyncState(PhotoEntity.SYNC_STATE_SYNCED);
                                photoDao.update(ph);
                            }));
                }

                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                lastSyncedTimestamp = System.currentTimeMillis();
                isSyncing.set(false);
                updateStatus(SyncState.SYNCED, 0, "All changes saved to cloud");

            } catch (Exception e) {
                Log.e(TAG, "Error pushing pending sync: " + e.getMessage(), e);
                isSyncing.set(false);
                updateStatus(SyncState.ERROR, 0, "Sync encountered an issue: " + e.getLocalizedMessage());
            }
        });
    }

    /**
     * Called whenever local data changes (insert/update/delete) to immediately update sync chip
     * to show pending count (e.g. "1 Pending") when offline or trigger cloud sync when online.
     */
    public void notifyDataChanged() {
        executors.diskIO().execute(() -> {
            int pending = personDao.getPendingSyncPeople(PersonEntity.SYNC_STATE_PENDING).size()
                    + storyDao.getPendingSyncStories(StoryEntity.SYNC_STATE_PENDING).size()
                    + photoDao.getPendingSyncPhotos(PhotoEntity.SYNC_STATE_PENDING).size();

            if (!isOnline.get()) {
                if (pending > 0) {
                    updateStatus(SyncState.PENDING_CHANGES, pending, pending + " change(s) waiting for connection");
                } else {
                    updateStatus(SyncState.OFFLINE, 0, "Offline (Local mode)");
                }
            } else {
                if (pending > 0) {
                    syncPendingChanges();
                } else {
                    updateStatus(SyncState.SYNCED, 0, "All changes saved to cloud");
                }
            }
        });
    }

    /**
     * Triggers a full manual bidirectional sync.
     */
    public void triggerManualSync() {
        if (!isOnline.get()) {
            checkPendingCountAndUpdateOffline();
            return;
        }

        updateStatus(SyncState.SYNCING, 0, "Syncing data now...");
        syncPendingChanges();

        String familyId = prefs.getFamilyId();
        if (familyId != null && !familyId.isEmpty()) {
            FamilyRepository.getInstance(appContext).syncDownFamilyData(familyId, null);
        }
    }

    private void checkPendingCountAndUpdateOffline() {
        executors.diskIO().execute(() -> {
            int pending = personDao.getPendingSyncPeople(PersonEntity.SYNC_STATE_PENDING).size()
                    + storyDao.getPendingSyncStories(StoryEntity.SYNC_STATE_PENDING).size()
                    + photoDao.getPendingSyncPhotos(PhotoEntity.SYNC_STATE_PENDING).size();

            if (pending > 0) {
                updateStatus(SyncState.PENDING_CHANGES, pending, pending + " change(s) waiting for connection");
            } else {
                updateStatus(SyncState.OFFLINE, 0, "Offline (Local mode)");
            }
        });
    }

    private void updateStatus(SyncState state, int pending, String message) {
        executors.mainThread().execute(() -> {
            syncStatusLiveData.setValue(new SyncStatus(state, pending, lastSyncedTimestamp, message));
        });
    }

    // --- Entity Mappers ---

    private Map<String, Object> personToMap(PersonEntity p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("familyId", p.getFamilyId());
        map.put("fullName", p.getFullName());
        map.put("nickName", p.getNickName());
        map.put("gender", p.getGender());
        map.put("birthDate", p.getBirthDate());
        map.put("deathDate", p.getDeathDate());
        map.put("isDeceased", p.isDeceased());
        map.put("hometown", p.getHometown());
        map.put("occupation", p.getOccupation());
        map.put("phoneNumber", p.getPhoneNumber());
        map.put("biography", p.getBiography());
        map.put("photoPath", p.getPhotoPath());

        // Attach Base64 thumbnail to preserve photos across devices and logins
        if (p.getPhotoPath() != null) {
            java.io.File photoFile = new java.io.File(p.getPhotoPath());
            if (photoFile.exists()) {
                String base64 = com.shikor.app.util.ImageUtils.fileToBase64Thumbnail(photoFile, 256, 75);
                if (base64 != null) {
                    map.put("photoBase64", base64);
                }
            }
        }

        map.put("fatherId", p.getFatherId());
        map.put("motherId", p.getMotherId());
        map.put("spouseId", p.getSpouseId());
        map.put("marriageDate", p.getMarriageDate());
        map.put("createdByUid", p.getCreatedByUid());
        map.put("generation", p.getGeneration());
        map.put("updatedAt", p.getUpdatedAt());
        return map;
    }

    private Map<String, Object> storyToMap(StoryEntity s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("personId", s.getPersonId());
        map.put("familyId", s.getFamilyId());
        map.put("title", s.getTitle());
        map.put("body", s.getBody());
        map.put("audioPath", s.getAudioPath());
        map.put("audioDurationMs", s.getAudioDurationMs());
        map.put("photoPath", s.getPhotoPath());
        map.put("createdAt", s.getCreatedAt());
        map.put("authorUid", s.getAuthorUid());
        map.put("updatedAt", s.getUpdatedAt());
        return map;
    }

    private Map<String, Object> photoToMap(PhotoEntity ph) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", ph.getId());
        map.put("personId", ph.getPersonId());
        map.put("familyId", ph.getFamilyId());
        map.put("path", ph.getPath());
        map.put("caption", ph.getCaption());
        map.put("year", ph.getYear());
        map.put("taggedPersonIds", ph.getTaggedPersonIds());
        map.put("createdAt", ph.getCreatedAt());
        map.put("updatedAt", ph.getUpdatedAt());
        return map;
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
            String photoBase64 = doc.getString("photoBase64");
            if (photoBase64 != null && !photoBase64.trim().isEmpty()) {
                boolean fileExists = photoPath != null && new java.io.File(photoPath).exists();
                if (!fileExists) {
                    java.io.File photosDir = new java.io.File(appContext.getFilesDir(), "photos");
                    if (!photosDir.exists()) photosDir.mkdirs();
                    java.io.File destFile = new java.io.File(photosDir, "person_" + id + ".jpg");
                    if (com.shikor.app.util.ImageUtils.base64ToFile(photoBase64, destFile)) {
                        photoPath = destFile.getAbsolutePath();
                    }
                }
            }

            String fatherId = doc.getString("fatherId");
            String motherId = doc.getString("motherId");
            String spouseId = doc.getString("spouseId");
            String marriageDate = doc.getString("marriageDate");
            String createdByUid = doc.getString("createdByUid");
            Long genLong = doc.getLong("generation");
            int generation = genLong != null ? genLong.intValue() : 0;
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            return new PersonEntity(
                    id, familyId, fullName, nickName, gender, birthDate, deathDate,
                    isDeceased, hometown, occupation, phoneNumber, biography, photoPath,
                    fatherId, motherId, spouseId, marriageDate, createdByUid, generation,
                    updatedAt, PersonEntity.SYNC_STATE_SYNCED
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Person from Firestore doc: " + e.getMessage());
            return null;
        }
    }

    private StoryEntity parseStoryFromDoc(DocumentSnapshot doc, String fallbackFamilyId) {
        try {
            String id = doc.getString("id") != null ? doc.getString("id") : doc.getId();
            String personId = doc.getString("personId");
            String familyId = doc.getString("familyId") != null ? doc.getString("familyId") : fallbackFamilyId;
            String title = doc.getString("title") != null ? doc.getString("title") : "";
            String body = doc.getString("body") != null ? doc.getString("body") : "";
            String audioPath = doc.getString("audioPath");
            Long durLong = doc.getLong("audioDurationMs");
            long audioDurationMs = durLong != null ? durLong : 0;
            String photoPath = doc.getString("photoPath");
            Long createdLong = doc.getLong("createdAt");
            long createdAt = createdLong != null ? createdLong : System.currentTimeMillis();
            String authorUid = doc.getString("authorUid");
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            if (personId == null) return null;

            return new StoryEntity(
                    id, personId, familyId, title, body, audioPath, audioDurationMs,
                    photoPath, createdAt, authorUid, updatedAt, StoryEntity.SYNC_STATE_SYNCED
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Story from Firestore doc: " + e.getMessage());
            return null;
        }
    }

    private PhotoEntity parsePhotoFromDoc(DocumentSnapshot doc, String fallbackFamilyId) {
        try {
            String id = doc.getString("id") != null ? doc.getString("id") : doc.getId();
            String personId = doc.getString("personId");
            String familyId = doc.getString("familyId") != null ? doc.getString("familyId") : fallbackFamilyId;
            String path = doc.getString("path") != null ? doc.getString("path") : "";
            String caption = doc.getString("caption");
            Long yearLong = doc.getLong("year");
            int year = yearLong != null ? yearLong.intValue() : 0;
            List<String> tagged = (List<String>) doc.get("taggedPersonIds");
            Long createdLong = doc.getLong("createdAt");
            long createdAt = createdLong != null ? createdLong : System.currentTimeMillis();
            Long updatedLong = doc.getLong("updatedAt");
            long updatedAt = updatedLong != null ? updatedLong : System.currentTimeMillis();

            if (personId == null) return null;

            return new PhotoEntity(
                    id, personId, familyId, path, caption, year,
                    tagged != null ? tagged : new ArrayList<>(),
                    createdAt, updatedAt, PhotoEntity.SYNC_STATE_SYNCED
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Photo from Firestore doc: " + e.getMessage());
            return null;
        }
    }
}
