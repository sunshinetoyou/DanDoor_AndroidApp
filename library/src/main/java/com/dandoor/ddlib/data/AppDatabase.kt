package com.dandoor.ddlib.data

import BeaconPositionConverter
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dandoor.ddlib.data.dao.LabDao
import com.dandoor.ddlib.data.dao.ScanDataDao
import com.dandoor.ddlib.data.entity.Lab
import com.dandoor.ddlib.data.entity.ScanData

@Database(entities = [Lab::class, ScanData::class], version = 2)
@TypeConverters(BeaconPositionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labDao(): LabDao
    abstract fun scanDataDao(): ScanDataDao
}
