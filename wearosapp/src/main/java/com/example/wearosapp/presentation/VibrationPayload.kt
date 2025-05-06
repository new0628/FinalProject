package com.example.wearosapp.presentation

import android.os.VibrationEffect
import android.util.Log
import org.json.JSONObject

data class VibrationPayload(
    val duration: Long,
    val amplitude: Int,
    val message: String
) {
    companion object {
        fun fromJsonData(data: ByteArray): VibrationPayload? {
            return try {
                val json = JSONObject(String(data))
                VibrationPayload(
                    duration = json.optLong("duration", 1000L),
                    amplitude = json.optInt("amplitude", VibrationEffect.DEFAULT_AMPLITUDE),
                    message = json.optString("message", "알림")
                )
            } catch (e: Exception) {
                Log.d("WearApp", "Payload err: $e")
                null
            }
        }
    }
}
