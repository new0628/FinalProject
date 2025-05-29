package com.example.finalproject

import android.content.Context

// 진동 세기, 지속 시간 영구 저장 Object
object CustomVibrationPreference {
    private const val BASIC_NAME = "custom_vibration"
//    private const val BASIC_AMPLITUDE = "amplitude"
//    private const val BASIC_DURATION = "duration"


    // 기본 키 템플릿
    private fun keyAmp(type: String)    = "${type}_amplitude"
    private fun keyDur(type: String)    = "${type}_duration"

    fun save(context: Context, type: String, amplitude: Int, duration: Long) {
        val prefs = context.getSharedPreferences(BASIC_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(keyAmp(type), amplitude)
            putLong(keyDur(type), duration)
            apply()
        }
    }

    fun load(context: Context, type: String): Pair<Int, Long> {
        val prefs = context.getSharedPreferences(BASIC_NAME, Context.MODE_PRIVATE)
        val amp = prefs.getInt(keyAmp(type), /* default */128)
        val dur = prefs.getLong(keyDur(type), /* default */3000L)
        return amp to dur
    }

    fun loadForMode(context: Context, mode: String): Pair<Int, Long> {
        val prefs = context.getSharedPreferences("custom_vibration", Context.MODE_PRIVATE)
        val ampKey = "${mode}_amplitude"
        val durKey = "${mode}_duration"

        val amplitude = prefs.getInt(ampKey, 128)
        val duration = prefs.getLong(durKey, 3000L)

        return Pair(amplitude, duration)
    }

}