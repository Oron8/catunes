package com.pepe.musicplayer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pepe.musicplayer.R
import com.pepe.musicplayer.data.Song
import com.pepe.musicplayer.ui.LibraryViewModel
import com.pepe.musicplayer.ui.PlaylistViewModel
import java.io.File

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playlistViewModel: PlaylistViewModel,
    onSongClick: (List<Song>, Int) -> Unit
) {
    val songs by libraryViewModel.songs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState(initial = emptyList())

    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val searchFilter by libraryViewModel.searchFilter.collectAsState()

    var selectedSongForPlaylist by remember { mutableStateOf<Song?>(null) }

    // Filtrado de canciones avanzado
    val filteredSongs = remember(songs, searchQuery, searchFilter) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                when (searchFilter) {
                    "Canción" -> song.title.contains(searchQuery, ignoreCase = true)
                    "Artista" -> song.artist.contains(searchQuery, ignoreCase = true)
                    "Álbum" -> song.album.contains(searchQuery, ignoreCase = true)
                    else -> {
                        song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true) ||
                        song.album.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Encabezado con Logo y Título
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.catunes),
                contentDescription = "Logo CATUNES",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "CATUNES",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Barra de búsqueda premium
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { libraryViewModel.setSearch(it, searchFilter) },
            placeholder = { Text("Buscar en tu música...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { libraryViewModel.setSearch("", "Todo") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Fila de Filtros Rápidos (Chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todo", "Canción", "Artista", "Álbum").forEach { filter ->
                val selected = searchFilter == filter
                val chipBgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val chipTextColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                
                Surface(
                    color = chipBgColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { libraryViewModel.setSearch(searchQuery, filter) }
                ) {
                    Text(
                        text = filter,
                        color = chipTextColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (isScanning) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (songs.isEmpty() && !isScanning) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.catunes),
                    contentDescription = "Logo CATUNES",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "CATUNES",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Todavía no hay canciones.\nVe a 'Carpetas' y añade una para empezar a reproducir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredSongs.isEmpty() && searchQuery.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron canciones que coincidan con tu búsqueda.")
            }
        } else {
            LazyColumn {
                itemsIndexed(filteredSongs, key = { _, song -> song.uriString }) { index, song ->
                    ListItem(
                        headlineContent = {
                            Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Row {
                                Text(
                                    text = song.artist,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        libraryViewModel.setSearch(song.artist, "Artista")
                                    }
                                )
                                Text(" • ")
                                Text(
                                    text = song.album,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable {
                                        libraryViewModel.setSearch(song.album, "Álbum")
                                    }
                                )
                            }
                        },
                        leadingContent = {
                            AlbumArtThumbnail(song.albumArtPath)
                        },
                        trailingContent = {
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Acciones")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Agregar a playlist") },
                                        leadingIcon = { Icon(Icons.Filled.PlaylistAdd, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            selectedSongForPlaylist = song
                                        }
                                    )
                                }
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
    }

    // Diálogo para agregar canción a playlist
    selectedSongForPlaylist?.let { song ->
        AlertDialog(
            onDismissRequest = { selectedSongForPlaylist = null },
            title = { Text("Agregar a playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No tienes ninguna playlist creada. Ve a la pestaña 'Playlists' para crear una.")
                } else {
                    Column {
                        Text("Elige una playlist para \"${song.title}\":", modifier = Modifier.padding(bottom = 12.dp))
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(playlists) { playlist ->
                                ListItem(
                                    headlineContent = { Text(playlist.name) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playlistViewModel.addSong(playlist.id, song)
                                            selectedSongForPlaylist = null
                                        }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedSongForPlaylist = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/** Carátula pequeña para la lista: usa Coil si hay ruta, icono si no. */
@Composable
fun AlbumArtThumbnail(artPath: String?, modifier: Modifier = Modifier) {
    if (artPath != null && File(artPath).exists()) {
        AsyncImage(
            model = File(artPath),
            contentDescription = "Carátula",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(40.dp)
        )
    } else {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            modifier = modifier.size(40.dp)
        )
    }
}
