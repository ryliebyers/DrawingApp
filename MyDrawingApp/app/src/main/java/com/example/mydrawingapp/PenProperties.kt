package com.example.mydrawingapp


import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

class Pen {
    // Mutable state for pen size
    var size = mutableFloatStateOf(10f) // Default pen size

    // Mutable state for pen color
    var color = mutableStateOf(Color.Black)

    // Mutable state for line or circle selection
    var isLineDrawing = mutableStateOf(false)

    // Method to change pen size
    fun changePenSize(newSize: Float) {
        size.value = newSize
    }

    // Method to change pen color
    fun changePenColor(newColor: Color) {
        color.value = newColor
    }

    // Method to change drawing shape (line or circle)
    fun toggleDrawingShape() {
        isLineDrawing.value = !isLineDrawing.value
    }
}