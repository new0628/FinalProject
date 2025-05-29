package com.example.finalproject.ui.home

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.Toast

// 블루투스 권한 확인
object PermissionUtils {
    fun hasBluetoothConnectPermission(context: Context) : Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

//    fun hasBluetoothScanPermission(context: Context) : Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.BLUETOOTH_SCAN
//        ) == PackageManager.PERMISSION_GRANTED
//    }

    fun checkAndWarnIfNoBluetoothPermission(context: Context): Boolean {
        val hasPermission = hasBluetoothConnectPermission(context)
        if (!hasPermission) {
            Toast.makeText(context, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
        return hasPermission
    }

    fun checkAndWarnIfNoPermissionForAction(context: Context, permission: String, message: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        return granted
    }
}