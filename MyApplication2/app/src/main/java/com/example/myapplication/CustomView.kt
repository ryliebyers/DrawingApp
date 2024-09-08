package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private var bitmapCanvas = Canvas(bitmap)
    private val paint = Paint()
    private var penProperties = PenProperties() // Use PenProperties to store pen state
    private val rect: Rect by lazy { Rect(0, 0, width, height) }

    init {
        paint.isAntiAlias = true
        updatePaint()
    }

    private fun updatePaint() {
        paint.color = penProperties.color
        paint.strokeWidth = penProperties.size
        paint.style = penProperties.style
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    fun passBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.bitmapCanvas = Canvas(bitmap)
        invalidate()  // Redraw the view with the new bitmap
    }

    fun setPenColor(color: Int) {
        penProperties.color = color
        updatePaint()
    }

    fun setPenSize(size: Float) {
        penProperties.size = size
        updatePaint()
    }

    fun togglePenStyle() {
        penProperties.toggleStyle()
        updatePaint()
    }

    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        paint.color = penProperties.color
        paint.strokeWidth = penProperties.size
        paint.style = penProperties.style
        bitmapCanvas.drawLine(startX, startY, endX, endY, paint)
        invalidate()
    }

    private var lastX = -1f
    private var lastY = -1f

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // Scale factor to map touch coordinates to the bitmap
        val scaleX = bitmap?.width?.toFloat()?.div(width.toFloat()) ?: 1f
        val scaleY = bitmap?.height?.toFloat()?.div(height.toFloat()) ?: 1f


        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                lastX = event.x * scaleX
                lastY = event.y * scaleY
            }
            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x * scaleX
                val currentY = event.y * scaleY
                if (lastX >= 0 && lastY >= 0) {
                    drawLine(lastX, lastY, currentX, currentY) // Draw line with current pen properties
                }
                lastX = currentX
                lastY = currentY
            }
            MotionEvent.ACTION_UP -> {
                lastX = -1f
                lastY = -1f
            }
        }
        return true
    }
}