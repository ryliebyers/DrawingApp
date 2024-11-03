package com.example.drawingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.Drawing
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(navController: NavController, viewModel: DrawingViewModel) {
    // Collect drawings of current user from the ViewModel
    val drawings by viewModel.userDrawings.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2)) // Light gray background
            .padding(16.dp)
    ) {
        // Button to create a new drawing
        Button(
            onClick = {
                navController.navigate("create_drawing")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Drawing", color = Color(0xFFFFD700)) // Gold text
        }

        // Button For community Drawings
        Button(
            onClick = {
                navController.navigate("all_drawings")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Community Drawings", color = Color(0xFFFFD700)) // Gold text
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title text for editing existing drawings
        if (drawings.isNotEmpty()) {
            Text(
                "Your Drawings",
                color = Color.Gray, // Gray color
                modifier = Modifier.padding(8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(drawings) { drawing ->
                    Button(
                        onClick = {
                            navController.navigate("edit_drawing/${drawing.id}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(drawing.name, color = Color(0xFFFFD700)) // Gold text
                    }
                }
            }
        } else {
            // Message for no drawings available
            Text(
                "No drawings available",
                color = Color.Gray, // Gray color
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


