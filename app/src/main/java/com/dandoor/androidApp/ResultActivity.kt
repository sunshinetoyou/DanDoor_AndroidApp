package com.dandoor.androidApp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// ResultActivity 클래스 정의
class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)
        
        val resultView = findViewById<TextView>(R.id.resultTextView)
        
        // intent로 넘겨 받은 값 활용
        val receivedArray = intent.getIntArrayExtra("intArray")
        if(receivedArray != null){
            val text = receivedArray.joinToString(", ")
            resultView.text = "넘겨 받은 값: $text"
        }
        else
            resultView.text = "넘겨 받지 못함"
    }
}
