package com.pepe.musicplayer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // ── Playlists ────────────────────────────────────────────────────────────

    @Insert
    suspend fun createPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT * FROM playlists ORDER BY name")
    fun getAllPlaylists(): Flow<List<Playlist>>

    // ── Canciones en playlist ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(ref: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songUri = :songUri")
    suspend fun removeSongFromPlaylist(playlistId: Long, songUri: String)

    /**
     * Devuelve las canciones de una playlist, ordenadas por posición,
     * haciendo JOIN con la tabla songs para traer todos los datos.
     */
    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref ref ON s.uriString = ref.songUri
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.position
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    /** Siguiente posición libre para insertar al final de una playlist. */
    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: Long): Int
}
