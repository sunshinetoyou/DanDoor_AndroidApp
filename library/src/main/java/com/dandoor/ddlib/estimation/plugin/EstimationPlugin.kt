package com.dandoor.ddlib.estimation.plugin

import com.dandoor.ddlib.data.entity.EstimationSummary
import com.dandoor.ddlib.data.entity.Position
import com.dandoor.ddlib.estimation.model.TimeWindowBeaconRssi
import com.dandoor.ddlib.data.entity.config

interface EstimationPlugin {
    val name: String
    /** 각각의 평가 클래스마다 구현할 항목 */
    fun calcEstiPos(input: TimeWindowBeaconRssi): Position

    /** 모든 평가 클래스가 내장하고 있는 함수 */
    fun calcRealPos(input: config): Position {
        val t = (input.windowSize - input.startTime) / 1000.0 // 초 단위
        val totalLength = 4 * input.pathLength
        val d = (input.speed * t) % totalLength

        return when {
            d < input.pathLength -> Position(input.startPos.x + d, input.startPos.y)
            d < 2 * input.pathLength -> Position(input.startPos.x + input.pathLength, input.startPos.y + (d - input.pathLength))
            d < 3 * input.pathLength -> Position(input.startPos.x + input.pathLength - (d - 2 * input.pathLength), input.startPos.y + input.pathLength)
            else -> Position(input.startPos.x, input.startPos.y + input.pathLength - (d - 3 * input.pathLength))
        }
    }
    fun calcError(p1: Position, p2: Position): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return Math.sqrt(dx * dx + dy * dy)
    }

    /** 실험 번호로 쿼리된 모든 평가할 Beacon RSSI 값을 한번에 처리할 수 있게 하는 함수 */
    fun calc(input: List<TimeWindowBeaconRssi>, config: config): List<EstimationSummary> {
        return input.map { window ->
            val estiPos = calcEstiPos(window)
            val realPos = calcRealPos(config.copy(windowSize = window.windowStart))
            val error = calcError(estiPos, realPos)
            EstimationSummary(
                timestamp = window.windowStart,
                estiPos = estiPos,
                realPos = realPos,
                error = error,
                method = name
            )
        }
    }
}
