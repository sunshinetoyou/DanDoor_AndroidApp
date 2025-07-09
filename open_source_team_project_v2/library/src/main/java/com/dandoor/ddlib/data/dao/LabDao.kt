package com.dandoor.ddlib.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dandoor.ddlib.data.entity.Lab

/** LAB_DAO
 * Lab 테이블에 접근하기 위한 API를 정의함
 */
@Dao
interface LabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Lab): Long

    @Query("SELECT * FROM lab WHERE labId = :labId")
    suspend fun getLabById(labId: Long): Lab?

    @Query("SELECT * FROM lab")
    suspend fun getAllLabs(): List<Lab>

    @Query("SELECT MAX(labID) FROM lab")
    suspend fun getLabID(): Long
//    @Update
//    suspend fun updateLab(lab: Lab)
//    @Delete
//    suspend fun deleteLab(lab: Lab)
}