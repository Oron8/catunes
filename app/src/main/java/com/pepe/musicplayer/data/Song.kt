package com.pepe.musicplayer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val uriString: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val folderUri: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val trackNumber: Int = 0,
    val year: Int = 0,
    // Ruta local en cacheDir donde se guardó la carátula extraída del archivo.
    // Null si el archivo no tiene imagen incrustada o todavía no se escaneó.
    val albumArtPath: String? = null,
    // Contenido del archivo .lrc si existe al lado del archivo de audio.
    val lrcContent: String? = null
)
