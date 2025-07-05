package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "esti_data",
    foreignKeys = [ForeignKey(
        entity = Lab::class,
        parentColumns = ["labID"],
        childColumns = ["lid"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lid")]
)
data class EstiData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val esti_pos: Position,
    val real_pos: Position,
    val error: Double,
    val lid: Long,
    val method: String
)

data class EstimationSummary(
    val timestamp: Long,
    val estiPos: Position,
    val realPos: Position,
    val error: Double,
    val method: String
)