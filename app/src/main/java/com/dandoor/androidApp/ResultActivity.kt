package com.dandoor.androidApp

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// ResultActivity 클래스 정의
class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)

        val resultView = findViewById<TextView>(R.id.resultTextView)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val countView = findViewById<TextView>(R.id.countLabel)

        // intent로 넘겨 받은 값 활용
        val receivedArray = intent.getIntArrayExtra("intArray")
        if(receivedArray != null){
            // seekbar 설정
            seekBar.max = receivedArray.size
            seekBar.progress = receivedArray.size.coerceAtMost(1)

            updateDisplayedData(receivedArray, seekBar.progress, resultView)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateDisplayedData(receivedArray, progress, resultView)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        else
            resultView.text = "넘겨 받지 못함"
    }
    private fun updateDisplayedData(
        array: IntArray,
        count: Int,
        resultView: TextView
    ) {
        val limited = array.take(count)
        resultView.text = "넘겨 받은 값: ${limited.joinToString(", ")}"
    }
}
