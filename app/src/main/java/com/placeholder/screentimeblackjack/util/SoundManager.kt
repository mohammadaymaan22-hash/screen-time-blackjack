package com.placeholder.screentimeblackjack.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance, zero-external-dependency sound effects manager for Screen Time Blackjack.
 *
 * Synthesizes crisp 16-bit PCM casino audio waveforms (card slide, chip clink,
 * win chime, blackjack fanfare, bust thud) on first initialization and loads
 * them into [SoundPool] for zero-latency, hardware-accelerated playback.
 */
object SoundManager {

    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 44100

    private var soundPool: SoundPool? = null
    private var isInitialized = false

    private var soundCardDealId: Int = 0
    private var soundChipClickId: Int = 0
    private var soundWinId: Int = 0
    private var soundBlackjackId: Int = 0
    private var soundBustId: Int = 0
    private var soundPushId: Int = 0
    private var soundTapId: Int = 0

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool = pool

            val soundDir = File(context.cacheDir, "casino_sounds")
            if (!soundDir.exists()) {
                soundDir.mkdirs()
            }

            // Write synthesized WAV files
            val cardDealFile = File(soundDir, "card_deal.wav")
            if (!cardDealFile.exists() || cardDealFile.length() == 0L) {
                FileOutputStream(cardDealFile).use { it.write(createWav(synthCardDeal())) }
            }
            soundCardDealId = pool.load(cardDealFile.absolutePath, 1)

            val chipFile = File(soundDir, "chip_click.wav")
            if (!chipFile.exists() || chipFile.length() == 0L) {
                FileOutputStream(chipFile).use { it.write(createWav(synthChipClick())) }
            }
            soundChipClickId = pool.load(chipFile.absolutePath, 1)

            val winFile = File(soundDir, "win_chime.wav")
            if (!winFile.exists() || winFile.length() == 0L) {
                FileOutputStream(winFile).use { it.write(createWav(synthWinChime())) }
            }
            soundWinId = pool.load(winFile.absolutePath, 1)

            val bjFile = File(soundDir, "blackjack_fanfare.wav")
            if (!bjFile.exists() || bjFile.length() == 0L) {
                FileOutputStream(bjFile).use { it.write(createWav(synthBlackjackFanfare())) }
            }
            soundBlackjackId = pool.load(bjFile.absolutePath, 1)

            val bustFile = File(soundDir, "bust_thud.wav")
            if (!bustFile.exists() || bustFile.length() == 0L) {
                FileOutputStream(bustFile).use { it.write(createWav(synthBustThud())) }
            }
            soundBustId = pool.load(bustFile.absolutePath, 1)

            val pushFile = File(soundDir, "push_chime.wav")
            if (!pushFile.exists() || pushFile.length() == 0L) {
                FileOutputStream(pushFile).use { it.write(createWav(synthPushChime())) }
            }
            soundPushId = pool.load(pushFile.absolutePath, 1)

            val tapFile = File(soundDir, "tap_click.wav")
            if (!tapFile.exists() || tapFile.length() == 0L) {
                FileOutputStream(tapFile).use { it.write(createWav(synthTapClick())) }
            }
            soundTapId = pool.load(tapFile.absolutePath, 1)

            isInitialized = true
            Log.d(TAG, "SoundManager successfully initialized with synthesized casino audio.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundManager", e)
        }
    }

    fun playCardDeal() {
        soundPool?.play(soundCardDealId, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun playChipClick() {
        soundPool?.play(soundChipClickId, 0.8f, 0.8f, 1, 0, 1.0f)
    }

    fun playWin() {
        soundPool?.play(soundWinId, 1.0f, 1.0f, 2, 0, 1.0f)
    }

    fun playBlackjack() {
        soundPool?.play(soundBlackjackId, 1.0f, 1.0f, 3, 0, 1.0f)
    }

    fun playBust() {
        soundPool?.play(soundBustId, 0.8f, 0.8f, 2, 0, 1.0f)
    }

    fun playPush() {
        soundPool?.play(soundPushId, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    fun playTap() {
        soundPool?.play(soundTapId, 0.5f, 0.5f, 1, 0, 1.0f)
    }

    // =========================================================================
    // WAVEFORM SYNTHESIZERS (Pure Math / PCM 16-Bit)
    // =========================================================================

    /**
     * Card Deal Sound: Fast friction whoosh + paper snap (~85ms).
     */
    fun synthCardDeal(): ShortArray {
        val durationMs = 85
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val random = java.util.Random(12345)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // White noise filtered with rapid envelope
            val noise = (random.nextDouble() * 2.0 - 1.0)
            val envelope = exp(-t * 35.0) * (1.0 - exp(-t * 200.0))
            // High frequency snap around 2200Hz
            val snap = sin(2.0 * PI * 2200.0 * t) * exp(-t * 60.0) * 0.4
            val sampleVal = (noise * 0.6 + snap) * envelope
            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
        return samples
    }

    /**
     * Chip Click Sound: High-frequency resonant ceramic ping (2700Hz + 3800Hz, ~55ms).
     */
    fun synthChipClick(): ShortArray {
        val durationMs = 55
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq1 = 2650.0
            val freq2 = 3900.0
            val decay = exp(-t * 90.0)
            val wave = (sin(2.0 * PI * freq1 * t) * 0.6 + sin(2.0 * PI * freq2 * t) * 0.4) * decay
            samples[i] = (wave.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        return samples
    }

    /**
     * Win Chime: Ascending major triad bells (C6, E6, G6, C7, ~420ms).
     */
    fun synthWinChime(): ShortArray {
        val durationMs = 450
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00) // C6, E6, G6, C7
        val noteDelaySec = 0.075

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0

            for (n in notes.indices) {
                val noteStart = n * noteDelaySec
                if (t >= noteStart) {
                    val noteT = t - noteStart
                    val noteFreq = notes[n]
                    val envelope = exp(-noteT * 7.0)
                    val bell = sin(2.0 * PI * noteFreq * noteT) + 0.3 * sin(2.0 * PI * noteFreq * 2.0 * noteT)
                    sum += bell * envelope * 0.35
                }
            }
            samples[i] = (sum.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
        }
        return samples
    }

    /**
     * Blackjack Fanfare: Brilliant casino jackpot sparkle (~600ms).
     */
    fun synthBlackjackFanfare(): ShortArray {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(783.99, 1046.50, 1318.51, 1567.98, 2093.00) // G5, C6, E6, G6, C7
        val noteDelaySec = 0.065

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0

            for (n in notes.indices) {
                val noteStart = n * noteDelaySec
                if (t >= noteStart) {
                    val noteT = t - noteStart
                    val noteFreq = notes[n]
                    val envelope = exp(-noteT * 6.0)
                    val bell = sin(2.0 * PI * noteFreq * noteT) + 0.35 * sin(2.0 * PI * noteFreq * 2.7 * noteT)
                    sum += bell * envelope * 0.32
                }
            }
            samples[i] = (sum.coerceIn(-1.0, 1.0) * 31000).toInt().toShort()
        }
        return samples
    }

    /**
     * Bust / Loss Sound: Descending muted dual tones (250Hz -> 160Hz, ~300ms).
     */
    fun synthBustThud(): ShortArray {
        val durationMs = 300
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 250.0 - (t / (durationMs / 1000.0)) * 100.0
            val decay = exp(-t * 9.0)
            val wave = (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 0.5) * t)) * decay
            samples[i] = (wave.coerceIn(-1.0, 1.0) * 27000).toInt().toShort()
        }
        return samples
    }

    /**
     * Push Sound: Neutral dual harmony bells (880Hz + 1174Hz, ~250ms).
     */
    fun synthPushChime(): ShortArray {
        val durationMs = 250
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 10.0)
            val wave = (sin(2.0 * PI * 880.0 * t) * 0.5 + sin(2.0 * PI * 1174.66 * t) * 0.5) * decay
            samples[i] = (wave.coerceIn(-1.0, 1.0) * 25000).toInt().toShort()
        }
        return samples
    }

    /**
     * Light Tap Sound (~25ms).
     */
    fun synthTapClick(): ShortArray {
        val durationMs = 25
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 150.0)
            val wave = sin(2.0 * PI * 1800.0 * t) * decay
            samples[i] = (wave.coerceIn(-1.0, 1.0) * 22000).toInt().toShort()
        }
        return samples
    }

    /**
     * Converts a [ShortArray] of PCM audio into a standard 44-byte header WAV [ByteArray].
     */
    fun createWav(pcmSamples: ShortArray): ByteArray {
        val dataSize = pcmSamples.size * 2
        val totalSize = 36 + dataSize
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalSize)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // fmt subchunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1) // AudioFormat 1 = PCM
        buffer.putShort(1) // NumChannels = 1 (Mono)
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(SAMPLE_RATE * 2) // ByteRate = SampleRate * NumChannels * BitsPerSample/8
        buffer.putShort(2) // BlockAlign = NumChannels * BitsPerSample/8
        buffer.putShort(16) // BitsPerSample = 16

        // data subchunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(dataSize)

        for (sample in pcmSamples) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }
}
