package com.pepe.musicplayer.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.pepe.musicplayer.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "MusicScanner"
private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "ogg", "wav", "m4a", "aac", "opus", "wma")

/**
 * Recorre recursivamente una carpeta (elegida vía Storage Access Framework) y
 * extrae metadata de cada archivo de audio encontrado usando MediaMetadataRetriever.
 *
 * Ahora también extrae carátulas de álbum incrustadas y las guarda en cacheDir
 * para que la UI pueda cargarlas con Coil sin inflar la base de datos de Room.
 */
class MusicScanner(private val context: Context) {

    // Directorio en cache donde se guardan las carátulas.
    private val artCacheDir: File by lazy {
        File(context.cacheDir, "album_art").also { it.mkdirs() }
    }

    suspend fun scanFolder(folderUriString: String): List<Song> = withContext(Dispatchers.IO) {
        val treeUri = Uri.parse(folderUriString)
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val songs = mutableListOf<Song>()
        scanRecursive(rootDoc, folderUriString, songs)
        songs
    }

    private fun scanRecursive(dir: DocumentFile, folderUriString: String, out: MutableList<Song>) {
        val children = dir.listFiles()
        for (file in children) {
            if (file.isDirectory) {
                scanRecursive(file, folderUriString, out)
            } else if (isAudioFile(file.name)) {
                val audioBaseName = file.name?.substringBeforeLast('.')
                val lrcFile = children.find { 
                    it.name?.substringBeforeLast('.')?.equals(audioBaseName, ignoreCase = true) == true &&
                    it.name?.substringAfterLast('.', "")?.lowercase() == "lrc"
                }
                val lrcContent = if (lrcFile != null) {
                    try {
                        context.contentResolver.openInputStream(lrcFile.uri)?.use { stream ->
                            stream.bufferedReader().readText()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al leer lrc para ${file.name}", e)
                        null
                    }
                } else null

                extractMetadata(file, folderUriString, lrcContent)?.let { out.add(it) }
            }
        }
    }

    private fun isAudioFile(name: String?): Boolean {
        if (name == null) return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in AUDIO_EXTENSIONS
    }

    private fun extractMetadata(file: DocumentFile, folderUriString: String, lrcContent: String?): Song? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.name?.substringBeforeLast('.') ?: "Desconocido"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Artista desconocido"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Álbum desconocido"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val track = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.substringBefore('/')?.toIntOrNull() ?: 0
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull() ?: 0

            // Carátula: usamos la clave "artista|álbum" para no duplicar la misma
            // imagen por cada track del álbum.
            val albumArtPath = extractOrReuseAlbumArt(retriever, artist, album)

            Song(
                uriString = file.uri.toString(),
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                folderUri = folderUriString,
                trackNumber = track,
                year = year,
                albumArtPath = albumArtPath,
                lrcContent = lrcContent
            )
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo leer: ${file.uri}", e)
            null
        } finally {
            retriever.release()
        }
    }

    /**
     * Devuelve la ruta del archivo de carátula en cache, o null si no hay imagen.
     * Usa un hash del par artista-álbum como nombre de archivo para reutilizar la
     * misma carátula entre todas las canciones del álbum.
     */
    private fun extractOrReuseAlbumArt(
        retriever: MediaMetadataRetriever,
        artist: String,
        album: String
    ): String? {
        val key = "${artist}|${album}".hashCode().toString()
        val artFile = File(artCacheDir, "$key.jpg")
        if (artFile.exists()) return artFile.absolutePath

        val pictureBytes = retriever.embeddedPicture ?: return null
        return try {
            artFile.writeBytes(pictureBytes)
            artFile.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "Error al guardar carátula para $artist / $album", e)
            null
        }
    }
}
