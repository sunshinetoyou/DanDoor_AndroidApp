package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab")
data class Lab(
    @PrimaryKey(autoGenerate = true) val labID: Long = 0,
    val beaconPositions: List<BeaconPosition>,
    val createdAt: Long,
    val alias: String
)

data class BeaconPosition(
    val x: Double,
    val y: Double,
    val z: Double? = null
)