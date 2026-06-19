package com.pepe.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import com.pepe.musicplayer.data.Song
import com.pepe.musicplayer.ui.PlaylistViewModel

/**
 * Detalle de una playlist: lista de canciones en orden + botón para quitarlas.
 * onSongClick arranca la reproducción desde esa canción.
 */
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistViewModel: PlaylistViewModel,
    libraryViewModel: com.pepe.musicplayer.ui.LibraryViewModel,
    onSongClick: (List<Song>, Int) -> Unit
) {
    val songs by playlistViewModel.songsInPlaylist(playlistId)
        .collectAsState(initial = emptyList())
    val allSongs by libraryViewModel.songs.collectAsState()

    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var showAddSongDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var addSongSearchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val filteredSongs = androidx.compose.runtime.remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
        // Toolbar with search & Add button
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en playlist...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Button(onClick = { showAddSongDialog = true }) {
                Text("Agregar")
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(filteredSongs, key = { _, song -> song.uriString }) { index, song ->
                ListItem(
                    headlineContent = {
                        Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text("${song.artist} • ${song.album}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingContent = {
                        Icon(Icons.Filled.MusicNote, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            playlistViewModel.removeSong(playlistId, song.uriString)
                        }) {
                            Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Quitar de playlist")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClick(filteredSongs, index) }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddSongDialog) {
        val addableSongs = androidx.compose.runtime.remember(allSongs, songs, addSongSearchQuery) {
            val existingUris = songs.map { it.uriString }.toSet()
            allSongs.filter { it.uriString !in existingUris }.filter {
                addSongSearchQuery.isBlank() ||
                it.title.contains(addSongSearchQuery, ignoreCase = true) ||
                it.artist.contains(addSongSearchQuery, ignoreCase = true) ||
                it.album.contains(addSongSearchQuery, ignoreCase = true)
            }
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddSongDialog = false; addSongSearchQuery = "" },
            title = { Text("Agregar canciones") },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = addSongSearchQuery,
                        onValueChange = { addSongSearchQuery = it },
                        placeholder = { Text("Buscar canción...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    if (addableSongs.isEmpty()) {
                        Text("No hay canciones disponibles para agregar.", modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            itemsIndexed(addableSongs) { _, song ->
                                ListItem(
                                    headlineContent = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    supportingContent = { Text("${song.artist} • ${song.album}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playlistViewModel.addSong(playlistId, song)
                                        }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddSongDialog = false; addSongSearchQuery = "" }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
