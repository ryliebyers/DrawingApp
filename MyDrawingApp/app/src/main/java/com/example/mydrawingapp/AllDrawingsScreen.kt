package com.example.mydrawingapp

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
import androidx.compose.material.Button

@Composable
fun AllDrawingsScreen(navController: NavController, viewModel: DrawingViewModel) {
    val allDrawings by viewModel.allDrawings.collectAsState(initial = emptyList())


    Column(modifier = Modifier.fillMaxSize()) {
        // Button to navigate back to login
        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Back")
        }

        LazyColumn {
            items(allDrawings) { drawing ->
                DrawingItem(drawing = drawing)
            }
        }
    }
}

@Composable
fun DrawingItem(drawing: Drawing) {
    val userEmail: String = UserSession.email

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = userEmail, style = MaterialTheme.typography.body1)
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


