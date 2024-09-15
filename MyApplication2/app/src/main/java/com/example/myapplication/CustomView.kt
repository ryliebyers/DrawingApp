package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

class CustomView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private var bitmapCanvas = Canvas(bitmap)
    private val paint = Paint()
    private var penProperties = PenProperties() // Use PenProperties to store pen state
    private val rect: Rect by lazy { Rect(0, 0, width, height) }
    private val drawings: LinkedHashMap<String, Bitmap> = LinkedHashMap()
    private var startX = 0f
    private var startY = 0f
    private var shapeType: PenProperties.ShapeType = PenProperties.ShapeType.LINE

    // Method to set the shape type
    fun setShape(shape: PenProperties.ShapeType) {
        shapeType = shape
    }

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
        // re-draw the view with the new bitmap
        invalidate()
    }

    fun setPenColor(color: Int) {
        penProperties.color = color
        updatePaint()
    }

    fun setPenSize(size: Float) {
        penProperties.size = size
        updatePaint()
    }


    fun getBitmap(): Bitmap {
        return bitmap
    }


    private fun drawShape(startX: Float, startY: Float, endX: Float, endY: Float) {
        paint.color = penProperties.color
        paint.strokeWidth = penProperties.size
        paint.style = penProperties.style

        when(shapeType){
            PenProperties.ShapeType.RECTANGLE -> {
                bitmapCanvas.drawRect(startX, startY, endX, endY, paint)
            }

            PenProperties.ShapeType.LINE -> {
                bitmapCanvas.drawLine(startX, startY, endX, endY, paint)
            }
            PenProperties.ShapeType.CIRCLE -> {
                val radius = hypot((endX - startX).toDouble(), (endY - startY).toDouble()).toFloat()
                bitmapCanvas?.drawCircle(startX, startY, radius, paint)
            }
        }
        invalidate()
    }

    private var lastX = -1f
    private var lastY = -1f


    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Scale factor to map touch coordinates to the bitmap
        val scaleX = bitmap.width.toFloat() / width.toFloat()
        val scaleY = bitmap.height.toFloat() / height.toFloat()
        val currentX = event.x * scaleX
        val currentY = event.y * scaleY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = currentX
                startY = currentY
                lastX = currentX
                lastY = currentY
            }
            MotionEvent.ACTION_MOVE -> {
                if(penProperties.shape == PenProperties.ShapeType.LINE) {
                    if (lastX >= 0 && lastY >= 0) {
                        drawShape(lastX, lastY, currentX, currentY) // Draw line with current pen properties
                    }
                    lastX = currentX
                    lastY = currentY
                } else{
                    // Erase the previous shape before drawing the new one for preview effect
                    invalidate()
                    drawShape(startX, startY, event.x * scaleX, event.y * scaleY)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (penProperties.shape != PenProperties.ShapeType.LINE) {
                    // Finalize the shape drawing for non-line shapes
                    drawShape(startX, startY, currentX, currentY)
                }
                lastX = -1f
                lastY = -1f
            }
        }
        return true
    }
}