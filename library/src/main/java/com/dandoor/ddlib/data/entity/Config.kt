package com.dandoor.ddlib.data.entity

import com.dandoor.ddlib.repository.DataManager

data class config(
    val speed: Double = DataManager.defualtConfig.speed,
    val pathLength: Double = DataManager.defualtConfig.pathLength,
    val windowSize: Long = DataManager.defualtConfig.windowSize,
    val startPos: Position = Position(0.0, 0.0),
    val startTime: Long = 0L
)
