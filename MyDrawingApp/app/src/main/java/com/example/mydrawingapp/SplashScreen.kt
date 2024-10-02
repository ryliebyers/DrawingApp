package com.example.mydrawingapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay



@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Load the animation file
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("pencils.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever // Loop the animation indefinitely
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.graphicsLayer(
                scaleX = 2f, // Scale horizontally by 2
                scaleY = 2f  // Scale vertically by 2
            )
        )

        // Start a coroutine to wait before navigating
        LaunchedEffect(Unit) {
            delay(3000)
            onTimeout()
        }
    }
}

