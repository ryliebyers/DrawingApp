package com.example.mydrawingapp

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

import android.widget.Toast
import androidx.navigation.NavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun saveDrawing(
    context: Context,
    bitmap: Bitmap?,
    name: String?,
    filePath: String?,
    viewModel: DrawingViewModel,
    navController: NavController,
    drawingId: Int?,
    email: String?
) {
    try {
//        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (email == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: No user logged in", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Debugging... Log values before proceeding to save
        println("DEBUG: filePath = $filePath, name = $name, bitmap = $bitmap, id = $drawingId")

        // Ensure that none of the critical values are null
        if (filePath == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: File path is missing", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (bitmap == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: Bitmap is null", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (name.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: Drawing name is missing", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Log information for debugging
        println("DEBUG: Saving drawing with name: $name, filePath: $filePath")

        // Save or overwrite the image to the specified file path
        val file = File(filePath)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Check if the drawing is new or being updated
        if (drawingId == null || drawingId == -1) {
            // Insert a new drawing
            viewModel.insertDrawing(Drawing(name = name, filePath = filePath, email = email))
        } else {
            // Update the existing drawing
            viewModel.updateDrawing(Drawing(id = drawingId, name = name, filePath = filePath, email = email))
        }

        // Show success message
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Drawing saved successfully!", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        }
    } catch (e: IOException) {
        // Handle IO-related exceptions (like issues with saving files)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to save drawing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        e.printStackTrace()
    } catch (e: Exception) {
        // Catch other unexpected exceptions
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        e.printStackTrace()
    }
}

