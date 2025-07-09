package com.dandoor.androidApp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
class SquareBoxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // padding 고려해서 사각형 영역 계산
        val left = paddingLeft.toFloat()
        val top = paddingTop.toFloat()
        val right = width.toFloat() - paddingRight
        val bottom = height.toFloat() - paddingBottom

        // 정사각형 유지: width, height 중 작은 값 기준
        val size = (right - left).coerceAtMost(bottom - top)
        val cx = (left + right) / 2
        val cy = (top + bottom) / 2
        val half = size / 2

        // 내부 사각형 좌표
        val rectLeft = cx - half
        val rectTop = cy - half
        val rectRight = cx + half
        val rectBottom = cy + half

        // 흰색 내부, 검정 테두리
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, fillPaint)
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, borderPaint)
    }
}
