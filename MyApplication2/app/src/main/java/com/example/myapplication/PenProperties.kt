package com.example.myapplication

import android.graphics.Color
import android.graphics.Paint

// Data class to encapsulate the properties of the pen
data class PenProperties(
    var color: Int = Color.BLACK,           // Default pen color
    var size: Float = 10f,                  // Default pen size (stroke width)
    var style: Paint.Style = Paint.Style.STROKE,  // Default pen style (STROKE or FILL)
    var shape: ShapeType = ShapeType.LINE
) {
    // Enum class inside PenProperties
    enum class ShapeType{
        LINE, RECTANGLE, CIRCLE
    }
    // Toggle between STROKE and FILL styles
    fun toggleStyle() {
        style = if (style == Paint.Style.STROKE) Paint.Style.FILL else Paint.Style.STROKE
    }
}