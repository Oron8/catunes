package com.pepe.musicplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pepe.musicplayer.data.AccentColor
import com.pepe.musicplayer.data.DarkModeOption
import com.pepe.musicplayer.data.ThemeRepository
import com.pepe.musicplayer.ui.LibraryViewModel
import com.pepe.musicplayer.ui.MusicPlayerNavHost
import com.pepe.musicplayer.ui.PlaylistViewModel
import com.pepe.musicplayer.ui.PlayerViewModel
import com.pepe.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {

    private val libraryViewModel: LibraryViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // No hacemos nada especial si el usuario deniega: la música sigue
            // sonando, simplemente sin notificación en la pantalla de bloqueo.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pedir POST_NOTIFICATIONS solo en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val themeRepository = ThemeRepository(applicationContext)

        setContent {
            val accent    by themeRepository.accentColor.collectAsState(initial = AccentColor.PURPLE)
            val darkMode  by themeRepository.darkMode.collectAsState(initial = DarkModeOption.SYSTEM)
            val fontScale by themeRepository.fontScale.collectAsState(initial = 1.0f)

            MusicPlayerTheme(accent = accent, darkMode = darkMode, fontScale = fontScale) {
                MusicPlayerNavHost(
                    libraryViewModel = libraryViewModel,
                    playerViewModel = playerViewModel,
                    playlistViewModel = playlistViewModel,
                    themeRepository = themeRepository
                )
            }
        }
    }
}
