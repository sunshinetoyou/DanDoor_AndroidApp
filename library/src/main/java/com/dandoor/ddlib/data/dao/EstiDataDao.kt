package com.dandoor.ddlib.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dandoor.ddlib.data.entity.EstiData

@Dao
interface EstiDataDao {
    // 평가 결과 저장 (단일)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstiData(result: EstiData): Long

    // 평가 결과 여러 개 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<EstiData>): List<Long>

    // 특정 실험(labId) + 알고리즘별 결과 조회
    @Query("SELECT * FROM esti_data WHERE lid = :labId AND method = :method")
    suspend fun getResultsByLabAndAlgorithm(labId: Long, method: String): List<EstiData>

    // 특정 실험(labId) 전체 결과 조회
    @Query("SELECT * FROM esti_data WHERE lid = :labId")
    suspend fun getResultsByLab(labId: Long): List<EstiData>

    // 모든 평가 결과 조회
    @Query("SELECT * FROM esti_data")
    suspend fun getAllResults(): List<EstiData>
}