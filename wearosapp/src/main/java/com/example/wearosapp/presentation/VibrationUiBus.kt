package com.example.wearosapp.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object VibrationUiBus {
    data class UiEvent(
        val message: String,
        val color:  Int,
        val duration: Long       // 화면을 몇 ms 동안 보여줄지
    )

    private val _events = MutableSharedFlow<UiEvent>(
        replay              = 1,                    // 최근 1개 기억
        extraBufferCapacity = 1,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    /** 발행 : tryEmit → 절대 블로킹되지 않음 */
    fun notify(message: String, color: Int, duration: Long) {
        _events.tryEmit(UiEvent(message, color, duration))
    }
}