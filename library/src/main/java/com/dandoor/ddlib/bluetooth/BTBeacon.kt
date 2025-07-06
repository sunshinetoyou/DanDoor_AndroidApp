package com.dandoor.ddlib.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.dandoor.ddlib.data.entity.BeaconData
import com.dandoor.ddlib.repository.DataManager

/** BTBeacon
 *
 *  비콘에 대해 스캐닝 실시 -> DataManager
 *
 *  onScanResult 콜백 함수를 통해 데이터 전달
 *
 *  (+) startScan(): 스캐닝 시작
 *  (+) stopScan():  스캐닝 정지
 */
class BTBeacon(
    private val context: Context,
    btAdapter: BluetoothAdapter,
    private val dtManager: DataManager  /** 비콘 스캔 행위를 dtManger와 바로 연결하기 위해 의존성 주입 */
) {
    private val btScanner: BluetoothLeScanner? = btAdapter.bluetoothLeScanner
    private var scanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                val beaconData = BeaconData(
                    result.device.name ?: result.scanRecord?.deviceName ?: "Unkown",
                    result.rssi,
                    System.currentTimeMillis()
                )
                dtManager.saveScanDataSync(beaconData)
                Log.d("BeaconScan", "비콘 발견: ${result.device.name}, RSSI: ${result.rssi}, bytes: ${result.scanRecord}")
            } catch (_: SecurityException) {
                /** device.name 빨간 줄 방지용 */
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("BeaconScan", "스캔 실패: $errorCode")
            scanning = false
        }
    }

    fun startScan() {
        if (scanning) return
        scanning = true
        Log.d("BeaconScan", "스캔 시작")

        // Android 12+ 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BLE", "BLUETOOTH_SCAN 권한 없음")
                return
            }
        }
        // Android 11- 권한 체크
        else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BLE", "ACCESS_FINE_LOCATION 권한 없음")
                return
            }
        }

        // 1. ScanSettings 설정 (최적화된 설정)
        val settings = ScanSettings.Builder().apply {
//            setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // 최대 속도 모드
//            setReportDelay(0) // 즉시 결과 보고
//            setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE) // 민감도 최대
        }.build()

        // 2. ScanFilter 설정 (nRF 필터)
        val nrfFilter = ScanFilter.Builder().apply {
            setManufacturerData(
                89, // Nordic Semiconductor
                byteArrayOf(2, 21, 1, 18, 35), // 데이터 패턴
                byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()) // 마스크 (앞 2바이트만 비교)
            )
        }.build()
        val filters = listOf(nrfFilter)

        btScanner?.startScan(filters, settings, scanCallback)
    }
    fun stopScan() {
        if (!scanning) return

        // Android 12+ 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BLE", "BLUETOOTH_SCAN 권한 없음")
                return
            }
        }
        // Android 11- 권한 체크
        else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("BLE", "ACCESS_FINE_LOCATION 권한 없음")
                return
            }
        }

        btScanner?.stopScan(scanCallback)
        scanning = false
//        dtManager.cancelCoroutineScope()
    }
}