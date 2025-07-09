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
    private var scanState = "스캔 시작"
    private var startTime: Long = 0
    private var endTime: Long = 0

    private fun init() {
        /** 컴포넌트 변수 */
        resultView = findViewById(R.id.resultTextView)
        resultView.text = ""

        accuracyLabel = findViewById(R.id.labelTotalAccuracy)
        scanButton = findViewById(R.id.btnScan)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /** 전역변수 */
        lid = intent.getLongExtra("lid", -1L)

        scanButton.text = scanState

        scanButton.setOnClickListener {
            when (scanState) {
                "스캔 시작" -> {
                    scanState = "스캔 중"
                    scanButton.text = scanState
                    scanButton.isEnabled = false

                    // 스캔 시작
                    startTime = System.currentTimeMillis()
                    val startFormatted = formatWindowStartMs(startTime)
                    btManager.startScan()
                    resultView.text = "시작시간:$startFormatted"
                    // 일정 시간 후 상태 전환 가능하게 버튼 활성화
                    scanButton.postDelayed({
                        scanButton.isEnabled = true
                    }, 200)
                }

                "스캔 중" -> {
                    scanState = "스캔 완료"
                    scanButton.text = scanState
                    scanButton.isEnabled = false

                    // 스캔 종료
                    endTime = System.currentTimeMillis()
                    btManager.stopScan()

                    val elapsedMillis = endTime - startTime
                    val distance = elapsedMillis * 70 / 1000
                    val startFormatted = formatWindowStartMs(startTime)
                    val endFormatted = formatWindowStartMs(endTime)

                    try {
                        val windowData = dtManager.getSlicedData(lid, startTime)
                        val beaconInfo = windowData.beaconRssi.entries.joinToString(separator = "\n") { (beacon, rssi) ->
                            "  $beacon: $rssi"
                        }

                        val text = """
                        시작시간 : $startFormatted
                        도착시간 : $endFormatted
                        총 거리: ${distance}cm
                        
                        스캔된 비콘 정보:
                        $beaconInfo
                    """.trimIndent()

                        resultView.text = text
                    } catch (e: Exception) {
                        resultView.text = """
                        시작시간 : $startFormatted
                        도착시간 : $endFormatted
                        총 거리: ${distance}cm
                    """.trimIndent()
                    }

                    scanButton.isEnabled = false
                }
            }
        }
    }


    fun formatWindowStartMs(windowStart: Long): String {
        val date = Date(windowStart)
        val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return format.format(date)
    }
}