package com.example.mydrawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.hypot

data class DrawnPoint(val x: Float, val y: Float, val color: Color, val size: Float)

@Composable
fun DrawingScreen(navController: NavController, drawingId: Int?, viewModel: DrawingViewModel) {
    val context = LocalContext.current
    var drawingName by remember { mutableStateOf("") }
    val drawingPath = remember { mutableStateListOf<DrawnPoint>() }
    val coroutineScope = rememberCoroutineScope()
    val THRESHOLD_DISTANCE = 5f

    // State to hold pen properties
    var penSize by remember { mutableStateOf(10f) } // Default pen size
    val penColor = remember { mutableStateOf(Color.Black) }

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

    // BackHandler to handle the device's back button
    BackHandler {
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
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
            // Back Button
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Back")
            }

            // Pen Size Slider
            Text("Pen Size: ${penSize.toInt()}")
            Slider(
                value = penSize,
                onValueChange = { newSize -> penSize = newSize },
                valueRange = 5f..50f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pen Color Box
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(color)
                            .clickable {
                                penColor.value = color
                            }
                    )
                }
            }

            // Canvas for Drawing
            Canvas(
                modifier = Modifier
                    .size(canvasWidth.dp, canvasHeight.dp) // Match the canvas size to the bitmap size
                    .background(Color.White)
//                    .pointerInput(Unit) {
//                        detectDragGestures { change, _ ->
//                            drawingPath.add(
//                                DrawnPoint(
//                                    x = change.position.x,
//                                    y = change.position.y,
//                                    color = penColor.value,
//                                    size = penSize
//                                )
//                            )
//                        }
//                    }


                    .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newPoint = change.position
                    if (drawingPath.isNotEmpty()) {
                        val lastPoint = drawingPath.last()
                        val distance = hypot(newPoint.x - lastPoint.x, newPoint.y - lastPoint.y)
                        // Use interpolation only if the distance exceeds the threshold
                        if (distance > THRESHOLD_DISTANCE) {
                            val interpolatedPoints = interpolatePoints(lastPoint, newPoint, 3) // You can adjust the number of steps
                            interpolatedPoints.forEach { point ->
                                drawingPath.add(
                                    DrawnPoint(
                                        x = point.x,
                                        y = point.y,
                                        color = penColor.value,
                                        size = penSize
                                    )
                                )
                            }
                        }
                    } else {
                        // If no points exist, add the initial point
                        drawingPath.add(
                            DrawnPoint(
                                x = newPoint.x,
                                y = newPoint.y,
                                color = penColor.value,
                                size = penSize
                            )
                        )
                    }
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

                // Draw each point with its own color and size
                for (point in drawingPath) {
                    drawCircle(
                        color = point.color,
                        radius = point.size,
                        center = androidx.compose.ui.geometry.Offset(point.x, point.y)
                    )
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
                        canvas.drawCircle(point.x, point.y, point.size, android.graphics.Paint().apply { color = point.color.toArgb() })
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


private fun interpolatePoints(start: DrawnPoint, end: Offset, steps: Int): List<Offset> {
    val points = mutableListOf<Offset>()
    val dx = (end.x - start.x) / steps
    val dy = (end.y - start.y) / steps
    for (i in 0..steps) {
        points.add(Offset(start.x + dx * i, start.y + dy * i))
    }
    return points
}
