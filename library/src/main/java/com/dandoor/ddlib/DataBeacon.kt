package com.dandoor.ddlib

data class DataBeacon(
    val beacon_name: String,
    val beacon_rssi: Int,
    val timestamp: Long
)
