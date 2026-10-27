package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Lightweight 8-Bit Retro Audio Synthesizer for Ben The Adventurer Android Native App.
 * Generates retro square, sine, and triangle waveforms on-the-fly using AudioTrack PCM
 * without requiring bulky external media assets.
 */
object RetroSoundPlayer {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false

    private fun playTone(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        waveType: Int = 0, // 0 = Square, 1 = Sine, 2 = Triangle
        volume: Float = 0.5f
    ) {
        if (isMuted) return

        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val samples = ShortArray(numSamples)

                var phase = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    val phaseIncrement = (2.0 * Math.PI * currentFreq) / SAMPLE_RATE
                    phase += phaseIncrement

                    // Fade out envelope at the end to prevent clicks
                    val envelope = if (progress > 0.8) (1.0 - progress) / 0.2 else 1.0

                    val sampleValue: Double = when (waveType) {
                        0 -> { // Square wave
                            val raw = if (sin(phase) >= 0) 1.0 else -1.0
                            raw * envelope
                        }
                        1 -> { // Sine wave
                            sin(phase) * envelope
                        }
                        else -> { // Triangle wave
                            val normPhase = (phase / (2.0 * Math.PI)) % 1.0
                            val raw = 2.0 * kotlin.math.abs(2.0 * (normPhase - kotlin.math.floor(normPhase + 0.5))) - 1.0
                            raw * envelope
                        }
                    }

                    samples[i] = (sampleValue * Short.MAX_VALUE * volume).toInt().coerceIn(
                        Short.MIN_VALUE.toInt(),
                        Short.MAX_VALUE.toInt()
                    ).toShort()
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samples.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Release after sound finishes
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {
                // Audio synthesis fallback non-blocking
            }
        }
    }

    fun playJump() {
        playTone(startFreq = 160f, endFreq = 580f, durationMs = 140, waveType = 0, volume = 0.45f)
    }

    fun playSlide() {
        playTone(startFreq = 180f, endFreq = 80f, durationMs = 150, waveType = 2, volume = 0.4f)
    }

    fun playCoin() {
        scope.launch {
            playTone(startFreq = 987f, endFreq = 987f, durationMs = 70, waveType = 1, volume = 0.5f)
            kotlinx.coroutines.delay(65)
            playTone(startFreq = 1318f, endFreq = 1318f, durationMs = 130, waveType = 1, volume = 0.55f)
        }
    }

    fun playStomp() {
        playTone(startFreq = 220f, endFreq = 60f, durationMs = 120, waveType = 0, volume = 0.6f)
    }

    fun playHurt() {
        playTone(startFreq = 120f, endFreq = 50f, durationMs = 200, waveType = 0, volume = 0.65f)
    }

    fun playBlast() {
        playTone(startFreq = 880f, endFreq = 200f, durationMs = 110, waveType = 0, volume = 0.5f)
    }

    fun playVictory() {
        scope.launch {
            val notes = listOf(523f, 659f, 784f, 1046f, 1318f)
            for (note in notes) {
                playTone(startFreq = note, endFreq = note, durationMs = 120, waveType = 2, volume = 0.55f)
                kotlinx.coroutines.delay(90)
            }
        }
    }
}
