package com.dandoor.ddlib.estimation.model

/** TimeWindowBeaconRssi
 *
 * 각각의 비콘마다 일정 시간 내의 신호를 평균내어 재정리 한 값.
 *
 * 설명
 *      Component                       사용되는 데이터 형식
 *
 *         비콘
 *                      com.dandoor.ddlib.data.entity.BeaconData
 *          ↓           비콘_데이터(beacon_name: String, beacon_rssi: Int, timestamp: Long)
 *
 *      내부 저장소       com.dandoor.ddlib.data.entity.ScanData
 *     (scan_data)      스캔_데이터(id: Long, timestamp: Long, beaconID: String, rssi: Int, lid: Long)
 *
 *          ↓           특정 시간 간격(timeWindow)으로 구분 -> 각 센서끼리 평균(beaconRssi)  -> ex) [(s1, -60), (s2,-75), ...]
 *                      이때, timeWindow와 beaconRssi를 가지는 데이터가 timewindowBeaconRssi 데이터이다.
 *       평가 함수
 *
 *          ↳           평가_데이터
 *                      (idx: Long, esti_pos: Position, real_pos: Position, error: Double, lid)
 */
data class TimeWindowBeaconRssi(
    val windowStart: Long,                  // 슬라이싱된 초기 시간값
    val beaconRssi: Map<String, Double>     // [(s1, -60), (s2,-75), ...]
)
