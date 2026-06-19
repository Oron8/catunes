package com.pepe.musicplayer.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlaylistDao_Impl implements PlaylistDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Playlist> __insertionAdapterOfPlaylist;

  private final EntityInsertionAdapter<PlaylistSongCrossRef> __insertionAdapterOfPlaylistSongCrossRef;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlaylist;

  private final SharedSQLiteStatement __preparedStmtOfRemoveSongFromPlaylist;

  public PlaylistDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlaylist = new EntityInsertionAdapter<Playlist>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `playlists` (`id`,`name`) VALUES (nullif(?, 0),?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Playlist entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
      }
    };
    this.__insertionAdapterOfPlaylistSongCrossRef = new EntityInsertionAdapter<PlaylistSongCrossRef>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `playlist_song_cross_ref` (`playlistId`,`songUri`,`position`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistSongCrossRef entity) {
        statement.bindLong(1, entity.getPlaylistId());
        statement.bindString(2, entity.getSongUri());
        statement.bindLong(3, entity.getPosition());
      }
    };
    this.__preparedStmtOfDeletePlaylist = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlists WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveSongFromPlaylist = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlist_song_cross_ref WHERE playlistId = ? AND songUri = ?";
        return _query;
      }
    };
  }

  @Override
  public Object createPlaylist(final Playlist playlist,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlaylist.insertAndReturnId(playlist);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addSongToPlaylist(final PlaylistSongCrossRef ref,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistSongCrossRef.insert(ref);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlaylist(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlaylist.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePlaylist.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeSongFromPlaylist(final long playlistId, final String songUri,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveSongFromPlaylist.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, playlistId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, songUri);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRemoveSongFromPlaylist.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Playlist>> getAllPlaylists() {
    final String _sql = "SELECT * FROM playlists ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playlists"}, new Callable<List<Playlist>>() {
      @Override
      @NonNull
      public List<Playlist> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final List<Playlist> _result = new ArrayList<Playlist>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Playlist _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            _item = new Playlist(_tmpId,_tmpName);
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
  public Flow<List<Song>> getSongsInPlaylist(final long playlistId) {
    final String _sql = "\n"
            + "        SELECT s.* FROM songs s\n"
            + "        INNER JOIN playlist_song_cross_ref ref ON s.uriString = ref.songUri\n"
            + "        WHERE ref.playlistId = ?\n"
            + "        ORDER BY ref.position\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playlistId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"songs",
        "playlist_song_cross_ref"}, new Callable<List<Song>>() {
      @Override
      @NonNull
      public List<Song> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
          try {
            final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
            final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
            final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
            final int _cursorIndexOfFolderUri = CursorUtil.getColumnIndexOrThrow(_cursor, "folderUri");
            final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
            final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
            final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
            final int _cursorIndexOfAlbumArtPath = CursorUtil.getColumnIndexOrThrow(_cursor, "albumArtPath");
            final int _cursorIndexOfLrcContent = CursorUtil.getColumnIndexOrThrow(_cursor, "lrcContent");
            final List<Song> _result = new ArrayList<Song>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final Song _item;
              final String _tmpUriString;
              _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
              final String _tmpTitle;
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              final String _tmpArtist;
              _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
              final String _tmpAlbum;
              _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
              final long _tmpDurationMs;
              _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
              final String _tmpFolderUri;
              _tmpFolderUri = _cursor.getString(_cursorIndexOfFolderUri);
              final long _tmpDateAdded;
              _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
              final int _tmpTrackNumber;
              _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
              final int _tmpYear;
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
              final String _tmpAlbumArtPath;
              if (_cursor.isNull(_cursorIndexOfAlbumArtPath)) {
                _tmpAlbumArtPath = null;
              } else {
                _tmpAlbumArtPath = _cursor.getString(_cursorIndexOfAlbumArtPath);
              }
              final String _tmpLrcContent;
              if (_cursor.isNull(_cursorIndexOfLrcContent)) {
                _tmpLrcContent = null;
              } else {
                _tmpLrcContent = _cursor.getString(_cursorIndexOfLrcContent);
              }
              _item = new Song(_tmpUriString,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpDurationMs,_tmpFolderUri,_tmpDateAdded,_tmpTrackNumber,_tmpYear,_tmpAlbumArtPath,_tmpLrcContent);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object nextPosition(final long playlistId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(MAX(position) + 1, 0) FROM playlist_song_cross_ref WHERE playlistId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playlistId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
