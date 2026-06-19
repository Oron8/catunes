package com.pepe.musicplayer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pepe.musicplayer.data.Song
import com.pepe.musicplayer.playback.MusicController
import com.pepe.musicplayer.playback.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val controller = MusicController(application)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        controller.connect {
            viewModelScope.launch {
                controller.state.collect { newState ->
                    val songChanged = newState.currentSong?.uriString != _playbackState.value.currentSong?.uriString
                    _playbackState.value = if (songChanged) {
                        newState.copy(
                            positionMs = 0L,
                            durationMs = controller.duration()
                        )
                    } else {
                        newState.copy(
                            positionMs = _playbackState.value.positionMs,
                            durationMs = _playbackState.value.durationMs
                        )
                    }
                }
            }
            viewModelScope.launch {
                while (true) {
                    delay(500)
                    _playbackState.value = _playbackState.value.copy(
                        positionMs = controller.currentPosition(),
                        durationMs = controller.duration()
                    )
                }
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int) = controller.playQueue(songs, startIndex)
    fun togglePlayPause() = controller.togglePlayPause()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun skipNext() = controller.skipNext()
    fun skipPrevious() = controller.skipPrevious()
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeatMode() = controller.cycleRepeatMode()

    override fun onCleared() {
        controller.release()
        super.onCleared()
    }
}
