package com.example.wearosapp.presentation

import android.content.Intent

import android.util.Log

import com.example.wearosapp.presentation.VibrateAppState.isAppRunning
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


//백그라운드 환경
class VibrateListenerService : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/vibrate") {
            Log.d("WearApp", "백그라운드 진동 수신 완료: $event")

            if (isAppRunning) {
                Log.d("WearApp", "앱 실행중 백그라운드 진동 알림 생략")
                return
            }

            val payload = VibrationPayload.fromJsonData(event.data)
            Log.d("WearApp", "수신된 payload → duration=${payload?.duration}, amplitude=${payload?.amplitude}, message=${payload?.message}")
            if (payload != null) {
                try {
                    VibrationHelper.vibrate(this, payload.duration, payload.amplitude)
                    val intent = Intent(this, VibrationDisplayActivity::class.java).apply {
                        putExtra("message", payload.message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                    }
                    startActivity(intent)

                    Log.d("WearApp", "백그라운드 메시지 표시 완료: ${payload.message}")
                } catch (e: Exception) {
                    Log.e("WearApp", "백그라운드 알림 처리중")
                }
            }
            else {
                Log.w("WearApp", "Json 불러오기 실패")
            }
        }
    }
}