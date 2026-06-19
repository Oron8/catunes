package com.pepe.musicplayer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Subimos a version = 3 por el campo lrcContent en Song.
// fallbackToDestructiveMigration() borra y recrea la tabla — la biblioteca se
// reconstruye con un re-escaneo, así que no se pierde nada importante.
@Database(entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class], version = 3, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
