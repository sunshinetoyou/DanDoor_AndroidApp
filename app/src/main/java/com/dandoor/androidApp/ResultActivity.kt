package com.dandoor.androidApp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout

// ResultActivity 클래스 정의
class ResultActivity : AppCompatActivity() {

    // UI component
    private lateinit var toolbar : Toolbar

    // Data
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)
        initViews()

        // 1. Intent로부터 값 받기
        val labID = intent.getLongExtra("labID", -1L)
        val alias = intent.getStringExtra("alias") ?: "No Alias"
        val createdAt = intent.getLongExtra("createdAt", -1L)

        // 2. 예쁘게 출력 (예시)
        val resultView = findViewById<TextView>(R.id.resultTextView)
        resultView.text = """
            Lab 정보
            ID: $labID
            Alias: $alias
            Created: $createdAt
        """.trimIndent()
    }

    private fun initViews() {
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dandoor_App Lab" // 원하는 타이틀로 변경
    }
}
