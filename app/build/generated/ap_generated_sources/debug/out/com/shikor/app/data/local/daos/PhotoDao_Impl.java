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
import com.shikor.app.data.local.converters.StringListConverter;
import com.shikor.app.data.local.entities.PhotoEntity;
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
public final class PhotoDao_Impl implements PhotoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PhotoEntity> __insertionAdapterOfPhotoEntity;

  private final EntityDeletionOrUpdateAdapter<PhotoEntity> __deletionAdapterOfPhotoEntity;

  private final EntityDeletionOrUpdateAdapter<PhotoEntity> __updateAdapterOfPhotoEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public PhotoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPhotoEntity = new EntityInsertionAdapter<PhotoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `photos` (`id`,`personId`,`familyId`,`path`,`caption`,`year`,`taggedPersonIds`,`createdAt`,`updatedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PhotoEntity entity) {
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
        if (entity.getPath() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPath());
        }
        if (entity.getCaption() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCaption());
        }
        statement.bindLong(6, entity.getYear());
        final String _tmp = StringListConverter.fromList(entity.getTaggedPersonIds());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindLong(10, entity.getSyncState());
      }
    };
    this.__deletionAdapterOfPhotoEntity = new EntityDeletionOrUpdateAdapter<PhotoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `photos` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PhotoEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfPhotoEntity = new EntityDeletionOrUpdateAdapter<PhotoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `photos` SET `id` = ?,`personId` = ?,`familyId` = ?,`path` = ?,`caption` = ?,`year` = ?,`taggedPersonIds` = ?,`createdAt` = ?,`updatedAt` = ?,`syncState` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PhotoEntity entity) {
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
        if (entity.getPath() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPath());
        }
        if (entity.getCaption() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCaption());
        }
        statement.bindLong(6, entity.getYear());
        final String _tmp = StringListConverter.fromList(entity.getTaggedPersonIds());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindLong(10, entity.getSyncState());
        if (entity.getId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM photos WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final PhotoEntity photo) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPhotoEntity.insert(photo);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<PhotoEntity> photos) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPhotoEntity.insert(photos);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final PhotoEntity photo) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPhotoEntity.handle(photo);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final PhotoEntity photo) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfPhotoEntity.handle(photo);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final String photoId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    if (photoId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, photoId);
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
  public LiveData<List<PhotoEntity>> getTimelinePhotos(final String familyId) {
    final String _sql = "SELECT * FROM photos WHERE familyId = ? ORDER BY year DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"photos"}, false, new Callable<List<PhotoEntity>>() {
      @Override
      @Nullable
      public List<PhotoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfTaggedPersonIds = CursorUtil.getColumnIndexOrThrow(_cursor, "taggedPersonIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PhotoEntity> _result = new ArrayList<PhotoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoEntity _item;
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
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final String _tmpCaption;
            if (_cursor.isNull(_cursorIndexOfCaption)) {
              _tmpCaption = null;
            } else {
              _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            }
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final List<String> _tmpTaggedPersonIds;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTaggedPersonIds)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTaggedPersonIds);
            }
            _tmpTaggedPersonIds = StringListConverter.toList(_tmp);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PhotoEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpPath,_tmpCaption,_tmpYear,_tmpTaggedPersonIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PhotoEntity>> getPhotosForPerson(final String personId) {
    final String _sql = "SELECT * FROM photos WHERE personId = ? ORDER BY year DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"photos"}, false, new Callable<List<PhotoEntity>>() {
      @Override
      @Nullable
      public List<PhotoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfTaggedPersonIds = CursorUtil.getColumnIndexOrThrow(_cursor, "taggedPersonIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PhotoEntity> _result = new ArrayList<PhotoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoEntity _item;
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
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final String _tmpCaption;
            if (_cursor.isNull(_cursorIndexOfCaption)) {
              _tmpCaption = null;
            } else {
              _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            }
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final List<String> _tmpTaggedPersonIds;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTaggedPersonIds)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTaggedPersonIds);
            }
            _tmpTaggedPersonIds = StringListConverter.toList(_tmp);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PhotoEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpPath,_tmpCaption,_tmpYear,_tmpTaggedPersonIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<PhotoEntity> getPhotoById(final String photoId) {
    final String _sql = "SELECT * FROM photos WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (photoId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, photoId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"photos"}, false, new Callable<PhotoEntity>() {
      @Override
      @Nullable
      public PhotoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfTaggedPersonIds = CursorUtil.getColumnIndexOrThrow(_cursor, "taggedPersonIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final PhotoEntity _result;
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
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final String _tmpCaption;
            if (_cursor.isNull(_cursorIndexOfCaption)) {
              _tmpCaption = null;
            } else {
              _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            }
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final List<String> _tmpTaggedPersonIds;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTaggedPersonIds)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTaggedPersonIds);
            }
            _tmpTaggedPersonIds = StringListConverter.toList(_tmp);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new PhotoEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpPath,_tmpCaption,_tmpYear,_tmpTaggedPersonIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpSyncState);
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
  public List<PhotoEntity> getPendingSyncPhotos(final int pendingState) {
    final String _sql = "SELECT * FROM photos WHERE syncState = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, pendingState);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
      final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
      final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
      final int _cursorIndexOfTaggedPersonIds = CursorUtil.getColumnIndexOrThrow(_cursor, "taggedPersonIds");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PhotoEntity> _result = new ArrayList<PhotoEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PhotoEntity _item;
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
        final String _tmpPath;
        if (_cursor.isNull(_cursorIndexOfPath)) {
          _tmpPath = null;
        } else {
          _tmpPath = _cursor.getString(_cursorIndexOfPath);
        }
        final String _tmpCaption;
        if (_cursor.isNull(_cursorIndexOfCaption)) {
          _tmpCaption = null;
        } else {
          _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
        }
        final int _tmpYear;
        _tmpYear = _cursor.getInt(_cursorIndexOfYear);
        final List<String> _tmpTaggedPersonIds;
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfTaggedPersonIds)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfTaggedPersonIds);
        }
        _tmpTaggedPersonIds = StringListConverter.toList(_tmp);
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PhotoEntity(_tmpId,_tmpPersonId,_tmpFamilyId,_tmpPath,_tmpCaption,_tmpYear,_tmpTaggedPersonIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
