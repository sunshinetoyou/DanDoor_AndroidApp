package com.dandoor.ddlib.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dandoor.ddlib.data.entity.ScanData

@Dao
interface ScanDataDao {
    @Insert
    suspend fun insert(scanData: ScanData): Long

    @Query("SELECT * FROM scan_data WHERE lid = :labID")
    suspend fun getScanData(labID: Long): List<ScanData>
}