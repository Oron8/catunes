package com.pepe.musicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.pepe.musicplayer.ui.PlayerViewModel
import java.io.File
import kotlin.math.roundToInt

data class LyricLine(val timeMs: Long, val text: String)

fun parseLrc(lrcContent: String?): List<LyricLine> {
    if (lrcContent.isNullOrBlank()) return emptyList()
    val lines = mutableListOf<LyricLine>()
    val timeRegex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2})]") // [mm:ss.xx]
    lrcContent.lines().forEach { line ->
        val match = timeRegex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLongOrNull() ?: 0L
            val sec = match.groupValues[2].toLongOrNull() ?: 0L
            val cent = match.groupValues[3].toLongOrNull() ?: 0L
            val timeMs = (min * 60 + sec) * 1000 + cent * 10
            val text = line.replace(timeRegex, "").trim()
            lines.add(LyricLine(timeMs, text))
        }
    }
    return lines.sortedBy { it.timeMs }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val state by playerViewModel.playbackState.collectAsState()
    var showLyrics by remember { mutableStateOf(false) }

    // Animación de escala para la carátula cuando se reproduce/pausa (optimizada con tween)
    val albumArtScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1f else 0.92f,
        animationSpec = tween(200),
        label = "albumArtScale"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Área central: carátula animada o letras de canciones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "playerContent"
            ) { targetShowLyrics ->
                if (targetShowLyrics) {
                    val lines = remember(state.currentSong) {
                        parseLrc(state.currentSong?.lrcContent)
                    }
                    if (lines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showLyrics = false }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay letras disponibles.\nToca aquí para volver a la carátula.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val activeLineIndex = lines.indexOfLast { it.timeMs <= state.positionMs }
                        val listState = rememberLazyListState()

                        LaunchedEffect(activeLineIndex) {
                            if (activeLineIndex >= 0) {
                                listState.animateScrollToItem(activeLineIndex)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(320.dp)
                                .clickable { showLyrics = false },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(lines) { idx, line ->
                                val isActive = idx == activeLineIndex
                                val color by animateColorAsState(
                                    targetValue = if (isActive) MaterialTheme.colorScheme.primary 
                                                  else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    label = "lyricColor"
                                )
                                val sizeMultiplier = if (isActive) 1.25f else 1.0f
                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize * sizeMultiplier,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = color,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Carátula grande o placeholder
                    val artPath = state.currentSong?.albumArtPath
                    Box(
                        modifier = Modifier
                            .scale(albumArtScale)
                            .clickable { showLyrics = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (artPath != null && File(artPath).exists()) {
                            AsyncImage(
                                model = File(artPath),
                                contentDescription = "Carátula (Toca para ver letras)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        contentDescription = "Toca para ver letras",
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            state.currentSong?.title ?: "Nada sonando",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.basicMarquee()
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            state.currentSong?.let { song ->
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .basicMarquee()
                        .clickable { onArtistClick(song.artist) }
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = song.album,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .basicMarquee()
                        .clickable { onAlbumClick(song.album) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        val progress = if (state.durationMs > 0) {
            (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Slider(
            value = progress,
            onValueChange = { value ->
                playerViewModel.seekTo((value * state.durationMs).roundToInt().toLong())
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(state.positionMs), style = MaterialTheme.typography.bodySmall)
            Text(formatTime(state.durationMs), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { playerViewModel.skipPrevious() }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(36.dp))
            }
            FilledIconButton(
                onClick = { playerViewModel.togglePlayPause() },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pausa",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { playerViewModel.skipNext() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(36.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Shuffle y Repeat
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeColor = MaterialTheme.colorScheme.primary
            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

            IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(24.dp),
                    tint = if (state.shuffleEnabled) activeColor else inactiveColor
                )
            }
            IconButton(onClick = { playerViewModel.cycleRepeatMode() }) {
                Icon(
                    if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repetir",
                    modifier = Modifier.size(24.dp),
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) activeColor else inactiveColor
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
