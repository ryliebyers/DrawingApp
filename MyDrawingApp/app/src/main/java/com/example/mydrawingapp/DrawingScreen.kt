package com.example.mydrawingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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

import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import androidx.compose.ui.platform.testTag


// Needed to convert ImageBitmap to Android Bitmap


data class DrawnPoint(val x: Float, val y: Float, val color: Color, val size: Float)
//data class Line(val start: Offset, val end: Offset, val color: Color = Color.Black, val strokeWidth: Dp = 1.dp)

@Composable
fun DrawingScreen(navController: NavController, drawingId: Int?, viewModel: DrawingViewModel) {
    val context = LocalContext.current
    var drawingName by remember { mutableStateOf("") }
    val drawingPath = remember { mutableStateListOf<DrawnPoint>() }
    val coroutineScope = rememberCoroutineScope()
    val THRESHOLD_DISTANCE = 5f
    val linesPath = remember { mutableStateListOf<Pair<DrawnPoint, DrawnPoint>>() } // Store lines

    // State to hold pen properties
    val pen = remember { Pen() }

    // State to control visibility of pen options
    var showPenOptions by remember { mutableStateOf(false) }

    // Color picker controller from Skydoves library
    val colorPickerController = remember { ColorPickerController() }

    // Add state to hold the ImageBitmap of the saved image
    var savedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var filePath by remember { mutableStateOf<String?>(null) } // Hold the file path for saving/updating
    var canvasWidth by remember { mutableStateOf(900) } // Default canvas width
    var canvasHeight by remember { mutableStateOf(1600) } // Default canvas height

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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(), // Make the row fill the available width
                horizontalArrangement = Arrangement.SpaceBetween, // Distribute the buttons evenly
                verticalAlignment = Alignment.CenterVertically // Align buttons in the center vertically
            ) {
                // Back Button
                Button(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                ) {
                    Text("Back")
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Pen Options Toggle Button
                Button(
                    onClick = {
                        showPenOptions = !showPenOptions
                    }

                ) {
                    Text("Pen")
                }
            }

            if (showPenOptions) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pen Size Slider
                        Text("Pen Size: ${pen.size.value.toInt()}")
                        Slider(
                            value = pen.size.value,
                            onValueChange = { newSize -> pen.changePenSize(newSize) },
                            valueRange = 5f..50f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Color Picker from Skydoves
                        Text("Pick a Pen Color")
                        HsvColorPicker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            controller = colorPickerController,
                            onColorChanged = { hsvColor ->
                                val color = Color(android.graphics.Color.parseColor("#" + hsvColor.hexCode))
                                pen.changePenColor(color)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Box to show the selected color
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(pen.color.value) // Display the selected color
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        // Shape Selector (Circle/Line)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("Circle")
                            RadioButton(
                                selected = !pen.isLineDrawing.value,
                                onClick = { pen.toggleDrawingShape() }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Line")
                            RadioButton(
                                selected = pen.isLineDrawing.value,
                                onClick = { pen.toggleDrawingShape() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dismiss button
                        Button(onClick = { showPenOptions = false }) {
                            Text("Close")
                        }
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .size(canvasWidthDp, canvasHeightDp) // Use correctly converted dp sizes
//                    .testTag("DrawingCanvas")
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startPoint ->
                                // ***Start a new drawing path for the current curve without connecting to the previous one***
                                if (!pen.isLineDrawing.value) {
                                    // Add a new starting point for freehand drawing (reset the starting point)
                                    drawingPath.add(DrawnPoint(startPoint.x, startPoint.y, pen.color.value, pen.size.value))
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (pen.isLineDrawing.value) {
                                    // Line Drawing Mode
                                    val start = change.position - dragAmount
                                    val end = change.position
                                    linesPath.add(
                                        Pair(
                                            DrawnPoint(start.x, start.y, pen.color.value, pen.size.value),
                                            DrawnPoint(end.x, end.y, pen.color.value, pen.size.value)
                                        )
                                    )
                                } else {
                                    // Freehand Drawing Mode (point drawing)
                                    val newPoint = change.position
                                    if (drawingPath.isNotEmpty()) {
                                        val lastPoint = drawingPath.last()
                                        val distance = hypot(newPoint.x - lastPoint.x, newPoint.y - lastPoint.y)
                                        if (distance > THRESHOLD_DISTANCE) {
                                            val interpolatedPoints = interpolatePoints(lastPoint, newPoint, 3)
                                            interpolatedPoints.forEach { point ->
                                                drawingPath.add(DrawnPoint(point.x, point.y, pen.color.value, pen.size.value))
                                            }
                                        }
                                    } else {
                                        // First point for this new gesture
                                        drawingPath.add(DrawnPoint(newPoint.x, newPoint.y, pen.color.value, pen.size.value))
                                    }
                                }
                            }
                        )
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


