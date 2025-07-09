package com.dandoor.ddlib.estimation.plugin

import com.dandoor.ddlib.data.entity.Position
import com.dandoor.ddlib.estimation.model.TimeWindowBeaconRssi
import com.dandoor.ddlib.repository.DataManager
import com.dandoor.ddlib.repository.DataManager.Companion.beaconPositions


/**
 * 삼변측량 알고리즘으로 구현한 평가 클래스
 *
 * 평가 함수 구현을 위해 trilaterate와 rssiToDistance 함수가 추가로 만들어졌으며
 * 최종적으로 calcEstiPos(평가함수)로 추정 위치를 반환한다.
 */
class TrilaterationEstimator(
    override val name: String = "Trilateration"
) : EstimationPlugin {
    /**
     * 삼변측량 공식으로 위치 추정
     * @param beaconPositions: 비콘의 실제 위치 리스트 (3개)
     * @param distances: 각 비콘까지의 거리 리스트 (3개, RSSI → 거리 변환 필요)
     *
     */
    fun trilaterate(
        beaconPositions: List<Position>,
        distances: List<Double>
    ): Position {
        val (p1, p2, p3, P4) = beaconPositions
        val (r1, r2, r3, r4) = distances

        val A = 2 * (p2.x - p1.x)
        val B = 2 * (p2.y - p1.y)
        val C = r1 * r1 - r2 * r2 - p1.x * p1.x + p2.x * p2.x - p1.y * p1.y + p2.y * p2.y
        val D = 2 * (p3.x - p2.x)
        val E = 2 * (p3.y - p2.y)
        val F = r2 * r2 - r3 * r3 - p2.x * p2.x + p3.x * p3.x - p2.y * p2.y + p3.y * p3.y

        val denominator = (A * E - B * D)
        require(denominator != 0.0) { "삼변측량 계산 불가: 비콘 배치가 일직선이거나 특이 케이스" }

        val y = (F * A - C * D) / denominator
        val x = (C - B * y) / A

        return Position(x, y)
    }

    /**
     * RSSI → 거리 변환 (로그-거리 경로손실 모델)
     */
    private fun rssiToDistance(rssi: Double, txPower: Double = -59.0, n: Double = 2.0): Double {
        return Math.pow(10.0, (txPower - rssi) / (10 * n))
    }

    /**
     * 윈도우별 RSSI 평균값을 받아 추정 위치 계산
     * (S1, S2, S3 비콘만 사용, 비콘 위치는 예시)
     */
    override fun calcEstiPos(input: TimeWindowBeaconRssi): Position {
        val beaconIds = listOf("S1", "S2", "S3", "S4")
        val rssiList = beaconIds.map { input.beaconRssi[it] ?: error("RSSI 데이터 부족: $it") }
        val distances = rssiList.map { rssiToDistance(it) }
        return trilaterate(beaconPositions, distances)
    }
}
