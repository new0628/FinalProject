package com.example.wearosapp.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/** Wear OS 백그라운드 수신 서비스 */
class VibrateListenerService : WearableListenerService() {

//    companion object {
//        const val ACTION_UPDATE_UI = "com.example.wearosapp.UPDATE_UI"   // ← 과거 호환용(사용 안 함)
//        const val EXTRA_MESSAGE    = "extra_message"
//        const val EXTRA_COLOR      = "extra_color"
//    }

    @Suppress("InlinedApi")          // FLAG_ACTIVITY_*
    @SuppressLint("InlinedApi")
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != "/vibrate") return
        Log.d("WearApp", "백그라운드 수신: $event")

        // 포그라운드에 앱이 떠 있으면 백그라운드용 UI는 띄우지 않음
        if (VibrateAppState.isAppRunning) {
            Log.d("WearApp", "앱이 포그라운드에 떠 있어 백그라운드 알림 스킵")
            return
        }

        val payload = VibrationPayload.fromJsonData(event.data) ?: return
        Log.d("WearApp", "payload = $payload")

        try {
            // 1) 진동
            VibrationHelper.vibrate(this, payload.duration, payload.amplitude)

            // 2) (선택) 화면 깨우는 Activity
            val color = pickColor(payload.message)
            val intent = Intent(this, VibrationDisplayActivity::class.java).apply {
                putExtra("message", payload.message)
                putExtra("color",   color)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)

            // 3) 모든 UI 에 알림 (SharedFlow)
            VibrationUiBus.notify(payload.message, color, payload.duration)

        } catch (e: Exception) {
            Log.e("WearApp", "백그라운드 처리 오류", e)
        }
    }

    /** 글자 → 색상 매핑 */
    private fun pickColor(message: String): Int = when (message) {
        "사이렌" -> 0xFFFFFF00.toInt()                // 노란색
        "경적" -> 0xFFFFFF00.toInt()                  // 노란색
        "초록불입니다. 출발하세요.", "초록불입니다. 출발하세요" -> 0xFF00FF00.toInt()   // 초록색
        "차량 충돌이 발생했습니다." -> 0xFFFF0000.toInt()   // 빨간색
        else -> 0xFF000000.toInt()   // 기본 검정
    }
}