package com.example.finalproject

import android.content.Context
import android.util.Log

import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WatchMessenger(private val context: Context) {
    /** 최초 연결된 워치(node)의 ID 1개만 캐싱 */
    private var watchNodeId: String? = null

    /** 비동기로 워치 노드 검색 & 캐싱 */
    suspend fun init() = withContext(Dispatchers.IO) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        watchNodeId = nodes.firstOrNull()?.id
        watchNodeId?.let { Log.d("WatchMessenger","워치 노드 캐싱 완료: $it") }
    }

    /** 진동 + 텍스트 알림을 워치로 전송 */
    fun sendVibration(message: String, amplitude: Int, duration: Long) {
        val nodeId = watchNodeId ?: run {
            Log.w("WatchMessenger","워치 미연결 – 전송 취소")
            return
        }

        val payload = JSONObject().apply {
            put("amplitude", amplitude)
            put("duration",  duration)
            put("message",   message)
        }.toString().toByteArray()

        Wearable.getMessageClient(context)
            .sendMessage(nodeId, "/vibrate", payload)
            .addOnSuccessListener { Log.d("WatchMessenger","알림 전송 성공") }
            .addOnFailureListener { Log.e("WatchMessenger","알림 전송 실패", it) }
    }
}