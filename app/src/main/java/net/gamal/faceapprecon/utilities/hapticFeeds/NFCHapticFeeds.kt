package net.gamal.faceapprecon.utilities.hapticFeeds

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import net.gamal.faceapprecon.utilities.tone.TonesGenerator

internal class NFCHapticFeeds(context: Context) : INFCHapticFeeds {

    override fun holdHapticForNFCPlaced(durationInMillis: Int) {
        playVibrator(durationInMillis)
        playSound(durationInMillis)
    }

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playSound(durationInMillis: Int) {
        TonesGenerator.registerSuccessfulTone()
    }

    private fun playVibrator(durationInMillis: Int) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                durationInMillis.toLong(), VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }
}