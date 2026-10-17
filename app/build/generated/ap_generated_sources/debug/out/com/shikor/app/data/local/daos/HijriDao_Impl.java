package com.shikor.app.data.local.daos;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shikor.app.data.local.entities.HijriCacheEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HijriDao_Impl implements HijriDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HijriCacheEntity> __insertionAdapterOfHijriCacheEntity;

  public HijriDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHijriCacheEntity = new EntityInsertionAdapter<HijriCacheEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `hijri_cache` (`gregorianDate`,`hijriDay`,`hijriMonthEn`,`hijriMonthAr`,`hijriYear`,`cachedAt`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HijriCacheEntity entity) {
        if (entity.getGregorianDate() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getGregorianDate());
        }
        if (entity.getHijriDay() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getHijriDay());
        }
        if (entity.getHijriMonthEn() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getHijriMonthEn());
        }
        if (entity.getHijriMonthAr() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getHijriMonthAr());
        }
        if (entity.getHijriYear() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getHijriYear());
        }
        statement.bindLong(6, entity.getCachedAt());
      }
    };
  }

  @Override
  public void insert(final HijriCacheEntity cache) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfHijriCacheEntity.insert(cache);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public HijriCacheEntity getCachedDate(final String gregorianDate) {
    final String _sql = "SELECT * FROM hijri_cache WHERE gregorianDate = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (gregorianDate == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, gregorianDate);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfGregorianDate = CursorUtil.getColumnIndexOrThrow(_cursor, "gregorianDate");
      final int _cursorIndexOfHijriDay = CursorUtil.getColumnIndexOrThrow(_cursor, "hijriDay");
      final int _cursorIndexOfHijriMonthEn = CursorUtil.getColumnIndexOrThrow(_cursor, "hijriMonthEn");
      final int _cursorIndexOfHijriMonthAr = CursorUtil.getColumnIndexOrThrow(_cursor, "hijriMonthAr");
      final int _cursorIndexOfHijriYear = CursorUtil.getColumnIndexOrThrow(_cursor, "hijriYear");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
      final HijriCacheEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpGregorianDate;
        if (_cursor.isNull(_cursorIndexOfGregorianDate)) {
          _tmpGregorianDate = null;
        } else {
          _tmpGregorianDate = _cursor.getString(_cursorIndexOfGregorianDate);
        }
        final String _tmpHijriDay;
        if (_cursor.isNull(_cursorIndexOfHijriDay)) {
          _tmpHijriDay = null;
        } else {
          _tmpHijriDay = _cursor.getString(_cursorIndexOfHijriDay);
        }
        final String _tmpHijriMonthEn;
        if (_cursor.isNull(_cursorIndexOfHijriMonthEn)) {
          _tmpHijriMonthEn = null;
        } else {
          _tmpHijriMonthEn = _cursor.getString(_cursorIndexOfHijriMonthEn);
        }
        final String _tmpHijriMonthAr;
        if (_cursor.isNull(_cursorIndexOfHijriMonthAr)) {
          _tmpHijriMonthAr = null;
        } else {
          _tmpHijriMonthAr = _cursor.getString(_cursorIndexOfHijriMonthAr);
        }
        final String _tmpHijriYear;
        if (_cursor.isNull(_cursorIndexOfHijriYear)) {
          _tmpHijriYear = null;
        } else {
          _tmpHijriYear = _cursor.getString(_cursorIndexOfHijriYear);
        }
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        _result = new HijriCacheEntity(_tmpGregorianDate,_tmpHijriDay,_tmpHijriMonthEn,_tmpHijriMonthAr,_tmpHijriYear,_tmpCachedAt);
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
