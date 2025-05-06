
package com.example.wearosapp.presentation


import android.content.pm.PackageManager
import android.os.Build

import android.os.Bundle

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VibrateListenerActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private var showVibrationMessage by mutableStateOf(false)
    private var mes: String? = null
    private var durationDelay: Long? = null
    //권한
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "알림 권한 허용됨", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "알림 권한 거부됨", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        checkPermissions()
        Wearable.getMessageClient(this).addListener(this) //이부분
        // 앱이 실행 중임을 상태로 표시
        VibrateAppState.setAppRunningState(true)
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showVibrationMessage) "$mes!" else "대기 중...",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        Wearable.getMessageClient(this).addListener(this)
        VibrateAppState.setAppRunningState(true)
    }

    override fun onStop() {
        super.onStop()
        Wearable.getMessageClient(this).removeListener(this)
        VibrateAppState.setAppRunningState(false)
    }

    override fun onDestroy() {
        Wearable.getMessageClient(this).removeListener(this)
        VibrateAppState.setAppRunningState(false)
        super.onDestroy()

    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/vibrate") {

            // 1) Logcat에 수신 로그 남기기
            Log.d("WearApp", "포그라운드 진동 수신 완료: $event")
            val payload = VibrationPayload.fromJsonData(event.data)
            Log.d("WearApp", "수신된 payload → duration=${payload?.duration}, amplitude=${payload?.amplitude}, message=${payload?.message}")
            if (payload != null) {
                try {
                    mes = payload.message
                    durationDelay = payload.duration
                    Toast.makeText(this, payload.message, Toast.LENGTH_SHORT).show()
                    resetAfterDelay()
                    VibrationHelper.vibrate(this, payload.duration, payload.amplitude)
                    showVibrationMessage = true

                } catch (e: Exception) {
                    Log.e("WearApp", "처리 실패: ${e.message}")
                }
            }
            else {
                Log.w("WearApp", "Json 불러오기 실패")
            }
        }
    }

    private fun resetAfterDelay() {
        // CoroutineScope를 사용해서 3초 후 자동으로 복구
        lifecycleScope.launch {
            delay(durationDelay ?: 3000L) // 기본은 3초 대기
            showVibrationMessage = false
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 요청
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}
