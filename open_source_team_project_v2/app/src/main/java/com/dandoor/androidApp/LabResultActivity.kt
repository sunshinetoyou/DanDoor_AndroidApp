package com.dandoor.androidApp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class LabResultActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lab)
        initViews()

        val resultView = findViewById<TextView>(R.id.resultTextView)

        // intent로 넘겨 받은 값 활용
        val lid = intent.getLongExtra("lid", -1L)

        resultView.text = lid.toString()

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dandoor_LAB"
    }

    /** 뷰 컴포넌트 초기화 */
    private fun initViews() {
        /** 컴포넌트 관련 변수 */

        /** 툴바 관련 변수 */
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
}