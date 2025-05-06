package com.example.wearosapp.presentation

object VibrateAppState {
    var isAppRunning: Boolean = false
        private set

    fun setAppRunningState(running: Boolean) {
        isAppRunning = running
    }
}