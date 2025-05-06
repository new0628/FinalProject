package com.example.finalproject

import android.content.Context

// 진동 세기, 지속 시간 영구 저장 Object
object CustomVibrationPreference {
    private const val BASIC_NAME = "custom_vibration"
    private const val BASIC_AMPLITUDE = "amplitude"
    private const val BASIC_DURATION = "duration"

    fun save(context: Context, amplitude: Int, duration: Long) {
        val prefs = context.getSharedPreferences(BASIC_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(BASIC_AMPLITUDE, amplitude)
            putLong(BASIC_DURATION, duration)
            apply()
        }
    }

    fun load(context: Context): Pair<Int, Long> {
        val prefs = context.getSharedPreferences(BASIC_NAME, Context.MODE_PRIVATE)
        val amplitude = prefs.getInt(BASIC_AMPLITUDE, 128)
        val duration = prefs.getLong(BASIC_DURATION, 3000L)
        return Pair(amplitude, duration)
    }
}