package com.pepe.musicplayer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pepe.musicplayer.data.MusicDatabase
import com.pepe.musicplayer.data.Playlist
import com.pepe.musicplayer.data.PlaylistSongCrossRef
import com.pepe.musicplayer.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = MusicDatabase.getInstance(application).playlistDao()

    val playlists: Flow<List<Playlist>> = dao.getAllPlaylists()

    // Playlist abierta actualmente en PlaylistDetailScreen
    private val _openPlaylistId = MutableStateFlow<Long?>(null)
    val openPlaylistId: StateFlow<Long?> = _openPlaylistId.asStateFlow()

    fun openPlaylist(id: Long) { _openPlaylistId.value = id }
    fun closePlaylist() { _openPlaylistId.value = null }

    fun songsInPlaylist(playlistId: Long): Flow<List<Song>> =
        dao.getSongsInPlaylist(playlistId)

    fun createPlaylist(name: String) {
        viewModelScope.launch { dao.createPlaylist(Playlist(name = name)) }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { dao.deletePlaylist(id) }
    }

    fun addSong(playlistId: Long, song: Song) {
        viewModelScope.launch {
            val pos = dao.nextPosition(playlistId)
            dao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, song.uriString, pos))
        }
    }

    fun removeSong(playlistId: Long, songUri: String) {
        viewModelScope.launch { dao.removeSongFromPlaylist(playlistId, songUri) }
    }
}
