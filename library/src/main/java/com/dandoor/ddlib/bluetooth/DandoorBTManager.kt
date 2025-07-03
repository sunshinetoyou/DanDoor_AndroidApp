package com.dandoor.ddlib.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import com.dandoor.ddlib.repository.DataManager

/**
 * 실제 사용자가 사용하는 클래스
 * 수행 범위 : [권한 획득], [차량 제어], [비콘 신호 수신]
 *
 * 목적: 하위 구현 요소들을 묶어서 사용자가 편하게 사용하게 하기 위함.
 *
 * (+) checkBTPermission(): BT 권한 확인 및 획득
 * (+) ...(추후에 정리)
 *
 */
class DandoorBTManager(
    private val context: Context,
    dtManager: DataManager
) {

    val btAdapter: BluetoothAdapter by lazy {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter
    }

    private val permissionManager = DandoorBTPermission(context)
    private val vehicleManager = DandoorBTVehicle(context, btAdapter)
    private val beaconManager = DandoorBTBeacon(context, btAdapter, dtManager)

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

    /** For Beacon */
    fun startPeriodicScan(handler: Handler) {
        val scanRunnable = object : Runnable {
            override fun run() {
                beaconManager.stopScan() // 혹시 이전 스캔이 남아있으면 중지
                beaconManager.startScan()
                handler.postDelayed(this, 10_000L)
            }
        }
        handler.post(scanRunnable)
    }
    fun startScan() {
        beaconManager.startScan()
    }
    fun stopScan() {
        beaconManager.stopScan()
    }

    // 권한 설정 결과
    fun onRequestPermissionResult(requestCode: Int, grantResult: IntArray, callback: (Boolean) -> Unit) {
        permissionManager.onRequestPermissionsResult(requestCode, grantResult, callback)
    }
}