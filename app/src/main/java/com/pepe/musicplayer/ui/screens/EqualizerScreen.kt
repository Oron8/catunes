package com.pepe.musicplayer.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.audiofx.Equalizer
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pepe.musicplayer.playback.PlaybackService

/**
 * Pantalla del ecualizador.
 *
 * Nos conectamos al PlaybackService para obtener el objeto Equalizer de Android
 * y controlamos cada banda con un Slider vertical.
 *
 * Si el servicio no está corriendo (no hay reproducción activa), mostramos un
 * mensaje en lugar de crashear.
 */
@Composable
fun EqualizerScreen() {
    val context = LocalContext.current
    var eq: Equalizer? by remember { mutableStateOf(null) }
    var enabled by remember { mutableStateOf(true) }

    // Listas que se llenamos una vez que tenemos el EQ
    val bandLevels = remember { mutableStateListOf<Float>() }
    val bandLabels = remember { mutableStateListOf<String>() }
    var minLevel by remember { mutableFloatStateOf(-1500f) }
    var maxLevel by remember { mutableFloatStateOf(1500f) }

    // Nos conectamos al servicio para obtener el Equalizer
    DisposableEffect(Unit) {
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                // PlaybackService no expone un Binder propio, pero podemos
                // acceder al singleton a través del contexto de la app.
                // Alternativa: usar un companion object en PlaybackService.
                // Aquí optamos por la solución más simple y directa.
            }
            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        // Intentamos bindear al servicio (no estricto: startService si no existe)
        val intent = Intent(context, PlaybackService::class.java)
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE)

        onDispose { try { context.unbindService(conn) } catch (_: Exception) {} }
    }

    // Como bindService sin IBinder no nos da el EQ directamente, usamos el
    // companion object del servicio para acceder al Equalizer cuando ya corre.
    // (La forma limpia sería un Binder personalizado, pero para un proyecto
    // personal esto es suficiente y mucho más simple.)
    DisposableEffect(Unit) {
        // Polling liviano: intentamos obtener el EQ cada vez que se compone la pantalla.
        val currentEq = PlaybackService.instance?.equalizer
        if (currentEq != null) {
            eq = currentEq
            enabled = currentEq.enabled
            val numBands = currentEq.numberOfBands.toInt()
            val range = currentEq.bandLevelRange
            minLevel = range[0].toFloat()
            maxLevel = range[1].toFloat()
            bandLevels.clear()
            bandLabels.clear()
            for (i in 0 until numBands) {
                bandLevels.add(currentEq.getBandLevel(i.toShort()).toFloat())
                val centerHz = currentEq.getCenterFreq(i.toShort()) / 1000
                bandLabels.add(if (centerHz >= 1000) "${centerHz / 1000}kHz" else "${centerHz}Hz")
            }
        }
        onDispose {}
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Ecualizador", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (eq == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Iniciá la reproducción para activar el ecualizador.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@Column
        }

        // Toggle habilitado/deshabilitado
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Activado")
            Switch(
                checked = enabled,
                onCheckedChange = { value ->
                    enabled = value
                    eq?.enabled = value
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Sliders verticales para cada banda
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bandLevels.forEachIndexed { index, level ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(48.dp)
                ) {
                    Text(
                        text = "${(level / 100).toInt()}dB",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    // Slider rotado 270° para hacerlo vertical
                    Slider(
                        value = level,
                        onValueChange = { newLevel ->
                            bandLevels[index] = newLevel
                            try {
                                eq?.setBandLevel(index.toShort(), newLevel.toInt().toShort())
                            } catch (_: Exception) {}
                        },
                        valueRange = minLevel..maxLevel,
                        modifier = Modifier
                            .height(150.dp)
                            .rotate(270f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bandLabels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
