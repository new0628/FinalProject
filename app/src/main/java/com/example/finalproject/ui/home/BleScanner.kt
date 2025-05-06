
// BleManager.kt
package com.example.finalproject.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings

import android.util.Log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BleScanner(private val app: Application) {
    // 블루투스 매니저 시스템 서비스
    private val btManager = app.getSystemService(Application.BLUETOOTH_SERVICE) as BluetoothManager
    // 블루투스 어댑터 객체
    private val adapter: BluetoothAdapter? = btManager.adapter
    // 블루투스 스캐너 가져오기
    private val scanner get() = adapter?.bluetoothLeScanner
    // 스캔된 BLE 목록
    private val _devices = MutableLiveData<List<BluetoothDevice>>(emptyList())
    val devices: LiveData<List<BluetoothDevice>> = _devices
    // 스캔된 캐시 목록
    private val cache = mutableListOf<BluetoothDevice>()

    // 권한 체크 (스캔, 연결)
    private fun hasScanPerm() = PermissionUtils.hasBluetoothScanPermission(app)
    private fun hasConnPerm() = PermissionUtils.hasBluetoothConnectPermission(app)

    // BLE 디바이스 추가 처리
    private fun addDevice(dev: BluetoothDevice) {

        try {
            if (dev.name.isNullOrBlank()) return
            if (cache.none { it.address == dev.address }) {
                cache += dev
                _devices.postValue(cache.toList())
            }
        } catch (e: SecurityException) {
            // 권한 거부된 경우
            return
        }

    }
    // 스캔 콜백 정의
    private val scanCb = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.device?.let { addDevice(it) }
        }
    }

    // BLE 스캔 시작
    @SuppressLint("MissingPermission")
    fun startBleScan(): Boolean {
        if (scanner == null) return false
        if (!hasScanPerm() || !hasConnPerm()) return false

        cache.clear()
        _devices.postValue(emptyList())

        val filters = emptyList<ScanFilter>()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        scanner!!.startScan(filters, settings, scanCb)
        Log.d("BleScanner", "BLE scan started (no filter)")
        return true
    }

    // BLE 스캔 중지
    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        val scanner = adapter?.bluetoothLeScanner ?: return
        if (!hasScanPerm()) return
        scanner.stopScan(scanCb)
        Log.d("BleScanner", "BLE scan stopped")
    }
}