package com.sakatoon.musicplayer.audio

import android.content.Context
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class EqualizerBand(val index: Short, val frequencyHz: Int, val level: Float)

data class EqualizerState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val error: String? = null
)

object EqualizerPresets {
    val names = listOf("Plano", "Rock", "Pop", "Electrónica", "Voz")
    private val profiles = mapOf(
        "Plano" to floatArrayOf(0f, 0f, 0f, 0f, 0f),
        "Rock" to floatArrayOf(.65f, .3f, -.2f, .25f, .6f),
        "Pop" to floatArrayOf(-.1f, .25f, .5f, .25f, -.05f),
        "Electrónica" to floatArrayOf(.7f, .35f, -.15f, .35f, .7f),
        "Voz" to floatArrayOf(-.4f, -.1f, .65f, .45f, -.2f)
    )

    fun values(name: String, bandCount: Int): FloatArray {
        require(bandCount > 0)
        val source = profiles[name] ?: profiles.getValue("Plano")
        if (bandCount == 1) return floatArrayOf(source[source.size / 2])
        return FloatArray(bandCount) { index ->
            val position = index.toFloat() * (source.lastIndex.toFloat() / (bandCount - 1))
            val left = position.toInt()
            val right = (left + 1).coerceAtMost(source.lastIndex)
            val fraction = position - left
            (source[left] + (source[right] - source[left]) * fraction).coerceIn(-1f, 1f)
        }
    }
}

object AudioEqualizer {
    private const val PREFS = "audio_equalizer"
    private var effect: Equalizer? = null
    private var minLevel = 0
    private var maxLevel = 0
    private var appContext: Context? = null
    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    @Synchronized
    fun attach(context: Context, audioSessionId: Int) {
        if (audioSessionId <= 0) return
        release()
        appContext = context.applicationContext
        try {
            val equalizer = Equalizer(0, audioSessionId)
            effect = equalizer
            minLevel = equalizer.bandLevelRange[0].toInt()
            maxLevel = equalizer.bandLevelRange[1].toInt()
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", false)
            val bands = (0 until equalizer.numberOfBands.toInt()).map { number ->
                val index = number.toShort()
                val normalized = prefs.getFloat("band_$number", 0f).coerceIn(-1f, 1f)
                equalizer.setBandLevel(index, normalizedToLevel(normalized))
                EqualizerBand(index, equalizer.getCenterFreq(index) / 1000, normalized)
            }
            equalizer.enabled = enabled
            _state.value = EqualizerState(true, enabled, bands)
        } catch (_: Exception) {
            release()
            _state.value = EqualizerState(error = "El ecualizador no está disponible en este dispositivo")
        }
    }

    fun setEnabled(enabled: Boolean) {
        val equalizer = effect ?: return
        try {
            equalizer.enabled = enabled
            appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean("enabled", enabled)?.apply()
            _state.value = _state.value.copy(isEnabled = enabled)
        } catch (_: Exception) {
            _state.value = _state.value.copy(error = "No se pudo activar el ecualizador")
        }
    }

    fun setBand(index: Int, normalized: Float) {
        val equalizer = effect ?: return
        val band = _state.value.bands.getOrNull(index) ?: return
        val value = normalized.coerceIn(-1f, 1f)
        try {
            equalizer.setBandLevel(band.index, normalizedToLevel(value))
            val updated = _state.value.bands.toMutableList()
            updated[index] = band.copy(level = value)
            _state.value = _state.value.copy(bands = updated, error = null)
            appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putFloat("band_$index", value)?.apply()
        } catch (_: Exception) {
            _state.value = _state.value.copy(error = "No se pudo ajustar esta banda")
        }
    }

    fun applyPreset(name: String) {
        EqualizerPresets.values(name, _state.value.bands.size).forEachIndexed(::setBand)
    }

    @Synchronized
    fun release() {
        effect?.release()
        effect = null
    }

    private fun normalizedToLevel(value: Float): Short {
        val level = if (value >= 0) value * maxLevel else -value * minLevel
        return level.roundToInt().coerceIn(minLevel, maxLevel).toShort()
    }
}
