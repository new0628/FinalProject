package com.example.finalproject.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.concurrent.thread
// 블루투스 소켓 관련
class BlueToothSocketManager (private val context: android.content.Context) {

    companion object {
        // RFCOMM 통신용 고정 UUID
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
    // 블루투스 소켓
    private var socket: BluetoothSocket? = null
    // 루빅파이에서 받을 데이터
    private var inStream: InputStream? = null
    // 루빅파이에게 보낼 데이터
    private var outStream: OutputStream? = null
    // 소켓 연결 시도
    @SuppressLint("MissingPermission")
    fun connect (
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        // 연결 권한 없으면 실패 처리
        if (!PermissionUtils.hasBluetoothConnectPermission(context)) {
            onError(SecurityException("BLUETOOTH_CONNECT permission not granted"))
            return
        }

        thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket?.connect()

                inStream = socket?.inputStream
                outStream = socket?.outputStream

                Log.d("BTSocket", "블루투스 소켓 연결 성공")
                onSuccess()
            } catch (e: Exception) {
                Log.e("BtSocket", "연결 실패 : ${e.message}")
                onError(e)
            }
        }
    }
    // 전송(문자열로)
    fun send(data: String) {
        thread {
            try {
                outStream?.write(data.toByteArray())
                Log.d("BtSocket", "전송: $data")
            } catch (e: Exception) {
                Log.e("BtSocket", "전송 실패: ${e.message}")
            }
        }
    }
    // 수신 (문자열로
    fun receive (onDataReceived: (String) -> Unit) {
        thread {
            try {
                val buffer = ByteArray(1024)
                while (true) {
                    val bytes = inStream?.read(buffer) ?: break
                    val message = String(buffer, 0, bytes)
                    Log.d("BtSocket", "받음: $message")
                    onDataReceived(message)
                }
            } catch (e: Exception) {
                Log.e("BtSocket", "받기 실패 : ${e.message}")
            }
        }
    }
    //소켓 연결 종료
    fun disconnect() {
        try {
            socket?.close()
            socket = null
            inStream = null
            outStream = null
            Log.d("BtSocket", "소켓 연결 해제")
        } catch (e: Exception) {
            Log.e("BtSocket", "해제 실패 e: ${e.message}")
        }
    }
}