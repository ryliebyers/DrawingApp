package com.example.drawingapp.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun LoginScreen(navController: NavController, viewModel: DrawingViewModel) {
    // Collect drawings from the ViewModel
    val drawings by viewModel.allDrawings.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                navController.navigate("create_drawing")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Drawing")
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (drawings.isNotEmpty()) {
            Text("Edit Existing Drawing", modifier = Modifier.padding(8.dp))


            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {

                items(drawings) { drawing ->
                    Button(
                        onClick = {
                            navController.navigate("edit_drawing/${drawing.id}")
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(drawing.name)
                    }
                }
            }
        } else {

            Text("No drawings available", modifier = Modifier.padding(8.dp))
        }
    }
}

