package com.pepe.musicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/** Tabla puente: qué canciones pertenecen a qué playlist y en qué posición. */
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songUri"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songUri: String,
    val position: Int
)
