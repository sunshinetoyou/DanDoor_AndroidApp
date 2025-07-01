package com.dandoor.ddlib

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/**
 * 블루투스 권한을 처리하는 클래스
 */
class DandoorBTPermission(private val context: Context) {

    val REQUEST_BLUETOOTH_PERMISSIONS = 1001

    /** 필요한 권한 목록 */
    private fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    }
    /** 권한이 모두 허용되었는지 확인 */
    fun hasRequiredPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    /** 권한 요청 시작 */
    fun requestBluetoothPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            getRequiredPermissions(),
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }
    /** 권한 요청 결과 처리 */
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        callback: (Boolean) -> Unit
    ) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            callback(allGranted)
        }
    }
}