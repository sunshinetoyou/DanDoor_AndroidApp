package com.dandoor.ddlib.estimation.plugin

import com.dandoor.ddlib.data.entity.EstimationSummary
import com.dandoor.ddlib.data.entity.Position
import com.dandoor.ddlib.estimation.model.TimeWindowBeaconRssi
import com.dandoor.ddlib.data.entity.config
import com.dandoor.ddlib.repository.DataManager

interface EstimationPlugin {
    val name: String

    fun calcEstiPos(input: TimeWindowBeaconRssi): Position
    fun calcRealPos(input: config): Position {
        val t = (input.windowStart - input.startTime) / 1000.0 // 초 단위
        val totalLength = 4 * input.pathLength
        val d = (input.speed * t) % totalLength

        return when {
            d < input.pathLength -> Position(input.startPos.x + d, input.startPos.y)
            d < 2 * input.pathLength -> Position(input.startPos.x + input.pathLength, input.startPos.y + (d - input.pathLength))
            d < 3 * input.pathLength -> Position(input.startPos.x + input.pathLength - (d - 2 * input.pathLength), input.startPos.y + input.pathLength)
            else -> Position(input.startPos.x, input.startPos.y + input.pathLength - (d - 3 * input.pathLength))
        }
    }

    /** 유클리드 거리 계산 (2D) */
    fun calcError(p1: Position, p2: Position): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return Math.sqrt(dx * dx + dy * dy)
    }

    fun calc(input: List<TimeWindowBeaconRssi>, config: config): List<EstimationSummary> {
        return input.map { window ->
            val estiPos = calcEstiPos(window)
            val realPos = calcRealPos(config.copy(windowStart = window.windowStart))
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
