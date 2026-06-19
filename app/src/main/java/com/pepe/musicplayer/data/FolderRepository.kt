package com.pepe.musicplayer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.folderDataStore by preferencesDataStore(name = "folder_prefs")
private val FOLDER_URIS_KEY = stringSetPreferencesKey("folder_uris")

// Repositorio que guarda las carpetas elegidas por el usuario (URIs persistentes vía SAF)
class FolderRepository(private val context: Context) {

    val folderUris: Flow<Set<String>> = context.folderDataStore.data.map { prefs ->
        prefs[FOLDER_URIS_KEY] ?: emptySet()
    }

    suspend fun addFolder(uri: String) {
        context.folderDataStore.edit { prefs ->
            val current = prefs[FOLDER_URIS_KEY] ?: emptySet()
            prefs[FOLDER_URIS_KEY] = current + uri
        }
    }

    suspend fun removeFolder(uri: String) {
        context.folderDataStore.edit { prefs ->
            val current = prefs[FOLDER_URIS_KEY] ?: emptySet()
            prefs[FOLDER_URIS_KEY] = current - uri
        }
    }
}
