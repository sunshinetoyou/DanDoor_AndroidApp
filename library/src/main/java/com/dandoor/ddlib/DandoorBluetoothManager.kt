package com.dandoor.ddlib

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.util.UUID

/**
 * 블루투스 통신을 관리하는 클래스
 *
 * 차량과 통신
 *  # 차량의 시동 제어
 *      - Start
 *      - Stop
 *      - Toggle
 *
 * 비콘과 통신
 *  # 비콘의 광고 데이터 수신
 *
 * */
class DandoorBluetoothManager(private val context: Context) {

    /**
     * 블루투스 이벤트를 처리하기 위한 콜백 인터페이스
     * 비동기 블루투스 작업의 결과를 UI 또는 다른 컴포넌트로 전달
     */
    interface BluetoothCallback {
        // FOR CAR
        fun onConnectionStatusChanged(isConnected: Boolean, deviceName: String?)
        fun onConnectionError(error: String)
        fun onCommandSent(command: String)
        fun onCommandError(error: String)

        //TODO FOR BEACON
    }

    companion object {
        /**
         * SPP(Serial Port Profile) UUID - 블루투스 직렬 통신용 표준 UUID (고정)
         * HC-06 같은 아두이노 기반 블루투스 모듈이 많이 사용함.
         * proto 개발 중에는 고정값으로 사용 예정
         */
        private val BT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TARGET_DEVICE_NAME = "HC-06"
    }

    /** 블루투스 이벤트를 받을 콜백 인터페이스 구현체 */
    private var callback: BluetoothCallback? = null
    /** 안드로이드 블루투스 어댑터 - 블루투스 하드웨어 제어 */
    private var btAdapter: BluetoothAdapter? = null
    /** 블루투스 소켓 - 실제 통신 연결을 담당 */
    private var btSocket: BluetoothSocket? = null
    /** 데이터 전송을 위한 출력 스트림 */
    private var outputStream: OutputStream? = null
    /** 현재 블루투스 연결 상태 플래그 */
    private var isConnected = false

    /** 콜백 등록 함수 */
    fun setCallback(callback: BluetoothCallback) {
        this.callback = callback
    }

    /** 블루투스 어댑처 초기화 함수 */
    fun initialize() : Boolean {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            btAdapter = bluetoothManager.adapter
            btAdapter != null
        } catch (e: Exception) {
            callback?.onConnectionError("블루투스 초기화 실패: ${e.message}")
            false
        }
    }

    /** 차량 블루투스 기기(HC-06)와 연결 시도 (※ 연결 이전에 수동 페어링을 한 상태여야 함) */
    fun connectToCar() {
        val systemBluetoothManger = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val adapter = btAdapter ?: systemBluetoothManger.adapter
        if (adapter == null) {
            callback?.onConnectionError("블루투스를 지원하지 않는 기기입니다.")
            return
        }
        if (!adapter.isEnabled) {
            callback?.onConnectionError("블루투스가 꺼져 있습니다.")
            return
        }

        try {
            val pairedDevices = adapter.bondedDevices
            val device = pairedDevices.firstOrNull { it.name == TARGET_DEVICE_NAME }

            if (device == null) {
                callback?.onConnectionError("HC-06 장치를 찾을 수 없습니다.")
                return
            }

            btSocket = device.createRfcommSocketToServiceRecord(BT_UUID)
            btSocket?.connect()
            outputStream = btSocket?.outputStream
            isConnected = true

            callback?.onConnectionStatusChanged(true, device.name)

        } catch (e: SecurityException) {
            callback?.onConnectionError("권한 오류: ${e.message}")
            disconnectFromCar()
        } catch (e: Exception) {
            callback?.onConnectionError("연결 실패: ${e.message}")
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
            callback?.onConnectionStatusChanged(false, null)
        }
    }

    /** 차량에 명령어 전송 */
    fun sendCarCommand(command: String) {
        if (!isConnected || outputStream == null) {
            callback?.onConnectionError("블루투스 연결이 필요합니다.")
            return
        }
        try {
            outputStream?.write((command + "c").toByteArray()) // 아두이노에서 받을 때, "c"를 기준으로 입력을 받음
            callback?.onCommandSent(command)
        } catch (e: Exception) {
            callback?.onCommandError("명령 전송 실패: ${e.message}")
        }
    }

    /** 현재 블루투스 연결 상태 반환 */
    fun isCarConnected(): Boolean = isConnected
}