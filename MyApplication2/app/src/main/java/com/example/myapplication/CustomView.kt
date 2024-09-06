package com.example.myapplication


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi




class CustomView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private var bitmapCanvas = Canvas(bitmap)
    private val paint = Paint()
    private val rect: Rect by lazy { Rect(0, 0, width, height) }

    init {
        paint.isAntiAlias = true
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
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

    @RequiresApi(Build.VERSION_CODES.O)
    public fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, color: Int) {
        paint.color = Color.BLACK
        bitmapCanvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        paint.color = color
        paint.strokeWidth = 10f // Set the stroke width for the line
        paint.style = Paint.Style.STROKE // Use STROKE style to draw lines
        bitmapCanvas.drawLine(startX, startY, endX, endY, paint)
        invalidate()
    }

    private var lastX = -1f
    private var lastY = -1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x
                val currentY = event.y
                if (lastX >= 0 && lastY >= 0) {
                    drawLine(lastX, lastY, currentX, currentY, Color.RED) // Adjust color as needed
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



