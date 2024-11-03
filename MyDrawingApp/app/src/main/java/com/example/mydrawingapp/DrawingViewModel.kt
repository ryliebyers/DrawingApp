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
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mydrawingapp.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File


class DrawingViewModel(private val repository: DrawingRepository) : ViewModel() {
    val serverDrawings = MutableStateFlow<List<Drawing>>(emptyList())

//    val allDrawings: Flow<List<Drawing>> = repository.allDrawings

    private val currentUserEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email ?: ""


    // Flow to observe only the current user's drawings
    val userDrawings: Flow<List<Drawing>> = repository.allDrawings.map { drawings ->
        drawings.filter { it.email == currentUserEmail }
    }





    init {
        fetchDrawings()
    }


    private fun fetchDrawings() {
        viewModelScope.launch {
            try {
                val drawings = fetchDrawingsFromServer() // Fetch drawings from server
                for (drawing in drawings) {
                    repository.insert(drawing) // Insert each drawing individually
                }
                serverDrawings.value = drawings // Update the state flow
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Error fetching drawings: ${e.message}")
            }
        }
    }



    private suspend fun fetchDrawingsFromServer(): List<Drawing> {
        return withContext(Dispatchers.IO) {
            try {
               RetrofitInstance.api.getDrawings()
            } catch (e: Exception) {
                Log.e("DrawingViewModel", "Error fetching drawings: ${e.message}")
                emptyList() // Return an empty list on error
            }
        }
    }




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


