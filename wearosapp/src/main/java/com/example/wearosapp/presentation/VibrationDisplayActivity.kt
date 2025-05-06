package com.example.wearosapp.presentation


import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.view.WindowManager
import android.widget.TextView
import androidx.activity.ComponentActivity

import com.example.wearosapp.R

class VibrationDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vibration_display)
        //화면이꺼져있을때 메시지 띄울 수 있게
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val message = intent.getStringExtra("message") ?: "알림"
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.messageTextView).text = message
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000L)

    }

}