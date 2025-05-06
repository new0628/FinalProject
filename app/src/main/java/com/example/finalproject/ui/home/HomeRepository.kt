// HomeRepository.kt
package com.example.finalproject.ui.home

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData


class HomeRepository(app: Application) {

    // BLE 관련 매니저 (검색, 권한 체크 등 담당) 인스턴스
    private val bleScanner = BleScanner(app)

    // GATT 연결 담당 매니저 인스턴스
    private val gattManager = GattManager(app)

    // 스캔된 BLE 기기 목록 LiveData
    val devices: LiveData<List<BluetoothDevice>> get() = bleScanner.devices

    // GATT 연결 상태 LiveData
    val state = gattManager.state

    // BLE 스캔 시작 요청
    fun startBleScan(): Boolean = bleScanner.startBleScan()

    // BLE 스캔 종료 요청
    fun stopBleScan() = bleScanner.stopBleScan()

    // GATT 연결 요청
    fun connectGatt(dev: BluetoothDevice) = gattManager.connectGatt(dev)

    // GATT 연결 해제 요청
    fun disconnectGatt() = gattManager.disconnectGatt()
    // 마지막 연결된 기기 재연결 시도
    fun reconnectLastDevice() = gattManager.reconnectLastDeviceIfPossible()
}
