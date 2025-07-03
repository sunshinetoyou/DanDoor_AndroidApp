package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_data",
    foreignKeys = [ForeignKey(
        entity = Lab::class,
        parentColumns = ["labID"],
        childColumns = ["lid"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lid")]
)
data class ScanData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val beaconID: String,
    val rssi: Int,
    val lid: Long // Lab의 labID를 참조하는 외래키
)