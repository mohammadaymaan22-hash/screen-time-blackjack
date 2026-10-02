package com.placeholder.screentimeblackjack.util

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SoundManagerTest {

    @Test
    fun testWavGenerationHeader() {
        val samples = ShortArray(441) { 1000 }
        val wav = SoundManager.createWav(samples)

        assertEquals(44 + 441 * 2, wav.size)
        // Verify RIFF header
        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])

        // Verify WAVE
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])

        val buffer = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN)
        val sampleRate = buffer.getInt(24)
        assertEquals(44100, sampleRate)
        val channels = buffer.getShort(22)
        assertEquals(1, channels.toInt())
        val bitsPerSample = buffer.getShort(34)
        assertEquals(16, bitsPerSample.toInt())
    }

    @Test
    fun testSynthesizersProduceNonEmptyAudio() {
        val deal = SoundManager.synthCardDeal()
        assertTrue(deal.isNotEmpty())
        assertTrue(deal.any { it != 0.toShort() })

        val chip = SoundManager.synthChipClick()
        assertTrue(chip.isNotEmpty())
        assertTrue(chip.any { it != 0.toShort() })

        val win = SoundManager.synthWinChime()
        assertTrue(win.isNotEmpty())
        assertTrue(win.any { it != 0.toShort() })

        val bj = SoundManager.synthBlackjackFanfare()
        assertTrue(bj.isNotEmpty())
        assertTrue(bj.any { it != 0.toShort() })

        val bust = SoundManager.synthBustThud()
        assertTrue(bust.isNotEmpty())
        assertTrue(bust.any { it != 0.toShort() })
    }
}
