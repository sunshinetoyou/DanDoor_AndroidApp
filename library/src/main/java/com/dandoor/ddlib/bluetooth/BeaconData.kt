package com.dandoor.ddlib.bluetooth

data class BeaconData(
    val beacon_name: String,
    val beacon_rssi: Int,
    val timestamp: Long
)
