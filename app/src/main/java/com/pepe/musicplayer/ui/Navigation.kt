package com.pepe.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.pepe.musicplayer.data.ThemeRepository
import com.pepe.musicplayer.playback.PlaybackState
import com.pepe.musicplayer.ui.screens.EqualizerScreen
import com.pepe.musicplayer.ui.screens.FoldersScreen
import com.pepe.musicplayer.ui.screens.LibraryScreen
import com.pepe.musicplayer.ui.screens.PlaylistDetailScreen
import com.pepe.musicplayer.ui.screens.PlaylistsScreen
import com.pepe.musicplayer.ui.screens.PlayerScreen
import com.pepe.musicplayer.ui.screens.SettingsScreen
import java.io.File

sealed class Screen(val route: String, val label: String) {
    object Library   : Screen("library",   "Biblioteca")
    object Folders   : Screen("folders",   "Carpetas")
    object Playlists : Screen("playlists", "Playlists")
    object Settings  : Screen("settings",  "Ajustes")
    object Player    : Screen("player",    "Reproduciendo")
    object Equalizer : Screen("equalizer", "Ecualizador")
    object PlaylistDetail : Screen("playlist_detail", "Playlist")
}

/** Pantallas que aparecen en la barra de navegación inferior. */
private val bottomNavItems = listOf(
    Screen.Library   to Icons.Filled.LibraryMusic,
    Screen.Folders   to Icons.Filled.Folder,
    Screen.Playlists to Icons.Filled.PlaylistPlay,
    Screen.Settings  to Icons.Filled.Settings,
    Screen.Equalizer to Icons.Filled.Equalizer,
)

@Composable
fun MusicPlayerNavHost(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    themeRepository: ThemeRepository
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding() // Agrega espacio para la barra del sistema
            ) {
                val playbackState by playerViewModel.playbackState.collectAsState()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // Mostrar el MiniPlayer si hay música cargada y no estamos en la pantalla del reproductor
                AnimatedVisibility(
                    visible = playbackState.currentSong != null && currentRoute != Screen.Player.route,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextClick = { playerViewModel.skipNext() },
                        onShuffleClick = { playerViewModel.toggleShuffle() },
                        onRepeatClick = { playerViewModel.cycleRepeatMode() },
                        onClick = { navController.navigate(Screen.Player.route) }
                    )
                }

                NavigationBar(
                    tonalElevation = 8.dp
                ) {
                    val currentRouteHierarchy = backStackEntry?.destination
                    bottomNavItems.forEach { (screen, icon) ->
                        NavigationBarItem(
                            selected = currentRouteHierarchy?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = { 
                                // Al navegar a Biblioteca desde el menú, limpiamos búsqueda si ya estamos ahí
                                if (screen == Screen.Library && currentRoute == Screen.Library.route) {
                                    libraryViewModel.setSearch("", "Todo")
                                }
                                navController.navigate(screen.route) { 
                                    launchSingleTop = true 
                                    restoreState = true
                                } 
                            },
                            icon = { Icon(icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    playlistViewModel = playlistViewModel,
                    onSongClick = { songs, index ->
                        playerViewModel.playQueue(songs, index)
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
            composable(Screen.Folders.route) {
                FoldersScreen(libraryViewModel = libraryViewModel)
            }
            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    playlistViewModel = playlistViewModel,
                    onOpenPlaylist = { id ->
                        playlistViewModel.openPlaylist(id)
                        navController.navigate(Screen.PlaylistDetail.route)
                    }
                )
            }
            composable(Screen.PlaylistDetail.route) {
                val playlistId by playlistViewModel.openPlaylistId.collectAsState()
                playlistId?.let { id ->
                    PlaylistDetailScreen(
                        playlistId = id,
                        playlistViewModel = playlistViewModel,
                        libraryViewModel = libraryViewModel,
                        onSongClick = { songs, index ->
                            playerViewModel.playQueue(songs, index)
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }
            }
            composable(Screen.Settings.route) {
                SettingsScreen(themeRepository = themeRepository)
            }
            composable(Screen.Player.route) {
                PlayerScreen(
                    playerViewModel = playerViewModel,
                    onArtistClick = { artist ->
                        libraryViewModel.setSearch(artist, "Artista")
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Library.route) { inclusive = true }
                        }
                    },
                    onAlbumClick = { album ->
                        libraryViewModel.setSearch(album, "Álbum")
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Library.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Equalizer.route) {
                EqualizerScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onClick: () -> Unit
) {
    val song = playbackState.currentSong ?: return
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        tonalElevation = 6.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            val artPath = song.albumArtPath
            if (artPath != null && File(artPath).exists()) {
                AsyncImage(
                    model = File(artPath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = song.artist,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.basicMarquee()
                )
            }

            // Shuffle
            IconButton(onClick = onShuffleClick) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(20.dp),
                    tint = if (playbackState.shuffleEnabled) activeColor else inactiveColor
                )
            }

            // Play/Pause
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pausa",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Next
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Siguiente",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repeat
            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = if (playbackState.repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repetir",
                    modifier = Modifier.size(20.dp),
                    tint = if (playbackState.repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) activeColor else inactiveColor
                )
            }
        }
    }
}
