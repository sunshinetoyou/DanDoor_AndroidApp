package com.dandoor.androidApp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dandoor.ddlib.bluetooth.BTManager
import com.dandoor.ddlib.bluetooth.BTVehicle
import com.dandoor.ddlib.data.entity.Lab
import com.dandoor.ddlib.repository.DataManager
import com.google.android.material.progressindicator.CircularProgressIndicator
/*
import android.util.Log
import com.dandoor.ddlib.bluetooth.ReceiveCallback
*/

class MainActivity : AppCompatActivity() {

    /** UI Component */
    private lateinit var btnNewExperiment: Button
    private lateinit var btnContinueExperiment: Button
    private lateinit var btnViewResults: Button

    /** 사이드바 관련 변수 */
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var sidebarMenu : LinearLayout
    private lateinit var toolbar: Toolbar
    private var pendingAction: PendingAction? = null
    enum class PendingAction { CONTINUE, RESULT }

    /** library 활용 */
    private lateinit var btManager: BTManager
    private lateinit var dtManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.root)
        initViews()

        // DataManger 초기화
        dtManager = DataManager(this)

        // BluetoothManger 초기화
        btManager = BTManager(this, dtManager)
        btManager.checkBTPermission(this) {granted ->
            if (granted) {
                setVehicleCallback()
                /*
                 아두이노로 부터 오는 거리 log 찍기
                    btManager.setReceiveCallback(object : ReceiveCallback {
                     override fun onReceive(message: String) {
                         Log.d("ArduinoBT", " 아두이노로 부터 수신 거리: $message")
                         // logcat에 "ArduinoBT" 검색하면 됨
                     }
                */ })

            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
        initViews()
        setDrawerListener()
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    /** 뷰 컴포넌트 초기화 */
    private fun initViews() {
        /** 컴포넌트 관련 변수 */
        btnNewExperiment = findViewById(R.id.btn_new_experiment)
        btnContinueExperiment = findViewById(R.id.btn_continue_experiment)
        btnViewResults = findViewById(R.id.btn_view_results)

        /** 사이브바 관련 변수 */
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        sidebarMenu = findViewById<LinearLayout>(R.id.sidebar_menu)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun btnListener() {
        // 버튼 클릭 리스너 예시
        btnNewExperiment.setOnClickListener {
            lifecycleScope.launch {
                dtManager.createLabDefault()
                val lid = dtManager.readLabID()
                // id를 활용한 추가 작업이 있으면 여기에 작성
                navigateToLab(lid)
            }
        }
        btnContinueExperiment.setOnClickListener {
            pendingAction = PendingAction.CONTINUE
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnViewResults.setOnClickListener {
            pendingAction = PendingAction.RESULT
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // 다른 페이지로 이동하는 함수
    private fun navigateToLab(lid: Long) {
        val intent = Intent(this, LabActivity::class.java)
        intent.putExtra("lid", lid)
        startActivity(intent)
    }
    private fun navigateToLabResult(lid: Long) {
        val intent = Intent(this, LabResultActivity::class.java)
        intent.putExtra("lid", lid)
        startActivity(intent)
    }

    /**
     * Side Bar
     * setDrawerListener()             : 사이드바 이벤트 리스너
     */
    fun setDrawerListener() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                lifecycleScope.launch {
                    val labs = dtManager.readAllLabs()
                    sidebarMenu.removeAllViews()
                    for (lab in labs) {
                        val menuItem = TextView(drawerView.context).apply {
                            text = "ID: ${lab.labID}\nAlias: ${lab.alias}\nCreated: ${lab.createdAt}"
                            setPadding(16, 16, 16, 16)
                            setOnClickListener {
                                drawerLayout.closeDrawer(GravityCompat.START)
                                when (pendingAction) {
                                    PendingAction.CONTINUE -> navigateToLab(lab.labID)
                                    PendingAction.RESULT -> navigateToLabResult(lab.labID)
                                    else -> Toast.makeText(context, "동작을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                                }
                                pendingAction = null // 동작 후 초기화
                            }
                        }
                        sidebarMenu.addView(menuItem)
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }


    /** Bluetooth
     *
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
}
