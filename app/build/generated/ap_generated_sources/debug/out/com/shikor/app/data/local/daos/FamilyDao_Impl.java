package com.shikor.app.data.local.daos;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shikor.app.data.local.entities.FamilyEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FamilyDao_Impl implements FamilyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FamilyEntity> __insertionAdapterOfFamilyEntity;

  private final EntityDeletionOrUpdateAdapter<FamilyEntity> __deletionAdapterOfFamilyEntity;

  private final EntityDeletionOrUpdateAdapter<FamilyEntity> __updateAdapterOfFamilyEntity;

  public FamilyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFamilyEntity = new EntityInsertionAdapter<FamilyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `families` (`id`,`name`,`code`,`adminUid`,`updatedAt`,`syncState`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final FamilyEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCode());
        }
        if (entity.getAdminUid() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAdminUid());
        }
        statement.bindLong(5, entity.getUpdatedAt());
        statement.bindLong(6, entity.getSyncState());
      }
    };
    this.__deletionAdapterOfFamilyEntity = new EntityDeletionOrUpdateAdapter<FamilyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `families` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final FamilyEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfFamilyEntity = new EntityDeletionOrUpdateAdapter<FamilyEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `families` SET `id` = ?,`name` = ?,`code` = ?,`adminUid` = ?,`updatedAt` = ?,`syncState` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final FamilyEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getCode() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCode());
        }
        if (entity.getAdminUid() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAdminUid());
        }
        statement.bindLong(5, entity.getUpdatedAt());
        statement.bindLong(6, entity.getSyncState());
        if (entity.getId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getId());
        }
      }
    };
  }

  @Override
  public void insert(final FamilyEntity family) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfFamilyEntity.insert(family);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final FamilyEntity family) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfFamilyEntity.handle(family);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final FamilyEntity family) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfFamilyEntity.handle(family);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<FamilyEntity> getFamily(final String familyId) {
    final String _sql = "SELECT * FROM families WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"families"}, false, new Callable<FamilyEntity>() {
      @Override
      @Nullable
      public FamilyEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfAdminUid = CursorUtil.getColumnIndexOrThrow(_cursor, "adminUid");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final FamilyEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCode;
            if (_cursor.isNull(_cursorIndexOfCode)) {
              _tmpCode = null;
            } else {
              _tmpCode = _cursor.getString(_cursorIndexOfCode);
            }
            final String _tmpAdminUid;
            if (_cursor.isNull(_cursorIndexOfAdminUid)) {
              _tmpAdminUid = null;
            } else {
              _tmpAdminUid = _cursor.getString(_cursorIndexOfAdminUid);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new FamilyEntity(_tmpId,_tmpName,_tmpCode,_tmpAdminUid,_tmpUpdatedAt,_tmpSyncState);
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
  public FamilyEntity getFamilySync(final String familyId) {
    final String _sql = "SELECT * FROM families WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
      final int _cursorIndexOfAdminUid = CursorUtil.getColumnIndexOrThrow(_cursor, "adminUid");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final FamilyEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpCode;
        if (_cursor.isNull(_cursorIndexOfCode)) {
          _tmpCode = null;
        } else {
          _tmpCode = _cursor.getString(_cursorIndexOfCode);
        }
        final String _tmpAdminUid;
        if (_cursor.isNull(_cursorIndexOfAdminUid)) {
          _tmpAdminUid = null;
        } else {
          _tmpAdminUid = _cursor.getString(_cursorIndexOfAdminUid);
        }
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _result = new FamilyEntity(_tmpId,_tmpName,_tmpCode,_tmpAdminUid,_tmpUpdatedAt,_tmpSyncState);
      } else {
        _result = null;
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
