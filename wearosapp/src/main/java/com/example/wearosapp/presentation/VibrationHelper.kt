package com.example.wearosapp.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log


object VibrationHelper {
    fun vibrate(context: Context, duration: Long = 1000L, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        }
        else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        }
        else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }

        Log.d("VibrationHelper", "진동 실행됨 (duration: $duration, amplitude: $amplitude")
    }
}