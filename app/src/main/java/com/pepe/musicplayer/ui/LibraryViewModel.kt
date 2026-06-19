package com.pepe.musicplayer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pepe.musicplayer.data.FolderRepository
import com.pepe.musicplayer.data.MusicDatabase
import com.pepe.musicplayer.data.Song
import com.pepe.musicplayer.scanner.MusicScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    private val folderRepository = FolderRepository(application)
    private val scanner = MusicScanner(application)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _folders = MutableStateFlow<Set<String>>(emptySet())
    val folders: StateFlow<Set<String>> = _folders.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow("Todo")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    init {
        viewModelScope.launch {
            db.songDao().getAllSongs().collect { _songs.value = it }
        }
        viewModelScope.launch {
            folderRepository.folderUris.collect { _folders.value = it }
        }
    }

    fun setSearch(query: String, filter: String = "Todo") {
        _searchQuery.value = query
        _searchFilter.value = filter
    }

    fun addFolderAndScan(uriString: String) {
        viewModelScope.launch {
            _isScanning.value = true
            folderRepository.addFolder(uriString)
            rescanFolder(uriString)
            _isScanning.value = false
        }
    }

    fun removeFolder(uriString: String) {
        viewModelScope.launch {
            folderRepository.removeFolder(uriString)
            db.songDao().deleteSongsInFolder(uriString)
        }
    }

    fun rescanAll() {
        viewModelScope.launch {
            _isScanning.value = true
            val currentFolders = folderRepository.folderUris.first()
            for (folder in currentFolders) {
                rescanFolder(folder)
            }
            _isScanning.value = false
        }
    }

    private suspend fun rescanFolder(uriString: String) {
        val found = scanner.scanFolder(uriString)
        db.songDao().deleteSongsInFolder(uriString)
        db.songDao().insertAll(found)
    }
}
