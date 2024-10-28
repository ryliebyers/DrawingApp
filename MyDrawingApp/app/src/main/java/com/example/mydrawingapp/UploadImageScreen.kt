import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.ImageUploader
import kotlinx.coroutines.launch

@Composable
fun UploadImageScreen(viewModel: DrawingViewModel, drawingId: Int) {
    val context = LocalContext.current
    val userId = "user123" // THEN ADD THE LOGIC TO GET THE USERS' IDs
    var imageBitmap: Bitmap? by remember { mutableStateOf(null) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Drawing",
                modifier = Modifier.size(200.dp).padding(bottom = 16.dp)
            )
        }

        Button(onClick = {
            // Fetch the drawing and convert it to a bitmap
            viewModel.viewModelScope.launch {
                val drawing = viewModel.getDrawingById(drawingId)
                drawing?.let {
                    imageBitmap = viewModel.getCurrentDrawing(it)
                } ?: run {
                    message = "No drawing found with ID: $drawingId"
                }
            }
        }) {
            Text("Capture Drawing")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
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
        }) {
            Text("Upload Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = message)
    }
}
