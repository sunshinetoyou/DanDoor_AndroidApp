package com.dandoor.ddlib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.OutputStream
import java.util.UUID

class DandoorBTVehicle(
    private val context: Context,
    private val btAdapter: BluetoothAdapter
) {

    /** 콜백 인터페이스 */
    interface VehicleConnectionCallback {
        fun onConnectionStatusChanged(connected: Boolean, deviceName: String?)
        fun onConnectionError(message: String)
        fun onCommandSent(command: String)
        fun onCommandError(message: String)
    }

    companion object {
        val BT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val TARGET_DEVICE_NAME = "HC-06"
    }

    private var btSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isConnected: Boolean = false

    var vehicleConnectionCallback: VehicleConnectionCallback? = null

    /** 차량 블루투스 기기(HC-06)와 연결 시도 (※ 연결 이전에 수동 페어링을 한 상태여야 함) */
    fun connectToCar() {
        val systemBluetoothManger = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val adapter = btAdapter ?: systemBluetoothManger.adapter
        if (adapter == null) {
            vehicleConnectionCallback?.onConnectionError("블루투스를 지원하지 않는 기기입니다.")
            return
        }
        if (!adapter.isEnabled) {
            vehicleConnectionCallback?.onConnectionError("블루투스가 꺼져 있습니다.")
            return
        }

        try {
            val pairedDevices = adapter.bondedDevices
            val device = pairedDevices.firstOrNull { it.name == TARGET_DEVICE_NAME }

            if (device == null) {
                vehicleConnectionCallback?.onConnectionError("HC-06 장치를 찾을 수 없습니다.")
                return
            }

            btSocket = device.createRfcommSocketToServiceRecord(BT_UUID)
            btSocket?.connect()
            outputStream = btSocket?.outputStream
            isConnected = true

            vehicleConnectionCallback?.onConnectionStatusChanged(true, device.name)

        } catch (e: SecurityException) {
            vehicleConnectionCallback?.onConnectionError("권한 오류: ${e.message}")
            disconnectFromCar()
        } catch (e: Exception) {
            vehicleConnectionCallback?.onConnectionError("연결 실패: ${e.message}")
            disconnectFromCar()
        }
    }

    /** 차량 블루투스 기기와 연결 해제 */
    fun disconnectFromCar() {
        try {
            outputStream?.close()
            btSocket?.close()
        } catch (e: Exception) {
            // 무시
        } finally {
            isConnected = false
            vehicleConnectionCallback?.onConnectionStatusChanged(false, null)
        }
    }

    /** 차량에 명령어 전송 */
    fun sendCarCommand(command: String) {
        if (!isConnected || outputStream == null) {
            vehicleConnectionCallback?.onConnectionError("블루투스 연결이 필요합니다.")
            return
        }
        try {
            outputStream?.write((command + "c").toByteArray()) // 아두이노에서 받을 때, "c"를 기준으로 입력을 받음
            vehicleConnectionCallback?.onCommandSent(command)
        } catch (e: Exception) {
            vehicleConnectionCallback?.onCommandError("명령 전송 실패: ${e.message}")
        }
    }
}