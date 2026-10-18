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
import com.shikor.app.data.local.entities.PersonEntity;
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
public final class PersonDao_Impl implements PersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PersonEntity> __insertionAdapterOfPersonEntity;

  private final EntityDeletionOrUpdateAdapter<PersonEntity> __deletionAdapterOfPersonEntity;

  private final EntityDeletionOrUpdateAdapter<PersonEntity> __updateAdapterOfPersonEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public PersonDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPersonEntity = new EntityInsertionAdapter<PersonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `people` (`id`,`familyId`,`fullName`,`nickName`,`gender`,`birthDate`,`deathDate`,`isDeceased`,`hometown`,`occupation`,`phoneNumber`,`biography`,`photoPath`,`fatherId`,`motherId`,`spouseId`,`marriageDate`,`createdByUid`,`generation`,`updatedAt`,`syncState`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PersonEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getFamilyId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFamilyId());
        }
        if (entity.getFullName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getFullName());
        }
        if (entity.getNickName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getNickName());
        }
        if (entity.getGender() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getGender());
        }
        if (entity.getBirthDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBirthDate());
        }
        if (entity.getDeathDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDeathDate());
        }
        final int _tmp = entity.isDeceased() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getHometown() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getHometown());
        }
        if (entity.getOccupation() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getOccupation());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getPhoneNumber());
        }
        if (entity.getBiography() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getBiography());
        }
        if (entity.getPhotoPath() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPhotoPath());
        }
        if (entity.getFatherId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getFatherId());
        }
        if (entity.getMotherId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getMotherId());
        }
        if (entity.getSpouseId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSpouseId());
        }
        if (entity.getMarriageDate() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getMarriageDate());
        }
        if (entity.getCreatedByUid() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCreatedByUid());
        }
        statement.bindLong(19, entity.getGeneration());
        statement.bindLong(20, entity.getUpdatedAt());
        statement.bindLong(21, entity.getSyncState());
      }
    };
    this.__deletionAdapterOfPersonEntity = new EntityDeletionOrUpdateAdapter<PersonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `people` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PersonEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfPersonEntity = new EntityDeletionOrUpdateAdapter<PersonEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `people` SET `id` = ?,`familyId` = ?,`fullName` = ?,`nickName` = ?,`gender` = ?,`birthDate` = ?,`deathDate` = ?,`isDeceased` = ?,`hometown` = ?,`occupation` = ?,`phoneNumber` = ?,`biography` = ?,`photoPath` = ?,`fatherId` = ?,`motherId` = ?,`spouseId` = ?,`marriageDate` = ?,`createdByUid` = ?,`generation` = ?,`updatedAt` = ?,`syncState` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PersonEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getFamilyId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFamilyId());
        }
        if (entity.getFullName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getFullName());
        }
        if (entity.getNickName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getNickName());
        }
        if (entity.getGender() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getGender());
        }
        if (entity.getBirthDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBirthDate());
        }
        if (entity.getDeathDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDeathDate());
        }
        final int _tmp = entity.isDeceased() ? 1 : 0;
        statement.bindLong(8, _tmp);
        if (entity.getHometown() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getHometown());
        }
        if (entity.getOccupation() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getOccupation());
        }
        if (entity.getPhoneNumber() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getPhoneNumber());
        }
        if (entity.getBiography() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getBiography());
        }
        if (entity.getPhotoPath() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPhotoPath());
        }
        if (entity.getFatherId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getFatherId());
        }
        if (entity.getMotherId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getMotherId());
        }
        if (entity.getSpouseId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSpouseId());
        }
        if (entity.getMarriageDate() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getMarriageDate());
        }
        if (entity.getCreatedByUid() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCreatedByUid());
        }
        statement.bindLong(19, entity.getGeneration());
        statement.bindLong(20, entity.getUpdatedAt());
        statement.bindLong(21, entity.getSyncState());
        if (entity.getId() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM people WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final PersonEntity person) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPersonEntity.insert(person);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<PersonEntity> people) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPersonEntity.insert(people);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final PersonEntity person) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPersonEntity.handle(person);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final PersonEntity person) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfPersonEntity.handle(person);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final String personId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    if (personId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, personId);
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
  public LiveData<List<PersonEntity>> getAllPeople(final String familyId) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? ORDER BY generation ASC, birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public List<PersonEntity> getAllPeopleSync(final String familyId) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? ORDER BY generation ASC, birthDate ASC";
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
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PersonEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PersonEntity> getAllPeopleGlobalSync() {
    final String _sql = "SELECT * FROM people ORDER BY generation ASC, birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PersonEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<PersonEntity> getPersonById(final String personId) {
    final String _sql = "SELECT * FROM people WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<PersonEntity>() {
      @Override
      @Nullable
      public PersonEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final PersonEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public PersonEntity getPersonByIdSync(final String personId) {
    final String _sql = "SELECT * FROM people WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final PersonEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _result = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PersonEntity>> searchPeople(final String familyId, final String query) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? AND (fullName LIKE '%' || ? || '%' OR nickName LIKE '%' || ? || '%' OR hometown LIKE '%' || ? || '%') ORDER BY fullName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 3;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 4;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PersonEntity>> getPeopleByStatus(final String familyId,
      final boolean isDeceased) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? AND isDeceased = ? ORDER BY fullName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 2;
    final int _tmp = isDeceased ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp_1 != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PersonEntity>> getPeopleByGeneration(final String familyId,
      final int generation) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? AND generation = ? ORDER BY fullName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, generation);
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PersonEntity>> getPeopleByHometown(final String familyId,
      final String hometown) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? AND hometown = ? ORDER BY fullName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 2;
    if (hometown == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, hometown);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public LiveData<List<PersonEntity>> getChildren(final String familyId, final String parentId) {
    final String _sql = "SELECT * FROM people WHERE (fatherId = ? OR motherId = ?) AND familyId = ? ORDER BY birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (parentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, parentId);
    }
    _argIndex = 2;
    if (parentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, parentId);
    }
    _argIndex = 3;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public List<PersonEntity> getChildrenSync(final String familyId, final String parentId) {
    final String _sql = "SELECT * FROM people WHERE (fatherId = ? OR motherId = ?) AND familyId = ? ORDER BY birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (parentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, parentId);
    }
    _argIndex = 2;
    if (parentId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, parentId);
    }
    _argIndex = 3;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PersonEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PersonEntity>> getSiblings(final String familyId, final String personId,
      final String fatherId, final String motherId) {
    final String _sql = "SELECT * FROM people WHERE id != ? AND familyId = ? AND ((? IS NOT NULL AND fatherId = ?) OR (? IS NOT NULL AND motherId = ?)) ORDER BY birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    _argIndex = 2;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 3;
    if (fatherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fatherId);
    }
    _argIndex = 4;
    if (fatherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fatherId);
    }
    _argIndex = 5;
    if (motherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, motherId);
    }
    _argIndex = 6;
    if (motherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, motherId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"people"}, false, new Callable<List<PersonEntity>>() {
      @Override
      @Nullable
      public List<PersonEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
          final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
          final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
          final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
          final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
          final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
          final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
          final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
          final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpFamilyId;
            if (_cursor.isNull(_cursorIndexOfFamilyId)) {
              _tmpFamilyId = null;
            } else {
              _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            }
            final String _tmpFullName;
            if (_cursor.isNull(_cursorIndexOfFullName)) {
              _tmpFullName = null;
            } else {
              _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            }
            final String _tmpNickName;
            if (_cursor.isNull(_cursorIndexOfNickName)) {
              _tmpNickName = null;
            } else {
              _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
            }
            final String _tmpGender;
            if (_cursor.isNull(_cursorIndexOfGender)) {
              _tmpGender = null;
            } else {
              _tmpGender = _cursor.getString(_cursorIndexOfGender);
            }
            final String _tmpBirthDate;
            if (_cursor.isNull(_cursorIndexOfBirthDate)) {
              _tmpBirthDate = null;
            } else {
              _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
            }
            final String _tmpDeathDate;
            if (_cursor.isNull(_cursorIndexOfDeathDate)) {
              _tmpDeathDate = null;
            } else {
              _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
            }
            final boolean _tmpIsDeceased;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
            _tmpIsDeceased = _tmp != 0;
            final String _tmpHometown;
            if (_cursor.isNull(_cursorIndexOfHometown)) {
              _tmpHometown = null;
            } else {
              _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
            }
            final String _tmpOccupation;
            if (_cursor.isNull(_cursorIndexOfOccupation)) {
              _tmpOccupation = null;
            } else {
              _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
            }
            final String _tmpPhoneNumber;
            if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
              _tmpPhoneNumber = null;
            } else {
              _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            }
            final String _tmpBiography;
            if (_cursor.isNull(_cursorIndexOfBiography)) {
              _tmpBiography = null;
            } else {
              _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
            }
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final String _tmpFatherId;
            if (_cursor.isNull(_cursorIndexOfFatherId)) {
              _tmpFatherId = null;
            } else {
              _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
            }
            final String _tmpMotherId;
            if (_cursor.isNull(_cursorIndexOfMotherId)) {
              _tmpMotherId = null;
            } else {
              _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
            }
            final String _tmpSpouseId;
            if (_cursor.isNull(_cursorIndexOfSpouseId)) {
              _tmpSpouseId = null;
            } else {
              _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
            }
            final String _tmpMarriageDate;
            if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
              _tmpMarriageDate = null;
            } else {
              _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
            }
            final String _tmpCreatedByUid;
            if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
              _tmpCreatedByUid = null;
            } else {
              _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
            }
            final int _tmpGeneration;
            _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
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
  public List<PersonEntity> getSiblingsSync(final String familyId, final String personId,
      final String fatherId, final String motherId) {
    final String _sql = "SELECT * FROM people WHERE id != ? AND familyId = ? AND ((? IS NOT NULL AND fatherId = ?) OR (? IS NOT NULL AND motherId = ?)) ORDER BY birthDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    _argIndex = 2;
    if (familyId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, familyId);
    }
    _argIndex = 3;
    if (fatherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fatherId);
    }
    _argIndex = 4;
    if (fatherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fatherId);
    }
    _argIndex = 5;
    if (motherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, motherId);
    }
    _argIndex = 6;
    if (motherId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, motherId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PersonEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PersonEntity> getPendingSyncPeople(final int pendingState) {
    final String _sql = "SELECT * FROM people WHERE syncState = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, pendingState);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfNickName = CursorUtil.getColumnIndexOrThrow(_cursor, "nickName");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
      final int _cursorIndexOfDeathDate = CursorUtil.getColumnIndexOrThrow(_cursor, "deathDate");
      final int _cursorIndexOfIsDeceased = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeceased");
      final int _cursorIndexOfHometown = CursorUtil.getColumnIndexOrThrow(_cursor, "hometown");
      final int _cursorIndexOfOccupation = CursorUtil.getColumnIndexOrThrow(_cursor, "occupation");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final int _cursorIndexOfBiography = CursorUtil.getColumnIndexOrThrow(_cursor, "biography");
      final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
      final int _cursorIndexOfFatherId = CursorUtil.getColumnIndexOrThrow(_cursor, "fatherId");
      final int _cursorIndexOfMotherId = CursorUtil.getColumnIndexOrThrow(_cursor, "motherId");
      final int _cursorIndexOfSpouseId = CursorUtil.getColumnIndexOrThrow(_cursor, "spouseId");
      final int _cursorIndexOfMarriageDate = CursorUtil.getColumnIndexOrThrow(_cursor, "marriageDate");
      final int _cursorIndexOfCreatedByUid = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUid");
      final int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
      final List<PersonEntity> _result = new ArrayList<PersonEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PersonEntity _item;
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        final String _tmpFamilyId;
        if (_cursor.isNull(_cursorIndexOfFamilyId)) {
          _tmpFamilyId = null;
        } else {
          _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
        }
        final String _tmpFullName;
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _tmpFullName = null;
        } else {
          _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
        }
        final String _tmpNickName;
        if (_cursor.isNull(_cursorIndexOfNickName)) {
          _tmpNickName = null;
        } else {
          _tmpNickName = _cursor.getString(_cursorIndexOfNickName);
        }
        final String _tmpGender;
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _tmpGender = null;
        } else {
          _tmpGender = _cursor.getString(_cursorIndexOfGender);
        }
        final String _tmpBirthDate;
        if (_cursor.isNull(_cursorIndexOfBirthDate)) {
          _tmpBirthDate = null;
        } else {
          _tmpBirthDate = _cursor.getString(_cursorIndexOfBirthDate);
        }
        final String _tmpDeathDate;
        if (_cursor.isNull(_cursorIndexOfDeathDate)) {
          _tmpDeathDate = null;
        } else {
          _tmpDeathDate = _cursor.getString(_cursorIndexOfDeathDate);
        }
        final boolean _tmpIsDeceased;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDeceased);
        _tmpIsDeceased = _tmp != 0;
        final String _tmpHometown;
        if (_cursor.isNull(_cursorIndexOfHometown)) {
          _tmpHometown = null;
        } else {
          _tmpHometown = _cursor.getString(_cursorIndexOfHometown);
        }
        final String _tmpOccupation;
        if (_cursor.isNull(_cursorIndexOfOccupation)) {
          _tmpOccupation = null;
        } else {
          _tmpOccupation = _cursor.getString(_cursorIndexOfOccupation);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        final String _tmpBiography;
        if (_cursor.isNull(_cursorIndexOfBiography)) {
          _tmpBiography = null;
        } else {
          _tmpBiography = _cursor.getString(_cursorIndexOfBiography);
        }
        final String _tmpPhotoPath;
        if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
          _tmpPhotoPath = null;
        } else {
          _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
        }
        final String _tmpFatherId;
        if (_cursor.isNull(_cursorIndexOfFatherId)) {
          _tmpFatherId = null;
        } else {
          _tmpFatherId = _cursor.getString(_cursorIndexOfFatherId);
        }
        final String _tmpMotherId;
        if (_cursor.isNull(_cursorIndexOfMotherId)) {
          _tmpMotherId = null;
        } else {
          _tmpMotherId = _cursor.getString(_cursorIndexOfMotherId);
        }
        final String _tmpSpouseId;
        if (_cursor.isNull(_cursorIndexOfSpouseId)) {
          _tmpSpouseId = null;
        } else {
          _tmpSpouseId = _cursor.getString(_cursorIndexOfSpouseId);
        }
        final String _tmpMarriageDate;
        if (_cursor.isNull(_cursorIndexOfMarriageDate)) {
          _tmpMarriageDate = null;
        } else {
          _tmpMarriageDate = _cursor.getString(_cursorIndexOfMarriageDate);
        }
        final String _tmpCreatedByUid;
        if (_cursor.isNull(_cursorIndexOfCreatedByUid)) {
          _tmpCreatedByUid = null;
        } else {
          _tmpCreatedByUid = _cursor.getString(_cursorIndexOfCreatedByUid);
        }
        final int _tmpGeneration;
        _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
        final long _tmpUpdatedAt;
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
        final int _tmpSyncState;
        _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
        _item = new PersonEntity(_tmpId,_tmpFamilyId,_tmpFullName,_tmpNickName,_tmpGender,_tmpBirthDate,_tmpDeathDate,_tmpIsDeceased,_tmpHometown,_tmpOccupation,_tmpPhoneNumber,_tmpBiography,_tmpPhotoPath,_tmpFatherId,_tmpMotherId,_tmpSpouseId,_tmpMarriageDate,_tmpCreatedByUid,_tmpGeneration,_tmpUpdatedAt,_tmpSyncState);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Cursor getPeopleCursor(final String familyId) {
    final String _sql = "SELECT * FROM people WHERE familyId = ? ORDER BY fullName ASC";
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
  public Cursor getPersonCursor(final String personId) {
    final String _sql = "SELECT * FROM people WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (personId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, personId);
    }
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @Override
  public Cursor getAllPeopleCursor() {
    final String _sql = "SELECT * FROM people ORDER BY fullName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _tmpResult = __db.query(_statement);
    return _tmpResult;
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
