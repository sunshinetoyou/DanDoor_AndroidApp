package com.dandoor.ddlib

import android.app.Activity
import android.content.Context

/**
 * 실제 사용될 클래스
 * 수행 범위 : [권한 획득], [차량 제어], [비콘 신호 수신]
 * 적용 패턴 : 파사드 패턴
 */
class DandoorBTManager(private val context: Context) {

    private val permissionManager = DandoorBTPermission(context)
    private val vehicleManager = DandoorBTVehicle(context)

    /** For Permission */
    fun checkBTPermission(activity: Activity, callback: (Boolean) -> Unit) {
        if (permissionManager.hasRequiredPermissions()) {
            callback(true)
        } else {
            permissionManager.requestBluetoothPermissions(activity)
        }
    }

    /** For Vehicle */
    fun setVehicleCallback(callback: DandoorBTVehicle.VehicleConnectionCallback) {
        vehicleManager.vehicleConnectionCallback = callback
    }
    fun connectToCar() {
        vehicleManager.connectToCar()
    }
    fun disconnectFromCar() {
        vehicleManager.disconnectFromCar()
    }
    fun sendCarCommand(command: String) {
        vehicleManager.sendCarCommand(command)
    }

    // 권한 설정 결과
    fun onRequestPermissionResult(requestCode: Int, grantResult: IntArray, callback: (Boolean) -> Unit) {
        permissionManager.onRequestPermissionsResult(requestCode, grantResult, callback)
    }
}