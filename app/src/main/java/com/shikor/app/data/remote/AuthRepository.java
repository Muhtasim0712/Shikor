package com.shikor.app.data.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shikor.app.util.Prefs;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AuthRepository - Manages Firebase Authentication and Firestore User/Family document mapping.
 *
 * CONCEPT: Firebase Authentication & Cloud Firestore Integration
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static AuthRepository sInstance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final Context appContext;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public interface FamilyCallback {
        void onSuccess(String familyId, String familyName, String familyCode);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private AuthRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AuthRepository(context);
        }
        return sInstance;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Registers a new user with Firebase Auth and creates their user document in Firestore.
     */
    public void register(String name, String email, String password, @NonNull AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Update display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        user.updateProfile(profileUpdates);

                        // Save user doc in Firestore: users/{uid}
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("displayName", name);
                        userData.put("email", email);
                        userData.put("familyId", null);
                        userData.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Prefs.getInstance(appContext).setUser(user.getUid(), name, email);
                                    callback.onSuccess(user);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create user record", e);
                                    // User was created in Auth even if Firestore failed
                                    Prefs.getInstance(appContext).setUser(user.getUid(), name, email);
                                    callback.onSuccess(user);
                                });
                    } else {
                        callback.onError("User creation returned null");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * Signs in with email and password and loads the user's familyId.
     */
    public void login(String email, String password, @NonNull AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        fetchUserFamilyData(user.getUid(), new ActionCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(user);
                            }

                            @Override
                            public void onError(String message) {
                                // Proceed anyway; user can select or join family
                                callback.onSuccess(user);
                            }
                        });
                    } else {
                        callback.onError("Login returned null user");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * Fetches user profile data from Firestore.
     */
    public void fetchUserFamilyData(String uid, @NonNull ActionCallback callback) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String familyId = documentSnapshot.getString("familyId");
                        String name = documentSnapshot.getString("displayName");
                        String email = documentSnapshot.getString("email");
                        Prefs.getInstance(appContext).setUser(uid, name, email);

                        if (familyId != null && !familyId.isEmpty()) {
                            // Fetch family details
                            db.collection("families").document(familyId).get()
                                    .addOnSuccessListener(familyDoc -> {
                                        if (familyDoc.exists()) {
                                            String familyName = familyDoc.getString("name");
                                            String code = familyDoc.getString("code");
                                            Prefs.getInstance(appContext).setFamily(familyId, familyName, code);
                                        }
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> callback.onSuccess());
                        } else {
                            callback.onSuccess();
                        }
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * Sends password reset email.
     */
    public void sendPasswordReset(String email, @NonNull ActionCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * CONCEPT: Creates a new family in Firestore with a unique 6-character code.
     */
    public void createFamily(String familyName, @NonNull FamilyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String familyId = UUID.randomUUID().toString();
        String code = generateFamilyCode();

        List<String> memberUids = new ArrayList<>();
        memberUids.add(currentUser.getUid());

        Map<String, Object> familyData = new HashMap<>();
        familyData.put("id", familyId);
        familyData.put("name", familyName);
        familyData.put("code", code);
        familyData.put("adminUid", currentUser.getUid());
        familyData.put("memberUids", memberUids);
        familyData.put("createdAt", System.currentTimeMillis());

        db.collection("families").document(familyId)
                .set(familyData)
                .addOnSuccessListener(aVoid -> {
                    // Update user's familyId
                    db.collection("users").document(currentUser.getUid())
                            .update("familyId", familyId)
                            .addOnCompleteListener(task -> {
                                Prefs.getInstance(appContext).setFamily(familyId, familyName, code);
                                callback.onSuccess(familyId, familyName, code);
                            });
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * CONCEPT: Joins an existing family by querying Firestore for the 6-character code.
     */
    public void joinFamily(String inputCode, @NonNull FamilyCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String cleanCode = inputCode.trim().toUpperCase();
        db.collection("families")
                .whereEqualTo("code", cleanCode)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onError("Invalid family code. No family found.");
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String familyId = doc.getId();
                    String familyName = doc.getString("name");
                    String code = doc.getString("code");

                    // Add current user to memberUids
                    doc.getReference().update("memberUids", FieldValue.arrayUnion(currentUser.getUid()))
                            .addOnSuccessListener(aVoid -> {
                                // Update user's doc
                                db.collection("users").document(currentUser.getUid())
                                        .update("familyId", familyId)
                                        .addOnCompleteListener(task -> {
                                            Prefs.getInstance(appContext).setFamily(familyId, familyName, code);
                                            callback.onSuccess(familyId, familyName, code);
                                        });
                            })
                            .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
    }

    /**
     * Signs out the user and clears preferences.
     */
    public void logout() {
        auth.signOut();
        Prefs.getInstance(appContext).clearSession();
    }

    /**
     * Generates a random 6-character uppercase alphanumeric code.
     */
    private String generateFamilyCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Exclude ambiguous chars like 0, O, 1, I
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
