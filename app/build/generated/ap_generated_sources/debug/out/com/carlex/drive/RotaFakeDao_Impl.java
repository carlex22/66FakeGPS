package com.carlex.drive;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RotaFakeDao_Impl implements RotaFakeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RotaFake> __insertionAdapterOfRotaFake;

  private final EntityDeletionOrUpdateAdapter<RotaFake> __deletionAdapterOfRotaFake;

  private final EntityDeletionOrUpdateAdapter<RotaFake> __updateAdapterOfRotaFake;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllExceptFirstFour;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRotaFakeWithTimeGreaterThan;

  public RotaFakeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRotaFake = new EntityInsertionAdapter<RotaFake>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `rota_fake` (`id`,`latitude`,`longitude`,`bearing`,`velocidade`,`tempo`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final RotaFake entity) {
        statement.bindLong(1, entity.id);
        statement.bindDouble(2, entity.latitude);
        statement.bindDouble(3, entity.longitude);
        statement.bindDouble(4, entity.bearing);
        statement.bindDouble(5, entity.velocidade);
        statement.bindLong(6, entity.tempo);
      }
    };
    this.__deletionAdapterOfRotaFake = new EntityDeletionOrUpdateAdapter<RotaFake>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `rota_fake` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final RotaFake entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfRotaFake = new EntityDeletionOrUpdateAdapter<RotaFake>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `rota_fake` SET `id` = ?,`latitude` = ?,`longitude` = ?,`bearing` = ?,`velocidade` = ?,`tempo` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final RotaFake entity) {
        statement.bindLong(1, entity.id);
        statement.bindDouble(2, entity.latitude);
        statement.bindDouble(3, entity.longitude);
        statement.bindDouble(4, entity.bearing);
        statement.bindDouble(5, entity.velocidade);
        statement.bindLong(6, entity.tempo);
        statement.bindLong(7, entity.id);
      }
    };
    this.__preparedStmtOfDeleteAllExceptFirstFour = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rota_fake WHERE id NOT IN (SELECT id FROM rota_fake ORDER BY id LIMIT 2)";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rota_fake";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRotaFakeWithTimeGreaterThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rota_fake WHERE tempo < ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final RotaFake rotaFake) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfRotaFake.insert(rotaFake);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final RotaFake rotaFake) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfRotaFake.handle(rotaFake);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final RotaFake rotaFake) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfRotaFake.handle(rotaFake);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAllExceptFirstFour() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllExceptFirstFour.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllExceptFirstFour.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public void deleteRotaFakeWithTimeGreaterThan(final long currentTimeMillis) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRotaFakeWithTimeGreaterThan.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, currentTimeMillis);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteRotaFakeWithTimeGreaterThan.release(_stmt);
    }
  }

  @Override
  public List<RotaFake> getAllRotaFake() {
    final String _sql = "SELECT * FROM rota_fake";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfBearing = CursorUtil.getColumnIndexOrThrow(_cursor, "bearing");
      final int _cursorIndexOfVelocidade = CursorUtil.getColumnIndexOrThrow(_cursor, "velocidade");
      final int _cursorIndexOfTempo = CursorUtil.getColumnIndexOrThrow(_cursor, "tempo");
      final List<RotaFake> _result = new ArrayList<RotaFake>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final RotaFake _item;
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final float _tmpBearing;
        _tmpBearing = _cursor.getFloat(_cursorIndexOfBearing);
        final double _tmpVelocidade;
        _tmpVelocidade = _cursor.getDouble(_cursorIndexOfVelocidade);
        final long _tmpTempo;
        _tmpTempo = _cursor.getLong(_cursorIndexOfTempo);
        _item = new RotaFake(_tmpLatitude,_tmpLongitude,_tmpBearing,_tmpVelocidade,_tmpTempo);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public RotaFake getRotaFakeWithMinTime(final long currentTimeMillis) {
    final String _sql = "SELECT * FROM rota_fake WHERE tempo >= ? ORDER BY tempo ASC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTimeMillis);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfBearing = CursorUtil.getColumnIndexOrThrow(_cursor, "bearing");
      final int _cursorIndexOfVelocidade = CursorUtil.getColumnIndexOrThrow(_cursor, "velocidade");
      final int _cursorIndexOfTempo = CursorUtil.getColumnIndexOrThrow(_cursor, "tempo");
      final RotaFake _result;
      if (_cursor.moveToFirst()) {
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final float _tmpBearing;
        _tmpBearing = _cursor.getFloat(_cursorIndexOfBearing);
        final double _tmpVelocidade;
        _tmpVelocidade = _cursor.getDouble(_cursorIndexOfVelocidade);
        final long _tmpTempo;
        _tmpTempo = _cursor.getLong(_cursorIndexOfTempo);
        _result = new RotaFake(_tmpLatitude,_tmpLongitude,_tmpBearing,_tmpVelocidade,_tmpTempo);
        _result.id = _cursor.getInt(_cursorIndexOfId);
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
  public RotaFake getLastRotaFakeByTime() {
    final String _sql = "SELECT * FROM rota_fake ORDER BY tempo DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfBearing = CursorUtil.getColumnIndexOrThrow(_cursor, "bearing");
      final int _cursorIndexOfVelocidade = CursorUtil.getColumnIndexOrThrow(_cursor, "velocidade");
      final int _cursorIndexOfTempo = CursorUtil.getColumnIndexOrThrow(_cursor, "tempo");
      final RotaFake _result;
      if (_cursor.moveToFirst()) {
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final float _tmpBearing;
        _tmpBearing = _cursor.getFloat(_cursorIndexOfBearing);
        final double _tmpVelocidade;
        _tmpVelocidade = _cursor.getDouble(_cursorIndexOfVelocidade);
        final long _tmpTempo;
        _tmpTempo = _cursor.getLong(_cursorIndexOfTempo);
        _result = new RotaFake(_tmpLatitude,_tmpLongitude,_tmpBearing,_tmpVelocidade,_tmpTempo);
        _result.id = _cursor.getInt(_cursorIndexOfId);
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
