package com.dandoor.ddlib.repository

import android.content.Context
import androidx.room.Room
import com.dandoor.ddlib.data.entity.BeaconData
import com.dandoor.ddlib.data.AppDatabase
import com.dandoor.ddlib.data.entity.EstiData
import com.dandoor.ddlib.data.entity.Lab
import com.dandoor.ddlib.data.entity.ScanData
import com.dandoor.ddlib.data.entity.Position
import com.dandoor.ddlib.data.entity.config
import com.dandoor.ddlib.estimation.model.TimeWindowBeaconRssi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    private val estiDataDao = db.estiDataDao()

    /** 코루틴을 사용해서 비동기 처리를 간편하게 할 수 있음 */
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        /** default Beacon Positions (ex)
         * */
        val beaconPositions = listOf(
            Position(x = 0.0, y = 0.0, z = 0.0),
            Position(x = 6.0, y = 0.0, z = 0.0),
            Position(x = 6.0, y = 6.0, z = 0.0),
            Position(x = 0.0, y = 6.0, z = 0.0)
        )
        val defualtConfig = config(
            speed = 0.0,
            pathLength = 2.0,
            startPos = Position(0.0, 0.0),
            windowSize = 1100L
        )
    }

    /** Lab Entity CRUD */
    fun createLabDefaultSync() {
        coroutineScope.launch {
            createLabDefault()
        }
    }
    suspend fun createLab(position: List<Position>, alias: String?): Long {
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

    /** ScanData Entity CRUD */
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
    suspend fun readScanData(
        id: Long,
        stime: Long? = null,
        etime: Long? = null
    ): List<ScanData> {
        return if (stime != null && etime != null) {
            scanDataDao.getScanDataInWindow(id, stime, etime)
        } else {
            scanDataDao.getScanData(id)
        }
    }

    /** EstiData Entity CRUD */
    fun saveEstiDataSync(estiData: EstiData) {
        coroutineScope.launch {
            estiDataDao.insertEstiData(estiData)
        }
    }
    fun saveAllEstiDataSync(results: List<EstiData>) {
        coroutineScope.launch {
            estiDataDao.insertAll(results)
        }
    }
    fun readEstiDataSync(labId: Long, method: String) {
        coroutineScope.launch {
            estiDataDao.getResultsByLabAndAlgorithm(labId, method)
        }
    }

    /** Special */
    /** 시간 윈도우(0.5)에 따른 각 비콘의 평균 RSSI 값 도출*/
    fun getWindowedBeaconRssi(labId: Long?): List<TimeWindowBeaconRssi> {
        if (labId == null) error("존재하지 않는 labID 입니다.")

        // 1. LabID로 ScanData 쿼리
        val scanDataList = runBlocking {
            readScanData(labId)
        }
        if (scanDataList.isEmpty()) return emptyList()

        // 2. 윈도우 시작 기준 계산
        val minTimestamp = scanDataList.minOf { it.timestamp }
        val windowSize = defualtConfig.windowSize

        // 3. 윈도우 인덱스 계산
        val groupedByWindow = scanDataList.groupBy { (it.timestamp - minTimestamp) / windowSize }

        // 4. 각 윈도우 내에서 beaconID별로 그룹핑 후 RSSI 평균 계산
        return groupedByWindow.map { (windowIdx, windowData) ->
            val beaconAverages = windowData
                .groupBy { it.beaconID }
                .mapValues { (_, scans) -> scans.map { it.rssi }.average() }
            TimeWindowBeaconRssi(
                windowStart = minTimestamp + windowIdx * windowSize,
                beaconRssi = beaconAverages
            )
        }.sortedBy { it.windowStart }
    }
    fun getSlicedData(labId: Long?, stime: Long): TimeWindowBeaconRssi {
        if (labId == null) error("존재하지 않는 labID 입니다.")

        val windowSize = defualtConfig.windowSize

        val scanDataList = runBlocking {
            readScanData(labId)
        }.filter { it.timestamp in stime until (stime + windowSize) }

        if (scanDataList.isEmpty()) error("해당 구간에 데이터가 없습니다.")

        val beaconAverages = scanDataList
            .groupBy { it.beaconID }
            .mapValues { (_, scans) -> scans.map { it.rssi }.average() }

        return TimeWindowBeaconRssi(
            windowStart = stime,
            beaconRssi = beaconAverages
        )
    }
}