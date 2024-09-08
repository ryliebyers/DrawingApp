package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
//needs this for setcontentview
import com.example.myapplication.R

class SplashActivity : AppCompatActivity() {

    private val splashDuration: Long = 3000 // Duration of splash screen in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Find the LottieAnimationView and set up the animation
        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation("splash_page.json") // The JSON file in assets
        lottieAnimationView.playAnimation()

        // Transition to MainActivity after the splash duration
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Finish SplashActivity so the user can't go back to it
        }, splashDuration)
    }
}