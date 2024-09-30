package com.example.mydrawingapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drawingapp.viewmodel.DrawingViewModel


class DrawingViewModelFactory(private val repository: DrawingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            return DrawingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

