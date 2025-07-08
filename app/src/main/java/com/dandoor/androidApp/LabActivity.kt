package com.dandoor.androidApp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dandoor.ddlib.bluetooth.BTManager
import com.dandoor.ddlib.estimation.EstimationPluginManager
import com.dandoor.ddlib.estimation.plugin.TrilaterationEstimator
import com.dandoor.ddlib.repository.DataManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LabActivity : AppCompatActivity() {

    /** UI Component */
    private lateinit var resultView: TextView
    private lateinit var accuracyLabel: TextView
    private lateinit var scanButton: Button
    private lateinit var toolbar: Toolbar

    /** library 활용 */
    private lateinit var btManager: BTManager
    private lateinit var dtManager: DataManager
    private lateinit var pluginManager: EstimationPluginManager

    /** 전역변수 */
    private var lid: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lab)
        init()

        // DataManger 초기화
        dtManager = DataManager(this)

        // BluetoothManger 초기화
        btManager = BTManager(this, dtManager)
        btManager.checkBTPermission(this)

        // pluginManager 초기화
        pluginManager = EstimationPluginManager(dtManager)
        val trilaterationPlugin = TrilaterationEstimator()
        pluginManager.register(trilaterationPlugin)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dandoor_LAB_${lid}"
    }

    /** 초기화 */
    private fun init() {
        /** 컴포넌트 변수 */
        resultView = findViewById<TextView>(R.id.resultTextView)
        resultView.text = " id | lid | timestamp | beaconID | rssi |"
        accuracyLabel = findViewById(R.id.labelTotalAccuracy)
        scanButton = findViewById(R.id.btnScan)
        scanButton.setOnClickListener {
            scanButton.isEnabled = false
            val originalText = scanButton.text
            val startTime = System.currentTimeMillis()
            scanButton.text = "스캔 중..."

            btManager.startScan()

            scanButton.postDelayed({
                scanButton.text = originalText
                scanButton.isEnabled = true
                btManager.stopScan()
                try {
                    val windowData = dtManager.getSlicedData(lid, startTime)
                    val beaconInfo = windowData.beaconRssi.entries.joinToString(separator = "\n") { (beacon, rssi) ->
                        "  $beacon: $rssi"
                    }
                    val text = "윈도우 시작: ${formatWindowStartMs(windowData.windowStart)}\n$beaconInfo"
                    resultView.text = text
                } catch (e: Exception) {
                    resultView.text = "해당 구간에 데이터가 없습니다."
                }
            }, 1000)
        }
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        /** 전역변수 */
        lid = intent.getLongExtra("lid", -1L)
    }

    fun formatWindowStartMs(windowStart: Long): String {
        val date = Date(windowStart)
        val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return format.format(date)
    }
}