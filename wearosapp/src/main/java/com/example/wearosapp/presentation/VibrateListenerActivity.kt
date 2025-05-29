package com.example.wearosapp.presentation

import android.Manifest
import android.content.pm.PackageManager

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/** Wear OS 포그라운드 UI Activity */
class VibrateListenerActivity : ComponentActivity(),
    MessageClient.OnMessageReceivedListener {

    /* ---------- 상태 ---------- */
    private var show       by mutableStateOf(false)
    private var msg        by mutableStateOf("대기 중…")
    private var bg         by mutableStateOf(Color.Black)
    private var delayMs: Long = 3_000L          // 기본 3초

    /* ---------- 권한 런처 ---------- */
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val toast = if (granted) "알림 권한 허용됨" else "알림 권한 거부됨"
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
    }

    /* ---------- 생명주기 ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        requestNotificationPermission()

        Wearable.getMessageClient(this).addListener(this)
        VibrateAppState.setAppRunningState(true)

        // ── SharedFlow 수신 : Service 와 동일한 UI 상태 반영 ──
        lifecycleScope.launchWhenStarted {
            VibrationUiBus.events.collect { ev ->
                msg     = ev.message
                bg      = Color(ev.color)
                delayMs = ev.duration
                show    = true
                resetAfterDelay()
            }
        }

        // ── Compose UI ──
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    if (show) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (msg.contains("차량 충돌")) "🚨\n$msg" else msg,
                                textAlign = TextAlign.Center,
                                color = if (bg == Color.Black) Color.White else Color.Black // 대비되게 처리
                            )
                        }
                    } else {
                        Text(
                            text = "대기 중…",
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onStop() {
        super.onStop()
        Wearable.getMessageClient(this).removeListener(this)
        VibrateAppState.setAppRunningState(false)
    }
    override fun onResume() {
        super.onResume()
        VibrateAppState.setAppRunningState(true)
    }

    override fun onPause() {
        VibrateAppState.setAppRunningState(false)
        super.onPause()
    }

    override fun onDestroy() {
        Wearable.getMessageClient(this).removeListener(this)
        VibrateAppState.setAppRunningState(false)
        super.onDestroy()
    }

    /* ---------- 포그라운드 직접 수신 ---------- */
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != "/vibrate") return

        val p = VibrationPayload.fromJsonData(event.data) ?: return
        Log.d("WearApp", "포그라운드 수신: $p")

        val color = pickColor(p.message)

        VibrationHelper.vibrate(this, p.duration, p.amplitude)
        Toast.makeText(this, p.message, Toast.LENGTH_SHORT).show()

        VibrationUiBus.notify(p.message, color, p.duration)
    }

    /* ---------- 헬퍼 ---------- */
    private fun resetAfterDelay() = lifecycleScope.launch {
        delay(3000L)
        show = false
        bg   = Color.Black
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    // VibrateListenerActivity 내부에 추가

    /** 메시지 → 배경 색상 매핑 */
    private fun pickColor(message: String): Int = when (message) {
        "사이렌", "경적" -> 0xFFFFFF00.toInt()        // 노란색
        "초록불입니다. 출발하세요", "초록불입니다. 출발하세요." -> 0xFF00FF00.toInt() // 초록색 (마침표 포함 예외도 포함)
        "차량 충돌이 발생했습니다." -> 0xFFFF0000.toInt() // 빨강
        else -> 0xFFFF0000.toInt() // 빨강 또는 기본 빨강
    }
}