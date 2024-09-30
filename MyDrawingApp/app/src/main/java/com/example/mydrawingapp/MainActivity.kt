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


class MainActivity : ComponentActivity() {
    private val drawingViewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory((application as DrawingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
