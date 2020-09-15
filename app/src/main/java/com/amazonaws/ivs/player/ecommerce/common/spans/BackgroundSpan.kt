package com.amazonaws.ivs.player.ecommerce.common.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

class BackgroundSpan(
    private val bgColor: Int,
    private val textColor: Int,
    private val corner: Int,
    private val padding: Int,
    private val width: Int,
    private val paddingMulti: Float
) : ReplacementSpan() {

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return width
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int,
                      bottom: Int, paint: Paint) {
        paint.color = bgColor
        val rect = RectF(x - padding / 2, top.toFloat(), x + width - (padding * 1.5).toInt(), bottom.toFloat())
        canvas.drawRoundRect(rect, corner.toFloat(), corner.toFloat(), paint)
        paint.color = textColor
        canvas.drawText(text.toString(), start, end, x + (padding * paddingMulti), y.toFloat(), paint)
    }
}
