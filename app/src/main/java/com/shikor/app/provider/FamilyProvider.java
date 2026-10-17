package com.shikor.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shikor.app.data.local.AppDatabase;
import com.shikor.app.util.Prefs;

/**
 * FamilyProvider - Custom ContentProvider exposing family member data for IPC and internal tools.
 * Protected by signature-level permission: com.shikor.app.permission.READ_FAMILY.
 *
 * CONCEPT: ContentProvider with UriMatcher, Cursor return, and URI notification observation.
 */
public class FamilyProvider extends ContentProvider {

    private static final String TAG = "FamilyProvider";

    public static final String AUTHORITY = "com.shikor.app.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/people");

    private static final int CODE_PEOPLE = 1;
    private static final int CODE_PERSON_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "people", CODE_PEOPLE);
        URI_MATCHER.addURI(AUTHORITY, "people/*", CODE_PERSON_ID);
    }

    private AppDatabase database;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (database == null && getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        if (database == null) return null;

        int match = URI_MATCHER.match(uri);
        Cursor cursor;

        switch (match) {
            case CODE_PEOPLE:
                String familyId = null;
                if (getContext() != null) {
                    familyId = Prefs.getInstance(getContext()).getFamilyId();
                }
                if (familyId != null && !familyId.isEmpty()) {
                    cursor = database.personDao().getPeopleCursor(familyId);
                } else {
                    cursor = database.personDao().getAllPeopleCursor();
                }
                break;

            case CODE_PERSON_ID:
                String personId = uri.getLastPathSegment();
                if (personId != null) {
                    cursor = database.personDao().getPersonCursor(personId);
                } else {
                    cursor = null;
                }
                break;

            default:
                Log.w(TAG, "Unknown URI queried: " + uri);
                return null;
        }

        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = URI_MATCHER.match(uri);
        switch (match) {
            case CODE_PEOPLE:
                return "vnd.android.cursor.dir/vnd.com.shikor.app.people";
            case CODE_PERSON_ID:
                return "vnd.android.cursor.item/vnd.com.shikor.app.person";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Read-only ContentProvider for security
        throw new UnsupportedOperationException("Insert not supported on FamilyProvider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported on FamilyProvider");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported on FamilyProvider");
    }
}
