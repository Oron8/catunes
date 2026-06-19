package com.pepe.musicplayer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY artist, album, trackNumber, title")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Query("DELETE FROM songs WHERE folderUri = :folderUri")
    suspend fun deleteSongsInFolder(folderUri: String)

    @Query("DELETE FROM songs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}
