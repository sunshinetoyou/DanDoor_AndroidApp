package com.dandoor.ddlib.data

import PositionConverter
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dandoor.ddlib.data.dao.EstiDataDao
import com.dandoor.ddlib.data.dao.LabDao
import com.dandoor.ddlib.data.dao.ScanDataDao
import com.dandoor.ddlib.data.entity.EstiData
import com.dandoor.ddlib.data.entity.Lab
import com.dandoor.ddlib.data.entity.ScanData

@Database(entities = [Lab::class, ScanData::class, EstiData::class], version = 2)
@TypeConverters(PositionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labDao(): LabDao
    abstract fun scanDataDao(): ScanDataDao
    abstract fun estiDataDao(): EstiDataDao
}
