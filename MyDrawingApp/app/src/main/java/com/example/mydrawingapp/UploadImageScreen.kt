import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
@Composable
fun UploadImageScreen(viewModel: DrawingViewModel, drawingId: Int) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid // Retrieve the logged-in user's ID
    var imageBitmap: Bitmap? by remember { mutableStateOf(null) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)) // Light gray background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Center align items
        verticalArrangement = Arrangement.Center // Center everything vertically
    ) {
        // Display the image, centered with a larger size and padding for visual separation
        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Drawing",
                modifier = Modifier
                    .size(300.dp) // Larger size for better visibility
                    .padding(bottom = 16.dp)
                    .border(2.dp, Color.Gray, shape = RoundedCornerShape(8.dp)) // Border and rounded corners
            )
        } ?: Text("No drawing captured yet", color = Color.Gray) // Placeholder text

        Spacer(modifier = Modifier.height(16.dp))

        // Capture Drawing Button
        Button(
            onClick = {
                // Fetch the drawing and convert it to a bitmap
                viewModel.viewModelScope.launch {
                    val drawing = viewModel.getDrawingById(drawingId)
                    drawing?.let {
                        imageBitmap = viewModel.getCurrentDrawing(it)
                    } ?: run {
                        message = "No drawing found with ID: $drawingId"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture Drawing", color = Color(0xFFFFD700)) // Gold text
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upload Image Button
        Button(
            onClick = {
                if (userId != null) {
                    imageBitmap?.let { bitmap ->
                        viewModel.viewModelScope.launch {
                            ImageUploader.uploadImage(
                                context = context,
                                bitmap = bitmap,
                                userId = userId,
                                onSuccess = { message = "Image uploaded successfully: $it" },
                                onError = { message = "Upload failed: $it" }
                            )
                        }
                    } ?: run {
                        message = "No image to upload. Capture a drawing first."
                    }
                } else {
                    message = "User not logged in. Please log in to upload images."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Image", color = Color(0xFFFFD700)) // Gold text
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display the status message at the bottom
        Text(text = message, color = Color.Gray, modifier = Modifier.padding(8.dp))
    }
}


