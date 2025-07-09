package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** LAB
 *
 *    |-------------|-------------------|---------------|-----------|
 *    |    labID    |  beaconPositions  |   createdAt   |   alias   |
 *    |_____________|___________________|_______________|___________|
 *        (Long)      (BeaconPosition)       (Long)        (String)
 *
 *    PK(Primary Key): labId
 *
 *    BeaconPosition은 x, y, z 좌표로 구성되어 각 비콘의 위치를 의미한다.
 *    가변적으로 비콘의 개수를 입력받기는 하지만, 디폴트로 4개의 비콘의 위치를 입력하게 고정함.
 *
 *    createdAt은 실험이 시작된 시간을 의미하며, 이를 통해 real_pos를 추출할 수 있을 것으로 예상.
 *
 *    alias는 실험에 대한 별명으로 추후 update 기능을 통해 lab의 명칭을 변경할 수 있도록 만들 예정
 *    디폴트 값으로는 "Unknown"이 들어가 있다.
 */
@Entity(tableName = "lab")
data class Lab(
    @PrimaryKey(autoGenerate = true) val labID: Long = 0,
    val beaconPositions: List<Position>,
    val createdAt: Long,
    val alias: String
)