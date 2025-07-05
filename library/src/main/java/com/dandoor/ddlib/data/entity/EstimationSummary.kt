package com.dandoor.ddlib.data.entity

data class EstimationSummary(
    val timestamp: Long,
    val estiPos: Position,
    val realPos: Position,
    val error: Double,
    val method: String
)
