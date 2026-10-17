package com.shikor.app.data.local.daos;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shikor.app.data.local.entities.StoryEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StoryDao_Impl implements StoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StoryEntity> __insertionAdapterOfStoryEntity;

  private final EntityDeletionOrUpdateAdapter<StoryEntity> __deletionAdapterOfStoryEntity;

  private final EntityDeletionOrUpdateAdapter<StoryEntity> __updateAdapterOfStoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public StoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStoryEntity = new EntityInsertionAdapter<StoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stories` (`id`,`personId`,`familyId`,`title`,`body`,`audioPath`,`audioDurationMs`,`photoPath`,`createdAt`,`authorUid`,`updatedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getPersonId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPersonId());
        }
        if (entity.getFamilyId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getFamilyId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTitle());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBody());
        }
        if (entity.getAudioPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAudioPath());
        }
        statement.bindLong(7, entity.getAudioDurationMs());
        if (entity.getPhotoPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPhotoPath());
        }
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getAuthorUid() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAuthorUid());
        }
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindLong(12, entity.getSyncState());
      }
    };
    this.__deletionAdapterOfStoryEntity = new EntityDeletionOrUpdateAdapter<StoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `stories` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfStoryEntity = new EntityDeletionOrUpdateAdapter<StoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `stories` SET `id` = ?,`personId` = ?,`familyId` = ?,`title` = ?,`body` = ?,`audioPath` = ?,`audioDurationMs` = ?,`photoPath` = ?,`createdAt` = ?,`authorUid` = ?,`updatedAt` = ?,`syncState` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final StoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getPersonId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPersonId());
        }
        if (entity.getFamilyId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getFamilyId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTitle());
        }
        if (entity.getBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBody());
        }
        if (entity.getAudioPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAudioPath());
        }
        statement.bindLong(7, entity.getAudioDurationMs());
        if (entity.getPhotoPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPhotoPath());
        }
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getAuthorUid() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAuthorUid());
        }
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindLong(12, entity.getSyncState());
        if (entity.getId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stories WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final StoryEntity story) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStoryEntity.insert(story);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<StoryEntity> stories) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStoryEntity.insert(stories);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final StoryEntity story) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfStoryEntity.handle(story);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final StoryEntity story) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfStoryEntity.handle(story);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final String storyId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    if (storyId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, storyId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public LiveData<List<StoryEntity>> getStoriesForPerson(final String personId) {
    final String _sql = "SELECT * FROM stories WHERE personId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"stories"}, false, new Callable<List<StoryEntity>>() {
      @Override
      @Nullable
      public List<StoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfAudioDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "audioDurationMs");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<StoryEntity> _result = new ArrayList<StoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StoryEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpPersonId;
            if (_cursor.isNull(_cursorIndexOfPersonId)) {
              _tmpPersonId = null;
            } else {
              _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getString(_cursorIndexOfBody);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final long _tmpAudioDurationMs;
            _tmpAudioDurationMs = _cursor.getLong(_cursorIndexOfAudioDurationMs);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpAuthorUid;
            if (_cursor.isNull(_cursorIndexOfAuthorUid)) {
              _tmpAuthorUid = null;
            } else {
              _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new StoryEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpTitle,_tmpBody,_tmpAudioPath,_tmpAudioDurationMs,_tmpPhotoPath,_tmpCreatedAt,_tmpAuthorUid,_tmpUpdatedAt,_tmpSyncState);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<StoryEntity>> getAllStoriesForFamily(final String familyId) {
    final String _sql = "SELECT * FROM stories WHERE familyId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"stories"}, false, new Callable<List<StoryEntity>>() {
      @Override
      @Nullable
      public List<StoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfAudioDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "audioDurationMs");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<StoryEntity> _result = new ArrayList<StoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StoryEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpPersonId;
            if (_cursor.isNull(_cursorIndexOfPersonId)) {
              _tmpPersonId = null;
            } else {
              _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getString(_cursorIndexOfBody);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final long _tmpAudioDurationMs;
            _tmpAudioDurationMs = _cursor.getLong(_cursorIndexOfAudioDurationMs);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpAuthorUid;
            if (_cursor.isNull(_cursorIndexOfAuthorUid)) {
              _tmpAuthorUid = null;
            } else {
              _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new StoryEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpTitle,_tmpBody,_tmpAudioPath,_tmpAudioDurationMs,_tmpPhotoPath,_tmpCreatedAt,_tmpAuthorUid,_tmpUpdatedAt,_tmpSyncState);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<StoryEntity> getStoryById(final String storyId) {
    final String _sql = "SELECT * FROM stories WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (storyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, storyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"stories"}, false, new Callable<StoryEntity>() {
      @Override
      @Nullable
      public StoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfAudioDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "audioDurationMs");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final StoryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpPersonId;
            if (_cursor.isNull(_cursorIndexOfPersonId)) {
              _tmpPersonId = null;
            } else {
              _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpBody;
            if (_cursor.isNull(_cursorIndexOfBody)) {
              _tmpBody = null;
            } else {
              _tmpBody = _cursor.getString(_cursorIndexOfBody);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final long _tmpAudioDurationMs;
            _tmpAudioDurationMs = _cursor.getLong(_cursorIndexOfAudioDurationMs);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpAuthorUid;
            if (_cursor.isNull(_cursorIndexOfAuthorUid)) {
              _tmpAuthorUid = null;
            } else {
              _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new StoryEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpTitle,_tmpBody,_tmpAudioPath,_tmpAudioDurationMs,_tmpPhotoPath,_tmpCreatedAt,_tmpAuthorUid,_tmpUpdatedAt,_tmpSyncState);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public StoryEntity getStoryByIdSync(final String storyId) {
    final String _sql = "SELECT * FROM stories WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (storyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, storyId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
      final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
      final int _cursorIndexOfAudioDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "audioDurationMs");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final StoryEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpPersonId;
        if (_cursor.isNull(_cursorIndexOfPersonId)) {
          _tmpPersonId = null;
        } else {
          _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final String _tmpBody;
        if (_cursor.isNull(_cursorIndexOfBody)) {
          _tmpBody = null;
        } else {
          _tmpBody = _cursor.getString(_cursorIndexOfBody);
        }
        final String _tmpAudioPath;
        if (_cursor.isNull(_cursorIndexOfAudioPath)) {
          _tmpAudioPath = null;
        } else {
          _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
        }
        final long _tmpAudioDurationMs;
        _tmpAudioDurationMs = _cursor.getLong(_cursorIndexOfAudioDurationMs);
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final String _tmpAuthorUid;
        if (_cursor.isNull(_cursorIndexOfAuthorUid)) {
          _tmpAuthorUid = null;
        } else {
          _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
        }
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _result = new StoryEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpTitle,_tmpBody,_tmpAudioPath,_tmpAudioDurationMs,_tmpPhotoPath,_tmpCreatedAt,_tmpAuthorUid,_tmpUpdatedAt,_tmpSyncState);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StoryEntity> getPendingSyncStories(final int pendingState) {
    final String _sql = "SELECT * FROM stories WHERE syncState = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, pendingState);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(_cursor, "body");
      final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
      final int _cursorIndexOfAudioDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "audioDurationMs");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<StoryEntity> _result = new ArrayList<StoryEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final StoryEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpPersonId;
        if (_cursor.isNull(_cursorIndexOfPersonId)) {
          _tmpPersonId = null;
        } else {
          _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        final String _tmpBody;
        if (_cursor.isNull(_cursorIndexOfBody)) {
          _tmpBody = null;
        } else {
          _tmpBody = _cursor.getString(_cursorIndexOfBody);
        }
        final String _tmpAudioPath;
        if (_cursor.isNull(_cursorIndexOfAudioPath)) {
          _tmpAudioPath = null;
        } else {
          _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
        }
        final long _tmpAudioDurationMs;
        _tmpAudioDurationMs = _cursor.getLong(_cursorIndexOfAudioDurationMs);
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final String _tmpAuthorUid;
        if (_cursor.isNull(_cursorIndexOfAuthorUid)) {
          _tmpAuthorUid = null;
        } else {
          _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
        }
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new StoryEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpTitle,_tmpBody,_tmpAudioPath,_tmpAudioDurationMs,_tmpPhotoPath,_tmpCreatedAt,_tmpAuthorUid,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Cursor getStoriesCursor(final String familyId) {
    final String _sql = "SELECT * FROM stories WHERE familyId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @Override
  public Cursor getStoryCursor(final String storyId) {
    final String _sql = "SELECT * FROM stories WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (storyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, storyId);
    }
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @Override
  public Cursor getAllStoriesCursor() {
    final String _sql = "SELECT * FROM stories ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
