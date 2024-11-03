package com.example.mydrawingapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
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
import androidx.core.content.FileProvider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.sqrt



// Needed to convert ImageBitmap to Android Bitmap
data class DrawnPoint(val x: Float, val y: Float, val color: Color, val size: Float)
// Marble data class
data class Marble(var x: Float, var y: Float, var vx: Float = 0f, var vy: Float = 0f, var size: Float, var color: Color)



@Composable
fun DrawingScreen(navController: NavController, drawingId: Int?, viewModel: DrawingViewModel) {
    val context = LocalContext.current
    var drawingName by remember { mutableStateOf("") }
    val drawingPath = remember { mutableStateListOf<DrawnPoint>() }
    val coroutineScope = rememberCoroutineScope()
    val THRESHOLD_DISTANCE = 5f
    val linesPath = remember { mutableStateListOf<Pair<DrawnPoint, DrawnPoint>>() } // Store lines
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // State to hold pen properties
    val pen = remember { Pen() }
    val marble = remember { Marble(450f, 800f, size = pen.size.value, color = pen.color.value) }
    var isMarbleMode by remember { mutableStateOf(false) } // State to toggle marble mode
    val sensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val marbleTrail = remember { mutableStateListOf<DrawnPoint>() }
// State to hold the marble's properties
    var marbleSize by remember { mutableStateOf(pen.size.value) }
    var marbleColor by remember { mutableStateOf(pen.color.value) }

    // Variables to hold shake detection data
    var lastShakeTime by remember { mutableStateOf(0L) }
    val SHAKE_THRESHOLD_GRAVITY = 1.1f // Adjust to set shake sensitivity
    val SHAKE_RESET_TIME_MS = 400L     // Minimum time between shakes in milliseconds


    var filePath by remember { mutableStateOf<String?>(null) }
    var canvasWidth by remember { mutableStateOf(900) } // Default canvas width
    var canvasHeight by remember { mutableStateOf(1600) } // Default canvas height


    fun shareImage() {
        if (filePath != null) {
            val imageFile = File(filePath)
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            Toast.makeText(context, "No image to share", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(isMarbleMode) {
        if (isMarbleMode) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val xAcceleration = -event.values[0] // Adjust signs as needed
                    val yAcceleration = event.values[1]

                    // Update marble velocity and position
                    marble.vx += xAcceleration
                    marble.vy += yAcceleration

                    // Apply some damping to simulate friction/resistance
                    marble.vx *= 0.5f
                    marble.vy *= 0.5f

                    // Update the position of the marble
                    marble.x += marble.vx
                    marble.y += marble.vy

                    // Ensure the marble stays within the bounds of the canvas
                    marble.x = marble.x.coerceIn(0f, canvasWidth.toFloat())
                    marble.y = marble.y.coerceIn(0f, canvasHeight.toFloat())


                    val previousX = marble.x
                    val previousY = marble.y

// Use  interpolatePoints function to get a smoother transition
                    val interpolatedOffsets = interpolatePoints(
                        DrawnPoint(previousX, previousY, marble.color, marble.size),
                        Offset(marble.x, marble.y),
                        steps = 5 // Increase this for more smoothness
                    )

// Convert the interpolated offsets to DrawnPoints with the same color and size as the marble
                    val interpolatedPoints = interpolatedOffsets.map { offset ->
                        DrawnPoint(offset.x, offset.y, marble.color, marble.size)
                    }

// Add the interpolated points to the marble trail
                    marbleTrail.addAll(interpolatedPoints)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        } else {
            onDispose { /* Nothing to clean up when not in marble mode */ }
        }
    }


    // State to control visibility of pen options
    var showPenOptions by remember { mutableStateOf(false) }

    // Color picker controller from Skydoves library
    val colorPickerController = remember { ColorPickerController() }

    // Add state to hold the ImageBitmap of the saved image
    var savedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }


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

    // Define the listener to detect shake events
    val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Calculate the acceleration
            val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val currentTime = System.currentTimeMillis()
                // If the shake is detected and enough time has passed since the last shake
                if (currentTime - lastShakeTime > SHAKE_RESET_TIME_MS) {
                    lastShakeTime = currentTime
                    // Increase the pen size by a factor (adjust as needed)
                    pen.changePenSize(pen.size.value + 10f)
                    // Ensure pen size does not exceed the upper bound
                    if (pen.size.value > 50f) {
                        pen.changePenSize(50f)
                    }
                    Toast.makeText(context, "Shake detected! Pen size increased.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    // Register and unregister the shake listener
    DisposableEffect(Unit) {
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(shakeListener)
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
            // Row for buttons (Back, Pen, Share, Import)
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // Black background
                ) {
                    Text("Back", color = Color(0xFFFFD700)) // Gold text
                }


                // Pen Options Button
                Button(
                    onClick = {
                        showPenOptions = !showPenOptions
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Pen", color = Color(0xFFFFD700))
                }

                // Share Button
                Button(
                    onClick = {
                        shareImage() // Share image functionality
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Share", color = Color(0xFFFFD700))
                }

                Button(onClick = {

                    // Navigate to upload image screen with the saved drawing ID
                    navController.navigate("upload_image/$drawingId")
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Upload Image", color = Color(0xFFFFD700))
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

                        // Row of boxes for different shades of black and gray
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly // Distribute the boxes evenly
                        ) {
                            // List of shades (from black to lighter shades of gray)
                            val shades = listOf(
                                Color.Black,
                                Color.DarkGray,
                                Color.Gray,
                                Color.LightGray
                            )

                            // Iterate over each shade and create a clickable box
                            shades.forEach { shade ->
                                Box(
                                    modifier = Modifier
                                        .size(50.dp) // Make it square with a size of 50dp
                                        .background(shade) // Set the background color to the current shade
                                        .clickable { pen.changePenColor(shade) } // Make it clickable to select the color
                                        .border(2.dp, Color.Gray) // Add a border to make it stand out
                                )
                            }
                        }



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

                        // Shape Selector (Circle/Line/Marble)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        )
//                        {
//                            Text("Circle")
//                            RadioButton(
//                                selected = !pen.isLineDrawing.value,
//                                onClick = { pen.toggleDrawingShape() }
//                            )
//                            Spacer(modifier = Modifier.width(16.dp))
//                            Text("Line")
//                            RadioButton(
//                                selected = pen.isLineDrawing.value,
//                                onClick = { pen.toggleDrawingShape() }
//                            )
//                            Spacer(modifier = Modifier.width(16.dp))
//                            Text("Marble")
//                            RadioButton(
//                                selected = isMarbleMode,
//                                onClick = { isMarbleMode = true }
//                            )
//                        }



                        {
                                Text("Circle")
                                RadioButton(
                                    selected = !pen.isLineDrawing.value && !isMarbleMode,
                                    onClick = {
                                        pen.isLineDrawing.value = false // Set to circle mode
                                        isMarbleMode = false // Disable marble mode
                                    },
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Line")
                                RadioButton(
                                    selected = pen.isLineDrawing.value && !isMarbleMode,
                                    onClick = {
                                        pen.isLineDrawing.value = true // Set to line mode
                                        isMarbleMode = false // Disable marble mode
                                    },
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Marble")
//                                RadioButton(
//                                    selected = isMarbleMode,
//                                    onClick = { isMarbleMode = true }
//                                )
                            RadioButton(
                                selected = isMarbleMode,
                                onClick = {
                                    if (!isMarbleMode) {
                                        // Switch to marble mode and apply stored marble properties
                                        isMarbleMode = true
//                                        pen.size.value = marbleSize
//                                        pen.color.value = marbleColor

                                        marbleSize = pen.size.value
                                        marbleColor = pen.color.value

                                    } else {
                                        // Exiting marble mode, save the marble's current properties
                                        marbleSize = pen.size.value
                                        marbleColor = pen.color.value
                                        isMarbleMode = false
                                    }
                                }
                            )
                            }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dismiss button
                        Button(onClick = { showPenOptions = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                            Text("Close", color = Color(0xFFFFD700))
                        }
                    }
                }
            }

            // Drawing Canvas
            Canvas(
                modifier = Modifier
                    .size(canvasWidthDp, canvasHeightDp)
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startPoint ->
                                if (!pen.isLineDrawing.value) {
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
                                    // Freehand Drawing Mode
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
                                        drawingPath.add(DrawnPoint(newPoint.x, newPoint.y, pen.color.value, pen.size.value))
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Draw saved image if available
                savedImageBitmap?.let { image ->
                    drawImage(image = image, topLeft = Offset.Zero)
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

                // Draw the marble and its trail if marble mode is active

                    // Draw the trail of the marble
                    for (point in marbleTrail) {
                        drawCircle(
                            color = marbleColor,
                            radius = marbleSize,
                            center = Offset(point.x, point.y)
                        )

                        if (isMarbleMode) {
                            // Draw the current position of the marble as a circle
                            drawCircle(
                                color = pen.color.value,
                                radius = pen.size.value,
                                center = Offset(marble.x, marble.y)
                            )
                        }
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
                                .background(Color(0xFFD3D3D3))
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

                    // Fill the canvas with a white background
                    canvas.drawColor(android.graphics.Color.WHITE)

                    // Draw existing saved image if editing
                    savedImageBitmap?.let { image ->
                        canvas.drawBitmap(image.asAndroidBitmap(), 0f, 0f, null)
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

                    // Perform the save operation
                    coroutineScope.launch(Dispatchers.IO) {
                        if (isCreatingNewDrawing) {
                            filePath = File(context.filesDir, "$drawingName.png").path
                        }


                        if (filePath != null && drawingName.isNotEmpty() ) {
                            saveDrawing(context, bitmap, drawingName, filePath!!, viewModel, navController, drawingId, currentUserEmail)
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Missing file path or drawing name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(if (isCreatingNewDrawing) "Save New Drawing" else "Update Drawing", color = Color(0xFFFFD700))
            }
        }
    }
}

// Helper function to interpolate points for smoother drawing
private fun interpolatePoints(start: DrawnPoint, end: Offset, steps: Int): List<Offset> {
    val points = mutableListOf<Offset>()
    val dx = (end.x - start.x) / steps
    val dy = (end.y - start.y) / steps
    for (i in 0..steps) {
        points.add(Offset(start.x + dx * i, start.y + dy * i))
    }
    return points
}



