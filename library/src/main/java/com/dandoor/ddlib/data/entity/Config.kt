package com.dandoor.ddlib.data.entity

import com.dandoor.ddlib.repository.DataManager

data class config(
    val speed: Double = DataManager.defaultCarSpeed,
    val pathLength: Double = DataManager.defaultPathLength,
    val windowStart: Long = DataManager.windowSize,
    val startPos: Position = Position(0.0, 0.0),
    val startTime: Long = 0L
)
