package com.pepe.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pepe.musicplayer.data.Playlist
import com.pepe.musicplayer.ui.PlaylistViewModel

/**
 * Pantalla principal de playlists: lista de playlists + FAB para crear una nueva.
 * Al tocar una playlist se llama onOpenPlaylist para navegar al detalle.
 */
@Composable
fun PlaylistsScreen(
    playlistViewModel: PlaylistViewModel,
    onOpenPlaylist: (Long) -> Unit
) {
    val playlists by playlistViewModel.playlists.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newName = "" },
            title = { Text("Nueva playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        playlistViewModel.createPlaylist(newName.trim())
                        newName = ""
                        showDialog = false
                    }
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; newName = "" }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva playlist")
            }
        }
    ) { padding ->
        if (playlists.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay playlists todavía.\nUsá el botón + para crear una.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist.id) },
                        onDelete = { playlistViewModel.deletePlaylist(playlist.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(playlist: Playlist, onClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(playlist.name) },
        leadingContent = { Icon(Icons.Filled.PlaylistPlay, contentDescription = null) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar playlist")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
