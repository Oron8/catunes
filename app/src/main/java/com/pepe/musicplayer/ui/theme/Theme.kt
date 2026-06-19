package com.pepe.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import com.pepe.musicplayer.R
import com.pepe.musicplayer.data.AccentColor
import com.pepe.musicplayer.data.DarkModeOption

val SixtyFourFontFamily = FontFamily(
    Font(R.font.sixtyfour_regular)
)

// Paletas oscuras
private fun darkSchemeFor(accent: AccentColor) = when (accent) {
    AccentColor.PURPLE -> darkColorScheme(primary = Color(0xFFBB86FC), secondary = Color(0xFF03DAC6))
    AccentColor.GREEN  -> darkColorScheme(primary = Color(0xFF66BB6A), secondary = Color(0xFF9CCC65))
    AccentColor.ORANGE -> darkColorScheme(primary = Color(0xFFFFA726), secondary = Color(0xFFFFCC80))
    AccentColor.BLUE   -> darkColorScheme(primary = Color(0xFF42A5F5), secondary = Color(0xFF80D8FF))
    AccentColor.RED    -> darkColorScheme(primary = Color(0xFFEF5350), secondary = Color(0xFFFF8A80))
}

// Paletas claras (mismos colores primarios, fondo claro)
private fun lightSchemeFor(accent: AccentColor) = when (accent) {
    AccentColor.PURPLE -> lightColorScheme(primary = Color(0xFF6200EE), secondary = Color(0xFF03DAC6))
    AccentColor.GREEN  -> lightColorScheme(primary = Color(0xFF388E3C), secondary = Color(0xFF558B2F))
    AccentColor.ORANGE -> lightColorScheme(primary = Color(0xFFE65100), secondary = Color(0xFFFF8F00))
    AccentColor.BLUE   -> lightColorScheme(primary = Color(0xFF1565C0), secondary = Color(0xFF0277BD))
    AccentColor.RED    -> lightColorScheme(primary = Color(0xFFC62828), secondary = Color(0xFFB71C1C))
}

@Composable
fun MusicPlayerTheme(
    accent: AccentColor = AccentColor.PURPLE,
    darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val useDark = when (darkMode) {
        DarkModeOption.DARK   -> true
        DarkModeOption.LIGHT  -> false
        DarkModeOption.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) darkSchemeFor(accent) else lightSchemeFor(accent)

    // Aplicamos el multiplicador de fuente escalando el lineHeight y letterSpacing
    // de cada estilo de la tipografía base.
    val baseTypography = MaterialTheme.typography
    val scaledTypography = baseTypography.copy(
        displayLarge   = baseTypography.displayLarge.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        displayMedium  = baseTypography.displayMedium.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        displaySmall   = baseTypography.displaySmall.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        headlineLarge  = baseTypography.headlineLarge.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        headlineMedium = baseTypography.headlineMedium.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        headlineSmall  = baseTypography.headlineSmall.scale(fontScale).copy(fontFamily = SixtyFourFontFamily),
        titleLarge     = baseTypography.titleLarge.scale(fontScale),
        titleMedium    = baseTypography.titleMedium.scale(fontScale),
        titleSmall     = baseTypography.titleSmall.scale(fontScale),
        bodyLarge      = baseTypography.bodyLarge.scale(fontScale),
        bodyMedium     = baseTypography.bodyMedium.scale(fontScale),
        bodySmall      = baseTypography.bodySmall.scale(fontScale),
        labelLarge     = baseTypography.labelLarge.scale(fontScale),
        labelMedium    = baseTypography.labelMedium.scale(fontScale),
        labelSmall     = baseTypography.labelSmall.scale(fontScale),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}

private fun TextStyle.scale(factor: Float): TextStyle =
    copy(fontSize = fontSize * factor, lineHeight = lineHeight * factor)
