package com.pepe.musicplayer.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pepe.musicplayer.ui.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(libraryViewModel: LibraryViewModel) {
    val context = LocalContext.current
    val folders by libraryViewModel.folders.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            libraryViewModel.addFolderAndScan(uri.toString())
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { pickFolderLauncher.launch(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar carpeta")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (isScanning) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            Row(Modifier.padding(16.dp)) {
                Button(onClick = { libraryViewModel.rescanAll() }) {
                    Text("Re-escanear todas")
                }
            }
            if (folders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tocá el + para agregar una carpeta con música")
                }
            } else {
                LazyColumn {
                    items(folders.toList()) { folderUri ->
                        ListItem(
                            headlineContent = { Text(decodeFolderName(folderUri)) },
                            leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { libraryViewModel.removeFolder(folderUri) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun decodeFolderName(uriString: String): String {
    return try {
        val decoded = android.net.Uri.decode(uriString)
        decoded.substringAfterLast(':').ifEmpty { decoded }
    } catch (e: Exception) {
        uriString
    }
}
