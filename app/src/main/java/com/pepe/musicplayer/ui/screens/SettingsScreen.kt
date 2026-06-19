package com.pepe.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pepe.musicplayer.data.AccentColor
import com.pepe.musicplayer.data.DarkModeOption
import com.pepe.musicplayer.data.ThemeRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val FONT_SCALES = listOf(0.85f to "Pequeña", 1.0f to "Normal", 1.15f to "Grande", 1.3f to "Muy grande")

@Composable
fun SettingsScreen(themeRepository: ThemeRepository) {
    val accent by themeRepository.accentColor.collectAsState(initial = AccentColor.PURPLE)
    val darkMode by themeRepository.darkMode.collectAsState(initial = DarkModeOption.SYSTEM)
    val fontScale by themeRepository.fontScale.collectAsState(initial = 1.0f)
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .padding(16.dp)
    ) {
        // ── Color de acento ───────────────────────────────────────────────────
        Text("Color de acento", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.height(220.dp)) {
            items(AccentColor.entries) { color ->
                ListItem(
                    headlineContent = { Text(colorLabel(color)) },
                    trailingContent = {
                        RadioButton(
                            selected = color == accent,
                            onClick = { scope.launch { themeRepository.setAccentColor(color) } }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope.launch { themeRepository.setAccentColor(color) } }
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        // ── Modo claro / oscuro ───────────────────────────────────────────────
        Text("Modo de pantalla", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        DarkModeOption.entries.forEach { option ->
            ListItem(
                headlineContent = { Text(darkModeLabel(option)) },
                trailingContent = {
                    RadioButton(
                        selected = option == darkMode,
                        onClick = { scope.launch { themeRepository.setDarkMode(option) } }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { themeRepository.setDarkMode(option) } }
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        // ── Tamaño de fuente ──────────────────────────────────────────────────
        Text("Tamaño de fuente", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        val currentLabel = FONT_SCALES.minByOrNull { (scale, _) ->
            kotlin.math.abs(scale - fontScale)
        }?.second ?: "Normal"
        Text(currentLabel, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = fontScale,
            onValueChange = { scope.launch { themeRepository.setFontScale(snapToNearest(it)) } },
            valueRange = 0.85f..1.3f,
            steps = 2,          // 4 posiciones: 0.85, 1.0, 1.15, 1.3
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth()) {
            FONT_SCALES.forEachIndexed { index, (_, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        // ── Volumen Estable (Normalización) ──────────────────────────────────
        val stableVolume by themeRepository.stableVolume.collectAsState(initial = false)
        ListItem(
            headlineContent = { Text("Volumen Estable") },
            supportingContent = { Text("Mantiene el nivel de volumen uniforme en todas las canciones.") },
            trailingContent = {
                androidx.compose.material3.Switch(
                    checked = stableVolume,
                    onCheckedChange = { scope.launch { themeRepository.setStableVolume(it) } }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { scope.launch { themeRepository.setStableVolume(!stableVolume) } }
        )
    }
}

/** Ajusta al valor más cercano dentro de los 4 escalones posibles. */
private fun snapToNearest(value: Float): Float {
    val scales = FONT_SCALES.map { it.first }
    return scales.minByOrNull { kotlin.math.abs(it - value) } ?: 1.0f
}

private fun colorLabel(color: AccentColor) = when (color) {
    AccentColor.PURPLE -> "Púrpura"
    AccentColor.GREEN  -> "Verde"
    AccentColor.ORANGE -> "Naranja"
    AccentColor.BLUE   -> "Azul"
    AccentColor.RED    -> "Rojo"
}

private fun darkModeLabel(option: DarkModeOption) = when (option) {
    DarkModeOption.SYSTEM -> "Igual que el sistema"
    DarkModeOption.DARK   -> "Siempre oscuro"
    DarkModeOption.LIGHT  -> "Siempre claro"
}
