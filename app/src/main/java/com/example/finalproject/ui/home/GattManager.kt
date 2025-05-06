package com.example.finalproject.ui.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.core.content.edit
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class GattManager(private val app: Application) {

    // 연결 상태 데이터
    private val _state = MutableLiveData(ConnState.NONE)
    val state: LiveData<ConnState> = _state
    // 현재 연결된 GATT 객체
    private var gatt: BluetoothGatt? = null
    // 마지막 연결된 디바이스
    private var lastDevice: BluetoothDevice? = null
    // 권한 체크
    private fun hasConnPerm() = PermissionUtils.hasBluetoothConnectPermission(app)
    // GATT 콜백 정의
    private val gattCb = object : BluetoothGattCallback() {
        // 연결 상태 변경 콜백
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _state.postValue(ConnState.FAILED)
                g.close()
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _state.postValue(ConnState.CONNECTED)
                g.discoverServices()
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _state.postValue(ConnState.FAILED)
                g.close()
                retryConnect(g.device)
            }
        }
        // 서비스 검색 완료 콜백
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            Log.d("GattManager", "GATT services discovered")
        }
    }
    // GATT 연결 시작
    @SuppressLint("MissingPermission")
    fun connectGatt(dev: BluetoothDevice) {
        if (!hasConnPerm()) {
            _state.postValue(ConnState.FAILED)
            return
        }
        lastDevice = dev
        saveLastDeviceAddress(dev.address)

        if (dev.bondState == BluetoothDevice.BOND_NONE) {
            _state.postValue(ConnState.BONDING)
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            app.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(c: Context?, i: Intent?) {
                    val d: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        i?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        i?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    d?.let {
                        if (it.address != dev.address) return
                        when (it.bondState) {
                            BluetoothDevice.BOND_BONDED -> {
                                app.unregisterReceiver(this)
                                _state.postValue(ConnState.BONDED)
                                reallyConnectGatt(dev)
                            }
                            BluetoothDevice.BOND_NONE -> {
                                _state.postValue(ConnState.FAILED)
                                app.unregisterReceiver(this)
                            }
                        }
                    }
                }
            }, filter)
            dev.createBond()
        } else {
            _state.postValue(ConnState.BONDED)
            reallyConnectGatt(dev)
        }
    }

    // 실제 GATT 접속
    @SuppressLint("MissingPermission")
    private fun reallyConnectGatt(dev: BluetoothDevice) {
        _state.postValue(ConnState.CONNECTING)
        gatt = dev.connectGatt(app, false, gattCb)
    }

    // GATT 연결 해제
    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        if (!hasConnPerm()) return
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _state.postValue(ConnState.NONE)
    }
    // 연결 끊겼을 시 재시도
    private fun retryConnect(device: BluetoothDevice) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (hasConnPerm()) {
                try {
                    _state.postValue(ConnState.CONNECTING)
                    gatt = device.connectGatt(app, false, gattCb)
                } catch (e: SecurityException) {
                    _state.postValue(ConnState.FAILED)
                }
            }
        }, 3000)
    }
    // 마지막 디바이스에 재연결 시도
    fun reconnectLastDeviceIfPossible() {
        val prefs = app.getSharedPreferences("BEL_PREFS", Context.MODE_PRIVATE)
        val mac = prefs.getString("LAST_DEVICE_ADDRESS", null)

        if (mac.isNullOrBlank()) {
            Log.w("GattManager", "저장된 주소없음")
            return
        }

        val manager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter

        if (!PermissionUtils.hasBluetoothConnectPermission(app)) {
            Log.w("GattManager", "다시 연결 권한이 없음")
            return
        }
        val device = adapter.getRemoteDevice(mac)

        try {
            Log.d("GattManager", "Reconnecting to device: ${device.address}, name: ${device.name ?: "이름 없음"}")
        } catch (e: SecurityException) {
            Log.d("GattManager", "Reconnecting to device: ${device.address}, name: 권한 없음 (SecurityException)")
        }
        connectGatt(device)
    }
    // 주소 저장
    private fun saveLastDeviceAddress(address: String) {
        val prefs = app.getSharedPreferences("BLE_PREFS", Context.MODE_PRIVATE)
        prefs.edit {
            putString("LAST_DEVICE_ADDRESS", address)
        }
    }
}
