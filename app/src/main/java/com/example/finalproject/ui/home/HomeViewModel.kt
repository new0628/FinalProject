// HomeViewModel.kt

package com.example.finalproject.ui.home

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// BLE / GATT / Socket 관리
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    // 소켓 통신 관리
    private val socketManager = BlueToothSocketManager(getApplication())
    // 소켓 수신 데이터
    private val _socketResponse = MutableLiveData<String>()
    val socketResponse: LiveData<String> = _socketResponse

    private val repo = HomeRepository(app)
    val devices = repo.devices // 스캔된 BLE 기기 리스트
    val connState = repo.state //연결 상태
    // BLE 스캔 시작
    fun startScan(onSuccess: ()->Unit, onFail: ()->Unit) {
        if (repo.startBleScan()) onSuccess() else onFail()
    }
    // BLE 스캔 중지
    fun stopScan()           = repo.stopBleScan()
    // GATT 연결
    fun connect(dev: BluetoothDevice)         = repo.connectGatt(dev)
    // GATT 연결 해제
    fun disconnect()          = repo.disconnectGatt()
    // 마지막 기기 재연결 시도
    fun reconnectLastDeviceIfPossible() = repo.reconnectLastDevice()
    // 루빅파이 연결
    fun connectToSocket (device: BluetoothDevice) {
        socketManager.connect (device,
            onSuccess = {
                Log.d("ViewModel", "소켓 연결 성공")
                socketManager.send("Hello from App")
                socketManager.receive { message ->
                    _socketResponse.postValue(message)
                }
            },
            onError = {
                Log.e("ViewModel", "소켓 연결 실패: ${it.message}")
            }
        )
    }
    // 소켓 연결 해제
    fun disconnectSocket() {
        socketManager.disconnect()
    }
}
