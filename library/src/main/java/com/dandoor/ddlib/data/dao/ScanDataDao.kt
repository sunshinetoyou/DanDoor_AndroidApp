package com.dandoor.ddlib.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dandoor.ddlib.data.entity.ScanData

/** SCAN_DATA_DAO
 * scan_data 테이블에 접근하기 위한 API를 정의함
 */
@Dao
interface ScanDataDao {
    @Insert
    suspend fun insert(scanData: ScanData): Long

    @Query("SELECT * FROM scan_data WHERE lid = :labID")
    suspend fun getScanData(labID: Long): List<ScanData>

    @Query("SELECT * FROM scan_data WHERE lid = :labID AND timestamp BETWEEN :stime AND :etime")
    suspend fun getScanDataInWindow(
        labID: Long,
        stime: Long,
        etime: Long
    ): List<ScanData>
}