# TODO — Mi Reproductor

Lista de lo que falta, con suficiente detalle técnico como para que cuando lo
ataques sepas por dónde arrancar. Ordenado más o menos por "lo más fácil/útil
primero" pero no es obligatorio hacerlo en ese orden.

---

## 🔴 Cosas chicas que conviene resolver pronto (no son features, son baches)

### 1. Pedir el permiso de notificaciones en runtime
En Android 13+ (`POST_NOTIFICATIONS`) hace falta pedirlo en runtime, no alcanza
con declararlo en el manifest. Hoy la app lo declara pero nunca lo pide, así
que si el usuario no lo activa a mano en Ajustes, la notificación de
reproducción no aparece (igual suena la música, solo que sin control desde la
notificación/lockscreen).

**Dónde tocar:** `MainActivity.kt`, en `onCreate`, usando
`ActivityResultContracts.RequestPermission()` (mismo patrón que ya usás para
`OpenDocumentTree` en `FoldersScreen.kt`). Pedirlo solo si
`Build.VERSION.SDK_INT >= 33`.

### 2. Manejar permisos "revocados" de carpetas
Si el usuario borra la carpeta del celular, la mueve, o le saca el permiso a
mano desde Ajustes de Android, `DocumentFile.fromTreeUri(...)` te va a devolver
algo que falla al escanear. Hoy el scanner silenciosamente no agrega nada
(`return@withContext emptyList()`), pero la carpeta sigue apareciendo en la
lista de "Carpetas" como si nada.

**Qué hacer:** en `FoldersScreen.kt`, mostrar un ícono de advertencia si
`DocumentFile.fromTreeUri(...).exists()` da `false`, y ofrecer "Quitar"
directamente.

### 3. Cancelar un escaneo en progreso
Si tenés una carpeta con miles de archivos, escanear tarda, y hoy no hay forma
de cancelarlo desde la UI (el botón "Re-escanear" se puede tocar de nuevo y
quedan corutinas duplicadas corriendo).

**Qué hacer:** guardar el `Job` de la corutina de escaneo en el ViewModel y
cancelarlo antes de lanzar uno nuevo, o deshabilitar el botón mientras
`isScanning == true` (esto último es más simple, alcanza para el MVP).

### 4. Manejo de errores visible
Hoy si `MusicScanner` falla en un archivo puntual, lo descarta calladito
(`catch (e: Exception) { null }`). Está bien para no frenar el escaneo
completo por un archivo corrupto, pero no hay forma de que el usuario sepa
"che, 3 archivos no se pudieron leer". Como mínimo, loguealo con `Log.w` para
poder debuggear con Logcat.

---

## 🟡 Nivel "Medio" (lo que charlamos como segunda etapa)

### 5. Carátulas de álbum
Ya extraemos toda la metadata textual; falta sacar la imagen embebida.

**Cómo:** `MediaMetadataRetriever` tiene `retriever.embeddedPicture`, que te
devuelve un `ByteArray` con la imagen (si el archivo la tiene incrustada, cosa
común en MP3/FLAC/M4A). El truco es que **no conviene guardarla en Room**
directamente (la base se haría gigante); lo que se hace normalmente es:

1. Al escanear, si `embeddedPicture != null`, guardar ese `ByteArray` como un
   archivo `.jpg` en el cache interno de la app (`context.cacheDir`), con
   nombre derivado del hash del álbum (artista+álbum), para no duplicar la
   misma carátula por cada track.
2. Guardar en el campo `Song.albumArtPath` (ya está el campo pensado en el
   modelo, solo falta poblarlo y usarlo — ah, momento: lo saqué del modelo
   final por simplicidad, así que primero hay que volver a agregarlo a
   `Song.kt` y a la migración de Room).
3. En la UI (`LibraryScreen.kt`, `PlayerScreen.kt`), cargar esa ruta con Coil
   (`io.coil-kt:coil-compose`, ya lo había evaluado, no quedó en las
   dependencias finales — hay que agregarlo al `build.gradle.kts`).

**Ojo con Room y versiones:** si agregás un campo nuevo a `Song`, tenés que
subir `version = 2` en `@Database` y o bien escribir una `Migration`, o (más
simple para un proyecto personal) usar `.fallbackToDestructiveMigration()` en
el builder de `MusicDatabase`, que borra y re-crea la tabla (total, la
biblioteca se reconstruye con un re-escaneo, no perdés nada importante).

### 6. Shuffle y Repeat
Esto es lo más fácil de toda la lista — Media3 lo trae nativo, no hay que
programar lógica de aleatoriedad a mano.

**Cómo:** en `MusicController.kt`, exponer dos funciones:
```kotlin
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
```
Y agregar dos botones más en `PlayerScreen.kt`. Es literal una tarde de laburo,
arrancaría por acá si tuviera que priorizar.

### 7. Playlists propias
Necesita:
- Una entidad nueva `Playlist(id, name)` y una tabla puente
  `PlaylistSongCrossRef(playlistId, songUri, position)` en Room (relación
  muchos-a-muchos, con orden).
- DAOs nuevos para crear/borrar playlist, agregar/quitar canción, reordenar.
- Pantalla nueva (`PlaylistsScreen.kt`) + ruta nueva en `Navigation.kt`.
- Desde `LibraryScreen.kt`, un long-press o botón "..." en cada canción para
  "Agregar a playlist".

Es el ítem más grande de esta lista en términos de código nuevo, pero no tiene
nada conceptualmente difícil, es CRUD + UI.

### 8. Ecualizador
Dos caminos:

- **`android.media.audiofx.Equalizer`** (API de Android, no de Media3): se
  engancha al `audioSessionId` del `ExoPlayer`. Es lo más directo:
  ```kotlin
  val equalizer = Equalizer(0, exoPlayer.audioSessionId)
  equalizer.enabled = true
  equalizer.setBandLevel(0, 500) // banda 0, +5dB
  ```
  Ojo: el `audioSessionId` puede cambiar si el player se reconfigura, hay que
  re-crear el `Equalizer` si eso pasa (escuchar
  `Player.Listener.onAudioSessionIdChanged` — este callback en Media3 puede
  requerir usar `AnalyticsListener` en vez de `Player.Listener` según la
  versión, conviene chequear la doc de Media3 1.3.x al momento de hacerlo).

- **Media3 `AudioProcessor` custom**: más prolijo y portable, pero mucho más
  laburo (hay que escribir el DSP del EQ vos mismo o usar una librería). Para
  un reproductor personal, la opción de arriba (`Equalizer` de Android) alcanza
  y sobra.

La UI sería un set de `Slider` verticales (uno por banda de frecuencia), Media
Material3 no trae un componente armado para esto, hay que armarlo con
`Slider` rotado o un `Canvas` custom.

### 9. Más opciones de tema
Hoy solo hay color de acento. Cosas fáciles de sumar en `ThemeRepository.kt` +
`SettingsScreen.kt`:
- Forzar modo claro/oscuro (hoy usa `darkColorScheme` siempre fijo; para modo
  claro real necesitás también definir `lightColorScheme` por cada
  `AccentColor` en `Theme.kt`).
- Tamaño de fuente (multiplicador sobre `MaterialTheme.typography`).
- Layout de biblioteca: lista vs grilla de carátulas (una vez que tengas el
  ítem 5 resuelto, esto es casi gratis con un `LazyVerticalGrid` en vez de
  `LazyColumn`).

---

## 🟢 Nivel "Completo" (la frutilla del postre)

### 10. Búsqueda y filtros
Un `SearchBar` de Material3 arriba de `LibraryScreen.kt`, filtrando la lista
de `songs` en memoria por título/artista/álbum (con la cantidad de canciones
que vas a tener — probablemente cientos, no decenas de miles — filtrar en
memoria con `.filter {}` sobre el `StateFlow` ya cacheado de Room alcanza,
no hace falta una query SQL con `LIKE`, aunque también se podría agregar un
`@Query` con `LIKE '%:query%'` en `SongDao` si en algún momento la librería
crece mucho).

Filtros por género: ojo que `MediaMetadataRetriever` también tiene
`METADATA_KEY_GENRE`, no lo estamos leyendo en `MusicScanner.kt` todavía — hay
que sumarlo al modelo `Song` (mismo tema de migración de Room que en el ítem
5).

### 11. Widget de home screen
Esto es una bestia aparte de Android: un `AppWidgetProvider` + un layout XML
de `RemoteViews` (los widgets clásicos NO usan Compose directamente, aunque
existe Glance, que es la forma "moderna" de hacer widgets con sintaxis
parecida a Compose —`androidx.glance:glance-appwidget`—, recomendado por sobre
RemoteViews a mano si vas a arrancar de cero hoy).

Necesita: mostrar carátula + título + artista + botones play/pausa/siguiente,
que mandan un `PendingIntent` con una acción custom que tu `PlaybackService`
escucha (`onStartCommand`) para controlar el player. Es la feature más
compleja de toda la lista en términos de "cosas nuevas de Android que
aprender", calculale un fin de semana entero si es la primera vez que tocás
widgets.

### 12. Gestos (swipe para siguiente/anterior)
En `PlayerScreen.kt`, envolver el contenido en un
`Modifier.pointerInput { detectHorizontalDragGestures { ... } }` y, según la
dirección y el umbral de distancia, llamar `playerViewModel.skipNext()` o
`skipPrevious()`. Relativamente simple, una tarde.

### 13. Letras sincronizadas (.lrc)
Si al lado del archivo de audio hay un `.lrc` con el mismo nombre (formato
estándar: líneas tipo `[01:23.45]Letra de la línea`), se puede:
1. En `MusicScanner.kt`, al encontrar `cancion.mp3`, buscar también
   `cancion.lrc` en la misma carpeta (`DocumentFile` te deja listar hermanos).
2. Parsearlo a una lista de `(timestampMs, texto)`.
3. En `PlayerScreen.kt`, resaltar la línea correspondiente a
   `state.positionMs` actual, con un `LazyColumn` que hace auto-scroll.

No hay librería estándar de Android para esto, el parser de `.lrc` es texto
plano y se escribe a mano en 30-40 líneas de Kotlin con regex.

### 14. Carpetas tipo "auto-detectar" sin elegir una por una
Hoy el usuario tiene que elegir carpetas a mano con el selector SAF. Una
alternativa más cómoda (pero con trade-offs de privacidad/permisos) sería usar
el **`MediaStore`** de Android (`MediaStore.Audio.Media`), que ya tiene
indexada toda la música del dispositivo sin que vos tengas que escanear nada.
Requiere el permiso `READ_MEDIA_AUDIO` (Android 13+) o `READ_EXTERNAL_STORAGE`
(versiones viejas), que es más invasivo que SAF pero te ahorra el escaneo
manual. Se podría ofrecer como una opción más ("Importar desde el sistema")
además del selector de carpetas actual, no en reemplazo.

---

## 🧪 Testing (si en algún momento te interesa, no es prioridad para uso personal)

- Tests unitarios del `MusicScanner` con archivos de audio de prueba en
  `androidTest` (necesita `Context` real, así que van como instrumented test,
  no test unitario puro).
- Tests de Compose UI con `createComposeRule()` para las pantallas.

No lo pondría arriba en la lista de prioridades para una app de uso personal,
pero si en algún momento sumás gente que la usa o la subís a algún lado, vale
la pena.

---

## Orden sugerido si tuviera que elegir

1. Shuffle/Repeat (ítem 6) — rapidísimo, alto impacto.
2. Permiso de notificaciones en runtime (ítem 1) — bache molesto de UX.
3. Carátulas de álbum (ítem 5) — cambia mucho la sensación de la app.
4. Playlists (ítem 7).
5. Lo demás, según ganas.

Pedime cualquiera de estos cuando quieras y lo armamos paso a paso como hoy.
