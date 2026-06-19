package com.pepe.musicplayer.playback

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.Equalizer
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pepe.musicplayer.MainActivity
import com.pepe.musicplayer.data.ThemeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "PlaybackService"

/**
 * Servicio que mantiene la reproducción viva en segundo plano y expone una
 * notificación con controles (play/pausa/siguiente/anterior) gracias a Media3.
 *
 * Ahora también crea un Equalizer de Android y lo expone para que
 * EqualizerScreen pueda leer/modificar las bandas mediante [instance].
 */
class PlaybackService : MediaSessionService() {

    companion object {
        /** Referencia débil al servicio en ejecución, para que EqualizerScreen
         *  pueda acceder al Equalizer sin necesidad de un Binder complejo. */
        var instance: PlaybackService? = null
            private set
    }

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    // Ecualizador global: lo creamos cuando tenemos el audioSessionId.
    var equalizer: Equalizer? = null
        private set

    // Normalizador de volumen (LoudnessEnhancer)
    private var loudnessEnhancer: android.media.audiofx.LoudnessEnhancer? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        val player = ExoPlayer.Builder(this).build().also { exoPlayer = it }

        val themeRepository = ThemeRepository(applicationContext)
        // Escuchar preferencias de volumen estable
        kotlinx.coroutines.MainScope().launch {
            themeRepository.stableVolume.collect { enabled ->
                setStableVolumeEnabled(enabled)
            }
        }

        // Crear el EQ una vez tenemos el session ID (puede cambiar si el player
        // se reconfigura internamente, por eso usamos el AnalyticsListener).
        initAudioEffects(player.audioSessionId)
        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onAudioSessionIdChanged(
                eventTime: AnalyticsListener.EventTime,
                audioSessionId: Int
            ) {
                releaseAudioEffects()
                initAudioEffects(audioSessionId)
                // Volver a aplicar el estado del normalizador
                kotlinx.coroutines.MainScope().launch {
                    val enabled = themeRepository.stableVolume.first()
                    setStableVolumeEnabled(enabled)
                }
            }
        })

        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    private fun initAudioEffects(sessionId: Int) {
        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo crear el Equalizer (sessionId=$sessionId)", e)
        }
        try {
            loudnessEnhancer = android.media.audiofx.LoudnessEnhancer(sessionId)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo crear el LoudnessEnhancer (sessionId=$sessionId)", e)
        }
    }

    private fun releaseAudioEffects() {
        try { equalizer?.release() } catch (_: Exception) {}
        equalizer = null
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        loudnessEnhancer = null
    }

    fun setStableVolumeEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.let {
                if (enabled) {
                    it.setTargetGain(800) // ganancia moderada para estabilizar volumen (8mB)
                    it.enabled = true
                } else {
                    it.enabled = false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al alternar volumen estable", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        instance = null
        releaseAudioEffects()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
