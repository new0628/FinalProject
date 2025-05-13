package com.example.finalproject

import android.util.Log
import com.example.finalproject.ui.events.EventItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

import org.json.JSONObject

class MqttManager(
    private val onMessageReceived: (EventItem) -> Unit
) {
    private val client = MqttClient(
        MqttConfig.BROKER_URI,
        MqttConfig.CLIENT_ID,
        null
    )

    fun connectAndSubscribe() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = true
                    keepAliveInterval = 60
                }
                Log.d("MQTT", "Connected to broker at ${MqttConfig.BROKER_URI}")

                client.connect(options)

                client.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        Log.w("MQTT", "연결 끊김: ${cause?.message}")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        try {
                            if (message?.isRetained == true) return  //처음에 남아있던 retained 메시지 무시
                            Log.d("MQTT", "메시지 수신됨 - topic: $topic, message: ${message?.toString()}")
                            val json = JSONObject(message.toString())
                            val item = EventItem(
                                mode = json.getString("mode"),
                                title = json.getString("event"),
                                date = json.getString("timestamp"),
                                color = json.getString("color")
                            )
                            onMessageReceived(item)
                        } catch (e: Exception) {
                            Log.e("MQTT", "메시지 처리 오류: ${e.message}")
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
                client.subscribe(MqttConfig.TOPIC)
            } catch (e: Exception) {
                Log.e("MQTT", "Connection error: ${e.message}")
            }
        }
    }

    fun disconnect() {
        if (client.isConnected) {
            client.disconnect()
        }
    }
}