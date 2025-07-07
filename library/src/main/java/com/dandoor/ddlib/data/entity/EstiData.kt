package com.dandoor.ddlib.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/** ESTI_DATA
 *
 *    |----------|-------------|----------------|----------------|--------|---------|----------|
 *    |    id    |  timestamp  |    esti_pos    |    real_pos    |  error |   lid   |  method  |
 *    |__________|_____________|________________|________________|________|_________|__________|
 *
 *    PK(Primary Key): id
 *    FK(Foreign Key): lid <- (Lab's) labID
 *
 *    위치 추정 결과 데이터를 저장하는 테이블.
 *    - esti_pos: 추정된 위치
 *    - real_pos: 실제 위치
 *    - error: 추정 오차(유클리드 거리로 측정한 실제 위치와 추정 위치의 오차, 정확도를 나타내는 지표로 활용)
 *    - method: 위치 추정에 사용된 알고리즘 또는 방식(지금은 TrilaterationEstimator 하나 밖에 없음)
 *    - lid: 실험 식별자(Lab의 labID와 연결)
 *
 *    실험 번호(labID) 및 알고리즘(method)에 따라 결과를 쿼리할 수 있게 설계됨.
 */
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