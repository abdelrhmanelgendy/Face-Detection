package net.gamal.faceapprecon.utilities.tone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.math.sin

internal object TonesGenerator : ITonesGenerator {

    private const val FREQ_ONLINE_REQUEST = 1050
    private const val FREQ_DECLINED = 3060
    private const val FREQ_NFC_TAPPED = 1100

    private const val SAMPLE_RATE = 44100 // Standard 44.1 KHz
    private var audioTrack: AudioTrack? = null
    private var volume = 0.5f

    //        For the row frequency: Approximately 941 Hz
    //                For the column frequency: Approximately 1209 Hz

    //        For the row frequency: Approximately 1400 Hz
//                For the column frequency: Approximately 2060 Hz
    override fun registerSuccessfulTone() {
        playSound(FREQ_ONLINE_REQUEST.toDouble(), 300.0)
     }

    override fun registerDeclinedTone() {
        playSound(FREQ_DECLINED.toDouble(), 300.0)
    }

    override fun registerDefaultTone(timeInMillis:Double) {
        playSound(FREQ_NFC_TAPPED.toDouble(), timeInMillis)
    }

    private fun playSound(freqOfTone: Double, durationInMillis: Double) {
        val audioBuffer = ByteArrayOutputStream()
        try {
            audioBuffer.write(genTone(durationInMillis, freqOfTone))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack.Builder().setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            ).setAudioFormat(
                AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            ).setBufferSizeInBytes(bufferSize).build()

            val maxVolume = AudioTrack.getMaxVolume()
            if (volume > maxVolume) {
                volume = maxVolume
            } else if (volume < 0) {
                volume = 0f
            }
            audioTrack!!.setVolume(volume)
            audioTrack!!.play() // Play the track
            audioTrack!!.write(
                audioBuffer.toByteArray(), 0, audioBuffer.size()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopTone()
    }

    private fun stopTone() {
        if (audioTrack != null && audioTrack!!.state == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack!!.stop()
            audioTrack!!.release()
        }
    }

    private fun genTone(durationInMillis: Double = 400.0, freqOfTone: Double = 440.0): ByteArray {
        val numSamples = ((durationInMillis) * SAMPLE_RATE)/1000
        val sample = DoubleArray(numSamples.roundToInt())
        val generatedSnd = ByteArray(2 * numSamples.roundToInt())

        for (i in 0 until numSamples.roundToInt()) {
            sample[i] = sin(2 * Math.PI * i / (SAMPLE_RATE / freqOfTone))
        }

        var idx = 0
        for (dVal in sample) {
            val ampValue = (dVal * 32767)
            generatedSnd[idx++] = (ampValue.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = (ampValue.toInt() and 0xff00 ushr 8).toByte()
        }
        return generatedSnd
    }
}