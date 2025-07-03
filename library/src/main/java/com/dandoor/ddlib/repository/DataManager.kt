package com.dandoor.ddlib.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.dandoor.ddlib.bluetooth.BeaconData
import com.dandoor.ddlib.bluetooth.DandoorBTBeacon
import com.dandoor.ddlib.data.AppDatabase
import com.dandoor.ddlib.data.entity.BeaconPosition
import com.dandoor.ddlib.data.entity.Lab
import com.dandoor.ddlib.data.entity.ScanData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Data Manager
 *
 * Lab, Scan_data, Esti_data의 관리를 총괄하는 매니저 클래스
 *
 * 각각의 테이블의 CRUD 기능을 구현하고, 사용의 편리함을 위해 부수적인 함수를 구현합니다.
 */
class DataManager(context: Context) {
    /** Room 패키지 활용 */
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "inner-db"
    ).fallbackToDestructiveMigration(true).build()

    private val labDao = db.labDao()
    private val scanDataDao = db.scanDataDao()

    /** 코루틴을 사용해서 비동기 처리를 간편하게 할 수 있음 */
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        /** default Beacon Positions (ex)
         *  이건 경로 선택 방식이 정해지면, 그에 따라 변경될 예정
         * */
        val beaconPositions = listOf(
            BeaconPosition(x = 0.0, y = 0.0, z = 2.5),
            BeaconPosition(x = 10.0, y = 0.0, z = 2.5),
            BeaconPosition(x = 0.0, y = 8.0, z = 2.5),
            BeaconPosition(x = 10.0, y = 8.0, z = 2.5)
        )
    }

    // Lab Entity CRUD
    fun createLabDefaultSync() {
        coroutineScope.launch {
            createLabDefault()
        }
    }
    suspend fun createLab(position: List<BeaconPosition>, alias: String?): Long {
        val lab = Lab(
            beaconPositions = position,
            createdAt = System.currentTimeMillis(),
            alias = alias ?: "UnKnown"
        )
        return labDao.insert(lab)
    }
    suspend fun createLabWithDefaultBeacons(alias: String?): Long {
        val row = Lab(
            beaconPositions = beaconPositions,
            createdAt = System.currentTimeMillis(),
            alias = alias?: "Unknown"
        )
        return labDao.insert(row)
    }
    suspend fun createLabDefault(): Long {
        val row = Lab(
            beaconPositions = beaconPositions,
            createdAt = System.currentTimeMillis(),
            alias = "Unknown"
        )
        return labDao.insert(row)
    }

    fun readLabByIdSync(labID: Long) {
        coroutineScope.launch {
            readLabById(labID)
        }
    }
    fun readAllLabsSync() {
        coroutineScope.launch {
            readAllLabs()
        }
    }
    suspend fun readLabById(labID: Long): Lab? = labDao.getLabById(labID)
    suspend fun readAllLabs(): List<Lab> = labDao.getAllLabs()
    suspend fun readLabID(): Long = labDao.getLabID()
//    suspend fun updateLab(lab: Lab) = labDao.updateLab(lab)
//    suspend fun deleteLab(lab: Lab) = labDao.deleteLab(lab)

    // ScanData Entity CRUD
    fun saveScanDataSync(beaconData: BeaconData) {
        coroutineScope.launch {
            saveScanData(beaconData)
        }
    }
    fun readScanDataSync(id: Long) {
        coroutineScope.launch {
            readScanData(id)
        }
    }
    suspend fun saveScanData(beaconData: BeaconData) {
        val scanData = ScanData(
            lid = labDao.getLabID(),
            timestamp = beaconData.timestamp,
            beaconID = beaconData.beacon_name,
            rssi = beaconData.beacon_rssi
        )
        scanDataDao.insert(scanData)
    }
    suspend fun readScanData(id: Long): List<ScanData> {
        return scanDataDao.getScanData(id)
    }
}