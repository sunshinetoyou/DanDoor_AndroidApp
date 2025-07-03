package com.dandoor.ddlib

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_data")
data class ScanData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val deviceName: String,
    val rssi: Int,
    val extraData: String? = null
)
