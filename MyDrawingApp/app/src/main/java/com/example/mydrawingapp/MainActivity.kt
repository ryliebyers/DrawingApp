package com.example.mydrawingapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.NavGraph
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.mydrawingapp.ui.theme.DrawingAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : ComponentActivity() {
    private val drawingViewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory((application as DrawingApplication).repository)
    }

    // Initialize FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
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



@Composable
fun AuthNavGraph(navController: NavController, viewModel: DrawingViewModel) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser: FirebaseUser? = firebaseAuth.currentUser

    // Redirect to login screen if user is not logged in
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // The main navigation graph for your app
    NavGraph(navController = navController as NavHostController, viewModel = viewModel)
}

