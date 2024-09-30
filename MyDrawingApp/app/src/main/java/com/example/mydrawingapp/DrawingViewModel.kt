package com.example.drawingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.mydrawingapp.Drawing
import com.example.mydrawingapp.DrawingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DrawingViewModel(private val repository: DrawingRepository) : ViewModel() {

    val allDrawings: Flow<List<Drawing>> = repository.allDrawings

    fun insertDrawing(drawing: Drawing) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(drawing)
        }
    }

    fun updateDrawing(drawing: Drawing) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(drawing)
        }
    }

    suspend fun getDrawingById(id: Int): Drawing? {
        return repository.getDrawingById(id)
    }
}


