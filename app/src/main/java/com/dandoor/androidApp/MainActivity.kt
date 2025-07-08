package com.dandoor.androidApp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.dandoor.ddlib.bluetooth.BTManager
import com.dandoor.ddlib.bluetooth.BTVehicle
import com.dandoor.ddlib.repository.DataManager
import com.dandoor.ddlib.data.entity.Lab
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var rvLabList: RecyclerView
    private lateinit var btnCreateNewLab: Button
    private lateinit var labAdapter: LabListAdapter

    /** 타이머 관련 변수 */
    private var countDownTimer: CountDownTimer? = null
    private var totalTimeInSeconds = 0
    private var isTimerRunning = false

    private lateinit var btManager: BTManager
    private lateinit var dtManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 새로운 activity_main.xml 사용 (기존 main.xml을 include)
        setContentView(R.layout.activity_main)
        // Library 모듈 초기화
        initializeManagers()

        // UI 컴포넌트 초기화
        initViews()

        // 사이드바 설정
        setupNavigationDrawer()

        // 기존 기능 설정
        setupBtnListeners()

        // Lab 데이터 로드 및 사이드바 리스트 설정
        loadLabDataAndSetupList()
    }
    private fun initializeManagers() {
        // DataManger 초기화
        dtManager = DataManager(this)

        // BluetoothManger 초기화
        btManager = BTManager(this, dtManager)
        btManager.checkBTPermission(this) { granted ->
            if (granted) {
                setVehicleCallback()
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
        initViews()
        setupBtnListeners()
        // 버튼 누르면 SecondActivity로 이동
        val myButton = findViewById<Button>(R.id.butt)
        myButton.setOnClickListener {
            val testarray = intArrayOf(1,2,3,4,5,6,7,8,9)  // 넘겨주는 값 예시로 배열
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("intArray", testarray)  // 값 넘겨주기
            startActivity(intent)
        }
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
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        // 초기값 설정
        etMinutes.setText("00")
        etSeconds.setText("00")
        // NavigationView의 헤더에서 RecyclerView와 버튼 찾기
        val headerView = navView.getHeaderView(0)
        rvLabList = headerView.findViewById(R.id.rv_lab_list)
        btnCreateNewLab = headerView.findViewById(R.id.btn_create_new_lab)
    }
    /**
     * Navigation Drawer 설정
     */
    private fun setupNavigationDrawer() {
        // 햄버거 메뉴 버튼을 기존 UI에 추가 (toolbar 없이 텍스트로 대체)
        val btnOpenDrawer = Button(this).apply {
            text = "실험 목록 ☰"
            setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 새 실험 생성 버튼 클릭 리스너
        btnCreateNewLab.setOnClickListener {
            createNewLab()
        }
    }
    /**
     * Library DataManager를 통해 Lab 데이터 로드 및 사이드바 리스트 설정
     */
    private fun loadLabDataAndSetupList() {
        lifecycleScope.launch {
            try {
                // Library의 DataManager를 통해 모든 Lab 데이터 조회
                val labList = dtManager.readAllLabs()

                // 사이드바 RecyclerView에 데이터 표시
                setupLabRecyclerView(labList)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Lab 데이터를 사이드바 RecyclerView에 표시
     */
    private fun setupLabRecyclerView(labList: List<Lab>) {
        // RecyclerView 레이아웃 매니저 설정
        rvLabList.layoutManager = LinearLayoutManager(this)

        // Lab 어댑터 생성 및 아이템 클릭 리스너 설정
        labAdapter = LabListAdapter(labList) { lab ->
            // Lab 아이템 클릭 시 ResultActivity로 이동
            openResultActivity(lab)
            // 사이드바 닫기
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // RecyclerView에 어댑터 연결
        rvLabList.adapter = labAdapter
    }
    /**
     * 새로운 Lab 생성 (Library DataManager 사용)
     */
    private fun createNewLab() {
        lifecycleScope.launch {
            try {
                // Library의 DataManager를 통해 기본 Lab 생성
                val newLabId = dtManager.createLabDefault()

                Toast.makeText(this@MainActivity, "새 실험 생성됨 (ID: $newLabId)", Toast.LENGTH_SHORT).show()

                // 사이드바 리스트 새로고침
                loadLabDataAndSetupList()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "실험 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * ResultActivity로 이동 (labID 전달)
     */
    private fun openResultActivity(lab: Lab) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            // Lab ID를 Intent Extra로 전달
            putExtra("labID", lab.labID)
            putExtra("labAlias", lab.alias)
            putExtra("createdAt", lab.createdAt)
        }
        startActivity(intent)
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
                    dtManager.createLabDefaultSync()
                    btManager.startScan()
                    startTimer()
                }
            }
            else {
                stopTimer()
                btManager.stopScan()
            }
        }

        btnEngineToggle.setOnCheckedChangeListener { _, isChecked ->
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
        btManager.setVehicleCallback(object : BTVehicle.VehicleConnectionCallback {
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
    /**
     * 뒤로가기 버튼 처리 (사이드바가 열려있으면 닫기)
     */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Activity 종료 시 리소스 해제
     */
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
