package com.example.mydrawingapp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun DrawingScreen(navController: NavController, drawingId: Int?, viewModel: DrawingViewModel) {
    val context = LocalContext.current
    var drawingName by remember { mutableStateOf("") }
    val drawingPath = remember { mutableStateListOf<Pair<Float, Float>>() }
    val coroutineScope = rememberCoroutineScope()

    // Add state to hold the ImageBitmap of the saved image
    var savedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var filePath by remember { mutableStateOf<String?>(null) } // Hold the file path for saving/updating
    var canvasWidth by remember { mutableStateOf(400) } // Default canvas width
    var canvasHeight by remember { mutableStateOf(400) } // Default canvas height

    // Flag to check if the user is creating a new drawing
    val isCreatingNewDrawing = drawingId == -1

    // Load existing drawing if editing
    LaunchedEffect(drawingId) {
        if (drawingId != null && drawingId != -1) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val drawing = viewModel.getDrawingById(drawingId)
                    drawing?.let {
                        drawingName = it.name
                        filePath = it.filePath // Store the existing file path

                        // Load the saved image from file path and convert it to ImageBitmap
                        val file = File(it.filePath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.path)
                            savedImageBitmap = bitmap.asImageBitmap() // Convert Bitmap to ImageBitmap

                            // Adjust the canvas size to match the bitmap size
                            canvasWidth = bitmap.width
                            canvasHeight = bitmap.height
                        }
                    }
                } catch (e: Exception) {
                    println("Error loading drawing: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        // Display a loading indicator if data is still loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Canvas for Drawing
            Canvas(
                modifier = Modifier
                    .size(canvasWidth.dp, canvasHeight.dp) // Match the canvas size to the bitmap size
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            drawingPath.add(Pair(change.position.x, change.position.y))
                        }
                    }
            ) {
                // Ensure that the image fills the canvas with no scaling or cropping
                savedImageBitmap?.let { image ->
                    drawImage(
                        image = image,  // ImageBitmap is now used here
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()) // Match canvas to image
                    )
                }

                // Draw any additional user input after loading the saved drawing
                for (point in drawingPath) {
                    drawCircle(Color.Blue, radius = 10f, center = androidx.compose.ui.geometry.Offset(point.first, point.second))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show input field for drawing name ONLY when creating a new drawing
            if (isCreatingNewDrawing) {
                BasicTextField(
                    value = drawingName,
                    onValueChange = { drawingName = it },
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(
                            Modifier
                                .background(Color.Gray)
                                .padding(16.dp)
                        ) {
                            if (drawingName.isEmpty()) Text("Enter Drawing Name")
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Save Button
            Button(
                onClick = {
                    // Create a new bitmap from the user's drawing
                    val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    for (point in drawingPath) {
                        canvas.drawCircle(point.first, point.second, 10f, android.graphics.Paint().apply { color = Color.Blue.toArgb() })
                    }

                    // Perform the save operation based on whether we're creating a new drawing or editing an existing one
                    coroutineScope.launch(Dispatchers.IO) {
                        // Generate a new file path if this is a new drawing
                        if (isCreatingNewDrawing) {
                            filePath = File(context.filesDir, "$drawingName.png").path
                        }

                        if (filePath != null && drawingName.isNotEmpty()) {
                            // Save or update the drawing
                            saveDrawing(context, bitmap, drawingName, filePath!!, viewModel, navController, drawingId)
                        } else {
                            // Handle error case for missing filePath or drawingName
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Missing file path or drawing name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            ) {
                Text(if (isCreatingNewDrawing) "Save New Drawing" else "Update Drawing")
            }
        }
    }
}




