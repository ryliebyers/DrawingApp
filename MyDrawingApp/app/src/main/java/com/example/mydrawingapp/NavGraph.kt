package com.example.drawingapp.ui

import UploadImageScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.drawingapp.ui.screens.LoginScreen
import com.example.mydrawingapp.DrawingScreen
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.AllDrawingsScreen
import com.example.mydrawingapp.SigninScreen
import com.example.mydrawingapp.SplashScreen

@Composable
fun NavGraph(navController: NavHostController, viewModel: DrawingViewModel) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("signin") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("signin"){
            SigninScreen(navController = navController, viewModel = viewModel)
        }
        composable("login") {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        composable("create_drawing") {
            DrawingScreen(navController = navController, drawingId = -1, viewModel = viewModel)
        }
        composable("all_drawings") {
            AllDrawingsScreen(navController = navController,viewModel = viewModel)
        }
        composable("edit_drawing/{drawingId}") { backStackEntry ->
            val drawingId = backStackEntry.arguments?.getString("drawingId")?.toInt() ?: -1
            DrawingScreen(navController = navController, drawingId = drawingId, viewModel = viewModel)
        }

        composable("upload_image/{drawingId}") { backStackEntry ->
            val drawingId = backStackEntry.arguments?.getString("drawingId")?.toInt() ?: -1
            UploadImageScreen(viewModel = viewModel, drawingId = drawingId)
        }


    }
}






