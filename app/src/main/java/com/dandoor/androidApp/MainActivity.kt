package com.dandoor.androidApp

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dandoor.ddlib.DandoorBTBeacon
import com.dandoor.ddlib.DandoorBTManager
import com.dandoor.ddlib.DandoorBTVehicle
import com.dandoor.ddlib.DataBeacon
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    /** UI Component */
    private lateinit var tvBleStatus: TextView
    private lateinit var btnConnectBluetooth: Button
    private lateinit var btnLabToggle: ToggleButton
    private lateinit var btnEngineToggle: ToggleButton
    private lateinit var progressTimer: CircularProgressIndicator
    private lateinit var etMinutes: EditText
    private lateinit var etSeconds: EditText
    private lateinit var tvTimerDisplay: TextView

    /** 타이머 관련 변수 */
    private var countDownTimer: CountDownTimer? = null
    private var totalTimeInSeconds = 0
    private var isTimerRunning = false

    private lateinit var btManager: DandoorBTManager
    private val beaconScanHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // BluetoothManger 초기화
        btManager = DandoorBTManager(this)
        // BT 권한 체크 및 요청
        btManager.checkBTPermission(this) {granted ->
            if (granted) {
                setVehicleCallback()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        initViews()
        setupBtnListeners()

        // 스캔 시작
        btManager.startScan(object : DandoorBTBeacon.BeaconScanCallback {
            override fun onScanResult(data: DataBeacon) {
                Log.d("MainActivity", "발견: ${data.timestamp}, RSSI: ${data.beacon_name}, RSSI: ${data.beacon_rssi}")
                // TODO DATA Manager에게 넘겨서 데이터 저장
            }
            override fun onScanFinished() {
                Log.d("MainActivity", "스캔 종료")
            }
        })
    }

    /** 뷰 컴포넌트 초기화 (main_activity의 컴포넌트와 연결) */
    private fun initViews() {
        tvBleStatus = findViewById(R.id.tv_ble_status)
        btnConnectBluetooth = findViewById(R.id.btn_connect_bluetooth)
        btnLabToggle = findViewById(R.id.btn_lab_toggle)
        btnEngineToggle = findViewById(R.id.btn_engine_toggle)
        progressTimer = findViewById(R.id.progress_timer)
        etMinutes = findViewById(R.id.et_minutes)
        etSeconds = findViewById(R.id.et_seconds)
        tvTimerDisplay = findViewById(R.id.tv_timer_display)

        // 초기값 설정
        etMinutes.setText("00")
        etSeconds.setText("00")
    }

    /** 블루투스 연결, 랩 토글, 시동 토글 버튼 리스너 */
    private fun setupBtnListeners() {
        btnConnectBluetooth.setOnClickListener {
            btManager.connectToCar()
        }

        btnLabToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 입력값 파싱
                val minutes = etMinutes.text.toString().toIntOrNull() ?: 0
                val seconds = etSeconds.text.toString().toIntOrNull() ?: 0
                totalTimeInSeconds = minutes * 60 + seconds

                // 유효성 검사
                if (totalTimeInSeconds <= 0) {
                    Toast.makeText(this, "시간을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    btnLabToggle.isChecked = false;
                } else {
                    // TODO Start LAB (DB 연결 시작 및 Lab 고정)
                    startTimer()
                }
            }
            else {
                // TODO Stop LAB (DB 연결 중단 및 Lab 횟수 증가)
                stopTimer()
            }
        }

        btnEngineToggle.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked) btManager.sendCarCommand("start")
            else btManager.sendCarCommand("stop")
        }
    }


    /** Timer
     * startTimer()     :
     * stopTImer()      :
     * resetTimerUI()   :
     * formatTime()     :
     * */
    private fun startTimer() {
        if (isTimerRunning) return

        // 입력값 파싱
        val minutes = etMinutes.text.toString().toIntOrNull() ?: 0
        val seconds = etSeconds.text.toString().toIntOrNull() ?: 0
        totalTimeInSeconds = minutes * 60 + seconds

        // 유효성 검사
        if (totalTimeInSeconds <= 0) {
            Toast.makeText(this, "시간을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        btnEngineToggle.isChecked = true
        isTimerRunning = true

        // 입력 UI 숨기기
        etMinutes.visibility = View.GONE
        etSeconds.visibility = View.GONE
        findViewById<TextView>(R.id.textViewM).visibility = View.GONE
        findViewById<TextView>(R.id.textViewS).visibility = View.GONE
        tvTimerDisplay.visibility = View.VISIBLE  // 타이머 표시 UI 보이기

        // 카운트다운 타이머 생성 및 시작
        countDownTimer = object : CountDownTimer((totalTimeInSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val progress = ((totalTimeInSeconds - secondsRemaining) * 100) / totalTimeInSeconds

                progressTimer.progress = progress
                tvTimerDisplay.text = formatTime(secondsRemaining)
            }

            override fun onFinish() {
                progressTimer.progress = 100
                btnLabToggle.isChecked = false
                btnEngineToggle.isChecked = false
                resetTimerUI()
            }
        }.start()
    }
    private fun stopTimer() {
        countDownTimer?.cancel()
        btnEngineToggle.isChecked = false
        resetTimerUI()
    }
    private fun resetTimerUI() {
        isTimerRunning = false
        progressTimer.progress = 0

        etMinutes.visibility = View.VISIBLE
        etSeconds.visibility = View.VISIBLE
        findViewById<TextView>(R.id.textViewM).visibility = View.VISIBLE
        findViewById<TextView>(R.id.textViewS).visibility = View.VISIBLE
        tvTimerDisplay.visibility = View.GONE
    }
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    /** Bluetooth
     * setVehicleCallback()          :
     * onRequestPermissionsResult()   :
     * updateBluetoothStatus(BOOL)    :
     */
    private fun setVehicleCallback() {
        btManager.setVehicleCallback(object : DandoorBTVehicle.VehicleConnectionCallback {
            override fun onConnectionStatusChanged(isConnected: Boolean, deviceName: String?) {
                updateBluetoothStatus(isConnected)
            }
            override fun onConnectionError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
            override fun onCommandSent(command: String) {
                Toast.makeText(this@MainActivity, "명령 전송: $command", Toast.LENGTH_SHORT).show()
            }
            override fun onCommandError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        })
    }
    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        btManager.onRequestPermissionResult(requestCode, grantResults) { allGranted ->
            if (allGranted) {
                setVehicleCallback()
            } else {
                Toast.makeText(this, "권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateBluetoothStatus(isConnected: Boolean) {
        if (isConnected) {
            tvBleStatus.text = "BLUETOOTH: 연결됨"
            tvBleStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connected))
        }
        else {
            tvBleStatus.text = "BLUETOOTH: 연결 해제"
            tvBleStatus.setTextColor(ContextCompat.getColor(this, R.color.status_disconnected))
        }
    }
}