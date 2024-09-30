package com.example.drawingapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.drawingapp.ui.screens.LoginScreen
import com.example.mydrawingapp.DrawingScreen
import com.example.drawingapp.viewmodel.DrawingViewModel
@Composable
fun NavGraph(navController: NavHostController, viewModel: DrawingViewModel) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable("create_drawing") {
            DrawingScreen(navController = navController, drawingId = -1, viewModel = viewModel)
        }
        composable("edit_drawing/{drawingId}") { backStackEntry ->
            val drawingId = backStackEntry.arguments?.getString("drawingId")?.toInt() ?: -1
            DrawingScreen(navController = navController, drawingId = drawingId, viewModel = viewModel)
        }
    }
}



