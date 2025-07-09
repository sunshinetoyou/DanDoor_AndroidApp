package com.dandoor.ddlib.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.util.Log
import com.dandoor.ddlib.repository.DataManager
import java.io.IOException

/** BTManger
 * 기능 범위 : [권한 획득], [차량 제어], [비콘 신호 수신]
 *
 * [권한 획득]
 * F?. 핸드폰 블루투스 권한 확인       : checkPermission(activity, callback)
 *
 * [차량 제어]
 * F?. 차량 콜백 함수 등록            : setVehicleCallback(callback)
 * F?. 차량 블루투스 연결 ( 페어링 )   : connectToCar()
 * F?. 차량 블루투스 연결 해제         : disconnectedFromCar()                            (x)
 * F2~3. 차량 명령어 전송             : sendCarCommand(string)
 *      command: string { start, stop, toggle }
 *
 * [비콘 신호 수신]
 * F1. 비콘_데이터 스캔 시작            : startScan()
 * F?. 비콘 데이터 스캔 정지            : stopScan()
 * F?. 주기적인 비콘_데이터 스캔 시작     : startPeriodicScan()                             (x)
 */
// 아두이노 정보 수신을 위한 추상화
interface ReceiveCallback {
    fun onReceive(message: String)
}

class BTManager(
    private val context: Context,
    dtManager: DataManager
) {

    val btAdapter: BluetoothAdapter by lazy {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter
    }
    private val permissionManager = BTPermission(context)
    private val vehicleManager = BTVehicle(context, btAdapter)
    private val beaconManager = BTBeacon(context, btAdapter, dtManager)

    /** For Permission */
    fun checkBTPermission(activity: Activity) {
        if (!permissionManager.hasRequiredPermissions()) {
            permissionManager.requestBluetoothPermissions(activity)
        }
    }

    /** For Vehicle */
    fun setVehicleCallback(callback: BTVehicle.VehicleConnectionCallback) {
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
    fun onRequestPermissionResult(
        requestCode: Int,
        grantResult: IntArray,
        callback: (Boolean) -> Unit
    ) {
        permissionManager.onRequestPermissionsResult(requestCode, grantResult, callback)
    }

    //아두이노가 메시지 수신시 어떻게 행동할지 결정

    private var receiveCallback: ReceiveCallback? = null

    fun setReceiveCallback(callback: ReceiveCallback) {
        this.receiveCallback = callback
    }

    // 아두이노 메시지 수신처리
    private fun startReceiving() {
        Thread {
            try {
                val socket = vehicleManager.getSocket()  // 여기서 받아옴
                val inputStream = socket?.inputStream ?: return@Thread
                val buffer = ByteArray(1024)

                while (true) {
                    val bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    receiveCallback?.onReceive(message)
                }

            } catch (e: IOException) {
                Log.e("BTManager", "수신 실패", e)
            }
        }.start()
    }

}