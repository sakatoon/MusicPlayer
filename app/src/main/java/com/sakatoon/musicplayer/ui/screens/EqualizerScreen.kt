package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sakatoon.musicplayer.audio.AudioEqualizer
import com.sakatoon.musicplayer.audio.EqualizerPresets

@Composable
fun EqualizerScreen() {
    val state by AudioEqualizer.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text("Ecualizador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Ajusta el sonido a tu gusto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = AudioEqualizer::setEnabled,
                    enabled = state.isAvailable
                )
            }
        }

        if (!state.isAvailable) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        state.error ?: "Reproduce una canción para iniciar el ecualizador.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                Text("Perfiles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EqualizerPresets.names.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { name ->
                                AssistChip(
                                    onClick = { AudioEqualizer.applyPreset(name) },
                                    label = { Text(name) },
                                    enabled = state.isEnabled
                                )
                            }
                        }
                    }
                }
            }

            items(state.bands.size, key = { state.bands[it].index }) { index ->
                val band = state.bands[index]
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatFrequency(band.frequencyHz), fontWeight = FontWeight.Medium)
                        Text("${(band.level * 12).toInt()} dB", color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = band.level,
                        onValueChange = { AudioEqualizer.setBand(index, it) },
                        valueRange = -1f..1f,
                        enabled = state.isEnabled
                    )
                }
            }

            item {
                Button(
                    onClick = { AudioEqualizer.applyPreset("Plano") },
                    enabled = state.isEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Restablecer a plano") }
                Text(
                    "Los cambios se aplican solamente al audio reproducido por MusicPlayer.",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatFrequency(hz: Int): String = if (hz >= 1000) {
    val value = hz / 1000f
    if (value % 1f == 0f) "${value.toInt()} kHz" else "%.1f kHz".format(value)
} else "$hz Hz"
