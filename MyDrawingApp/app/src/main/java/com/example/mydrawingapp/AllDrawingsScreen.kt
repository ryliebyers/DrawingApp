package com.example.mydrawingapp

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingapp.viewmodel.DrawingViewModel
import coil.compose.rememberImagePainter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

//@Composable
//fun AllDrawingsScreen(navController: NavController, viewModel: DrawingViewModel) {
//    val _allDrawings by viewModel.serverDrawings.collectAsState(initial = emptyList())
//    val serverDrawingsFlow: Flow<List<Drawing>> = viewModel.serverDrawings
//    @Composable
//    fun WebViewScreen() {
//        val context = LocalContext.current
//
//        AndroidView(
//            factory = { context ->
//                WebView(context).apply {
//                    webViewClient = WebViewClient() // Handle loading URLs
//                    settings.javaScriptEnabled = true // Enable JavaScript if needed
//                    loadUrl("http://0.0.0.0:8081/static/images.html") // Load your URL
//                }
//            },
//            update = { webView ->
//                webView.loadUrl("http://0.0.0.0:8081/static/images.html") // Reload URL if needed
//            }
//        )
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        // Button to navigate back to login
//        Button(
//            onClick = {
//                navController.navigate("login") {
//                    popUpTo("splash") { inclusive = true }
//                }
//            },
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(text = "Back")
//        }
//
//        LazyColumn {
//            items(_allDrawings) { drawing ->
//                DrawingItem(drawing = drawing)
//            }
//        }
//    }
//}
//
//@Composable
//fun DrawingItem(drawing: Drawing) {
//    val userEmail: String = UserSession.email
//
//    Column(modifier = Modifier.padding(8.dp)) {
//        Text(text = userEmail, style = MaterialTheme.typography.body1)
//        Text(text = "Drawing Name: ${drawing.name}", style = MaterialTheme.typography.body2)
//        Image(
//            painter = rememberAsyncImagePainter(drawing.filePath),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color.Gray)
//        )
//    }
//}

@Composable
fun AllDrawingsScreen(navController: NavController, viewModel: DrawingViewModel) {
    // Collect drawings from the viewModel
    val allDrawings by viewModel.serverDrawings.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        // Button to navigate back to login
        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier
                .fillMaxWidth() // Make the button fill the available width
                .padding(16.dp) // Add padding around the button
        ) {
            Text(text = "Back", color = Color(0xFFFFD700)) // Gold text
        }

        // Display the WebView for the server URL
        WebViewScreen()

        // Display the list of drawings
        LazyColumn {
            items(allDrawings) { drawing ->
                DrawingItem(drawing = drawing)
            }
        }
    }
}

@Composable
fun WebViewScreen() {
    val context = LocalContext.current

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient() // Handle loading URLs
                settings.javaScriptEnabled = true // Enable JavaScript if needed
                loadUrl("http://10.0.2.2:8081/static/images.html") // Load your URL
            }
        },
        update = { webView ->
            webView.loadUrl("http://10.0.2.2:8081/static/images.html") // Reload URL if needed
        }
    )
}

@Composable
fun DrawingItem(drawing: Drawing) {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = currentUserEmail, style = MaterialTheme.typography.body1)
        Text(text = "Drawing Name: ${drawing.name}", style = MaterialTheme.typography.body2)
        Image(
            painter = rememberAsyncImagePainter(drawing.filePath),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray)
        )
    }
}

