package com.sakatoon.musicplayer.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EqualizerPresetsTest {
    @Test
    fun flatPresetAlwaysMatchesDeviceBandCount() {
        assertArrayEquals(floatArrayOf(0f, 0f, 0f), EqualizerPresets.values("Plano", 3), 0f)
    }

    @Test
    fun presetsAreInterpolatedAndRemainNormalized() {
        val values = EqualizerPresets.values("Rock", 7)
        assertEquals(7, values.size)
        assertEquals(0.65f, values.first(), 0.001f)
        assertEquals(0.60f, values.last(), 0.001f)
        assertEquals(true, values.all { it in -1f..1f })
    }

    @Test(expected = IllegalArgumentException::class)
    fun bandCountMustBePositive() {
        EqualizerPresets.values("Plano", 0)
    }
}
