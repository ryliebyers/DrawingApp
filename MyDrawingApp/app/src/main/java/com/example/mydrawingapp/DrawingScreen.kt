package com.example.mydrawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.hypot
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.asAndroidBitmap
// Needed to convert ImageBitmap to Android Bitmap


data class DrawnPoint(val x: Float, val y: Float, val color: Color, val size: Float)
data class Line(val start: Offset, val end: Offset, val color: Color = Color.Black, val strokeWidth: Dp = 1.dp)

@Composable
fun DrawingScreen(navController: NavController, drawingId: Int?, viewModel: DrawingViewModel) {
    val context = LocalContext.current
    var drawingName by remember { mutableStateOf("") }
    val drawingPath = remember { mutableStateListOf<DrawnPoint>() }
    val coroutineScope = rememberCoroutineScope()
    val THRESHOLD_DISTANCE = 5f
    var isLineDrawing by remember { mutableStateOf(false) }
    val linesPath = remember { mutableStateListOf<Pair<DrawnPoint, DrawnPoint>>() } // Store lines

    // State to hold pen properties
    var penSize by remember { mutableStateOf(10f) } // Default pen size
    val penColor = remember { mutableStateOf(Color.Black) }

    // Add state to hold the ImageBitmap of the saved image
    var savedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var filePath by remember { mutableStateOf<String?>(null) } // Hold the file path for saving/updating
    var canvasWidth by remember { mutableStateOf(800) } // Default canvas width
    var canvasHeight by remember { mutableStateOf(800) } // Default canvas height

    val density = LocalDensity.current

    // Flag to check if the user is creating a new drawing
    val isCreatingNewDrawing = drawingId == -1

    // Convert pixel to dp
    val canvasWidthDp = remember(savedImageBitmap) {
        with(density) { (savedImageBitmap?.width ?: canvasWidth).toDp() }
    }

    val canvasHeightDp = remember(savedImageBitmap) {
        with(density) { (savedImageBitmap?.height ?: canvasHeight).toDp() }
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Circle")
                RadioButton(
                    selected = !isLineDrawing,
                    onClick = { isLineDrawing = false }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Line")
                RadioButton(
                    selected = isLineDrawing,
                    onClick = { isLineDrawing = true }
                )
            }
            Canvas(
                modifier = Modifier
                    .size(canvasWidthDp, canvasHeightDp) // Use correctly converted dp sizes
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            if (isLineDrawing) {
                                change.consume()

                                // Line Drawing Mode
                                val start = change.position - dragAmount
                                val end = change.position
                                linesPath.add(
                                    Pair(
                                        DrawnPoint(start.x, start.y, penColor.value, penSize),
                                        DrawnPoint(end.x, end.y, penColor.value, penSize)
                                    )
                                )
                            } else {
                                // Freehand Drawing Mode
                                val newPoint = change.position
                                if (drawingPath.isNotEmpty()) {
                                    val lastPoint = drawingPath.last()
                                    val distance = hypot(newPoint.x - lastPoint.x, newPoint.y - lastPoint.y)
                                    if (distance > THRESHOLD_DISTANCE) {
                                        val interpolatedPoints = interpolatePoints(lastPoint, newPoint, 3)
                                        interpolatedPoints.forEach { point ->
                                            drawingPath.add(DrawnPoint(point.x, point.y, penColor.value, penSize))
                                        }
                                    }
                                } else {
                                    drawingPath.add(DrawnPoint(newPoint.x, newPoint.y, penColor.value, penSize))
                                }
                            }
                        }
                    }
            ) {
                // Draw the saved image without scaling
                savedImageBitmap?.let { image ->
                    drawImage(
                        image = image,
                        topLeft = Offset.Zero // Draw at the top-left corner
                    )
                }

                // Draw each point with its own color and size
                for (point in drawingPath) {
                    drawCircle(
                        color = point.color,
                        radius = point.size,
                        center = Offset(point.x, point.y)
                    )
                }

                // Draw lines
                for (line in linesPath) {
                    drawLine(
                        color = line.first.color,
                        start = Offset(line.first.x, line.first.y),
                        end = Offset(line.second.x, line.second.y),
                        strokeWidth = line.first.size,
                        cap = StrokeCap.Round
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

            Button(
                onClick = {
                    // Create a new bitmap from the user's drawing
                    val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)

                    // Draw existing saved image if editing
                    savedImageBitmap?.let { image ->
                        canvas.drawBitmap(image.asAndroidBitmap(), 0f, 0f, null) // Use asAndroidBitmap() directly
                    }

                    // Draw freehand points
                    for (point in drawingPath) {
                        canvas.drawCircle(
                            point.x,
                            point.y,
                            point.size,
                            android.graphics.Paint().apply {
                                color = point.color.toArgb()
                                style = android.graphics.Paint.Style.FILL
                            }
                        )
                    }

                    // Draw lines
                    for (line in linesPath) {
                        canvas.drawLine(
                            line.first.x,
                            line.first.y,
                            line.second.x,
                            line.second.y,
                            android.graphics.Paint().apply {
                                color = line.first.color.toArgb()
                                strokeWidth = line.first.size
                                strokeCap = android.graphics.Paint.Cap.ROUND
                            }
                        )
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


