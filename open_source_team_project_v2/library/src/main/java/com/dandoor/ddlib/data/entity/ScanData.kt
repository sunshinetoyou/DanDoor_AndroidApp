package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** SCAN_DATA
 *
 *    |----------|-------------|--------------|----------|----------------|
 *    |    id    |  timestamp  |   beaconID   |   rssi   |   lid(labID)   | <- Lab's labID와 연결
 *    |__________|_____________|______________|__________|________________|
 *    
 *    PK(Primary Key): id
 *    FK(Foreign Key): lid <- (Lab's) labID
 *
 *    스캔하여 획득한 beacon_data를 저장하며, 고유 식별자(id)와 실험 식별자(lid)를 부여함.
 *
 *    실험 번호(labID)에 따라 실험 결과를 쿼리할 수 있게 만듦
 */
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