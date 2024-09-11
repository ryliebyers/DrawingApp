package com.example.myapplication
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class SimpleViewModel : ViewModel() {

    // Holds the currently active drawing's name (for saving and editing)
    var currentDrawingName: String? = null
    private val _bitmap: MutableLiveData<Bitmap> = MutableLiveData()
    val bitmap: LiveData<Bitmap> = _bitmap
    var isDrawingSaved: Boolean = false // New flag to track saved status

    // Store file paths instead of bitmaps
    private val drawings: MutableMap<String, String> = mutableMapOf()

    // Load a drawing by name from internal storage
    fun getDrawingByName(context: Context, drawingName: String): Bitmap? {
        // First check if the drawing is in the map
        val filePath = drawings[drawingName] ?: return null

        return loadDrawingFromStorage(filePath)
    }

    // Load the bitmap from internal storage
    private fun loadDrawingFromStorage(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun getAllDrawings(context: Context): List<Pair<String, Bitmap>> {
        val savedDrawings = mutableListOf<Pair<String, Bitmap>>()

        // Load all drawings stored in filesDir
        val files = context.filesDir.listFiles()
        files?.forEach { file ->
            if (file.extension == "png") {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    // Only add valid bitmaps to the list
                    savedDrawings.add(Pair(file.nameWithoutExtension, bitmap))

                    // Populate the drawings map
                    drawings[file.nameWithoutExtension] = file.absolutePath
                } else {
                    // If the bitmap couldn't be loaded (corrupt), delete the file
                    file.delete()
                }
            }
        }

        return savedDrawings
    }

    fun removeDrawingByName(drawingName: String) {
        val filePath = drawings[drawingName]
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete() // Delete the file from storage
            }
        }
        drawings.remove(drawingName) // Remove the entry from the map
    }



    fun setBitmap(newBitmap: Bitmap) {
        _bitmap.value = newBitmap
        isDrawingSaved = false // Set to false when new bitmap is set
    }
}
