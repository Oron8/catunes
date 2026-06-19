package com.pepe.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.pepe.musicplayer.data.Song
import kotlinx.coroutines.flow.MutableStateFlow

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

/**
 * Capa fina que conecta la UI (Compose) con el PlaybackService a través de un
 * MediaController, sin que la UI tenga que conocer ExoPlayer directamente.
 */
class MusicController(private val context: Context) {

    private var controller: MediaController? = null
    val state = MutableStateFlow(PlaybackState())
    private var currentQueue: List<Song> = emptyList()

    fun connect(onReady: () -> Unit = {}) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            
            // Inicializar el estado actual con la sesión existente si la hay
            controller?.let { player ->
                val currentMediaItem = player.currentMediaItem
                val song = currentQueue.find { it.uriString == currentMediaItem?.mediaId }
                    ?: currentMediaItem?.let { item ->
                        val meta = item.mediaMetadata
                        Song(
                            uriString = item.mediaId,
                            title = meta.title?.toString() ?: "Desconocido",
                            artist = meta.artist?.toString() ?: "Desconocido",
                            album = meta.albumTitle?.toString() ?: "Desconocido",
                            durationMs = player.duration.coerceAtLeast(0),
                            folderUri = ""
                        )
                    }
                state.value = PlaybackState(
                    isPlaying = player.isPlaying,
                    currentSong = song,
                    shuffleEnabled = player.shuffleModeEnabled,
                    repeatMode = player.repeatMode
                )
            }

            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    state.value = state.value.copy(isPlaying = isPlaying)
                }
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    state.value = state.value.copy(shuffleEnabled = shuffleModeEnabled)
                }
                override fun onRepeatModeChanged(repeatMode: Int) {
                    state.value = state.value.copy(repeatMode = repeatMode)
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val song = currentQueue.find { it.uriString == mediaItem?.mediaId }
                        ?: mediaItem?.let { item ->
                            val meta = item.mediaMetadata
                            Song(
                                uriString = item.mediaId,
                                title = meta.title?.toString() ?: "Desconocido",
                                artist = meta.artist?.toString() ?: "Desconocido",
                                album = meta.albumTitle?.toString() ?: "Desconocido",
                                durationMs = controller?.duration?.coerceAtLeast(0) ?: 0L,
                                folderUri = ""
                            )
                        }
                    state.value = state.value.copy(currentSong = song)
                }
            })
            onReady()
        }, MoreExecutors.directExecutor())
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        currentQueue = songs
        val items = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.uriString)
                .setMediaId(song.uriString)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
        }
        controller?.setMediaItems(items, startIndex, 0L)
        controller?.prepare()
        controller?.play()
        state.value = state.value.copy(currentSong = songs.getOrNull(startIndex))
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun skipNext() = controller?.seekToNextMediaItem()
    fun skipPrevious() = controller?.seekToPreviousMediaItem()

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeatMode() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun currentPosition(): Long = controller?.currentPosition ?: 0L
    fun duration(): Long = controller?.duration?.coerceAtLeast(0) ?: 0L

    fun release() {
        controller?.release()
        controller = null
    }
}
