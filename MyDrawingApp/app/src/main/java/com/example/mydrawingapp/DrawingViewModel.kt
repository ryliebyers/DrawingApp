package com.example.drawingapp.viewmodel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydrawingapp.Drawing
import com.example.mydrawingapp.DrawingRepository
import com.example.mydrawingapp.DrawnPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.graphics.Paint
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


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

    suspend fun getDrawingById(id: Int): Drawing? = withContext(Dispatchers.IO) {
        repository.getDrawingById(id)
    }

//    suspend fun getDrawingsByEmail(email: String): LiveData<List<Drawing>> {
//        return repository.getDrawingsByEmail(email).asLiveData()
//    }

    // Renders a Drawing object into a Bitmap by loading the image from its file path
    fun getCurrentDrawing(drawing: Drawing): Bitmap {
        val file = File(drawing.filePath)
        return if (file.exists()) {
            // Decode the bitmap from the file path
            BitmapFactory.decodeFile(drawing.filePath)
        } else {
            // If the file does not exist, create a blank bitmap as a fallback
            val width = 800
            val height = 800
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Fill the background with white
            // Returns the blank white bitmap
            bitmap
        }
    }


}


