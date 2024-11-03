package com.example.mydrawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.NavGraph
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.ui.theme.DrawingAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val drawingViewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory((application as DrawingApplication).repository)
    }

    // Initialize FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        // Add the AuthStateListener if needed to log or perform actions on auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            auth.currentUser?.let { currentUser ->
                val currentUserEmail = currentUser.email ?: ""
                // Log or use the current user's email if needed
                println("Logged-in user email: $currentUserEmail")
            } ?: run {
                // Handle user logout or no user signed in
                println("No user is currently signed in.")
            }
        }

        setContent {
            DrawingAppTheme {
                val navController = rememberNavController()
                Surface {
                    NavGraph(navController = navController, viewModel = drawingViewModel)
                }
            }
        }
    }
}