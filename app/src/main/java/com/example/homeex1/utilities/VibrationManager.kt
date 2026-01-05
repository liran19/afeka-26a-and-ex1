package com.example.homeex1.utilities

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Singleton for managing device vibration
 */
object VibrationManager {

    private var vibrator: Vibrator? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibrator = vibratorManager?.defaultVibrator

        isInitialized = true
    }

    fun vibrate(milliseconds: Long = 100) {
        if (!isInitialized || vibrator?.hasVibrator() != true) {
            return
        }

        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                milliseconds,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }

    fun cancel() {
        vibrator?.cancel()
    }
}

