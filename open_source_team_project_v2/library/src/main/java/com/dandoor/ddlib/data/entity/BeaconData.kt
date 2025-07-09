package com.dandoor.ddlib.data.entity

/** BEACON_DATA
 *
 *  실제로 비콘을 스캔한 뒤 가지고 있어야 하는 데이터 모음
 *
 *  beacon_name / beacon_rssi 는 onScanResult 콜백을 통해 획득
 *  timestamp 는 System 시간을 읽음
 */
data class BeaconData(
    val beacon_name: String,
    val beacon_rssi: Int,
    val timestamp: Long
)
