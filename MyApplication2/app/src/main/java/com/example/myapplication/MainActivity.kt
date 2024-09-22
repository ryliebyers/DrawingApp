package com.example.myapplication

import DrawFragment
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels

import com.example.myapplication.databinding.ActivityMainBinding
import android.content.pm.ActivityInfo




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            // Initial fragment transaction to load ClickFragment
            val clickFragment = ClickFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, clickFragment, "click_fragment")
                .commit()

            clickFragment.setButtonFunction {
                val drawFragment = DrawFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, drawFragment, "draw_fragment")

                    .addToBackStack(null)

                    .commit()

            }

        }
    }


    override fun onBackPressed() {
        // Check if the DrawFragment is currently active
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)

        if (fragment is DrawFragment) {
            // Only show the exit confirmation dialog if the drawing has not been saved
            if (!fragment.isDrawingSaved()) {
                fragment.showExitConfirmationDialog()
            } else {
                super.onBackPressed() // Exit if the drawing has already been saved
            }
        } else {
            super.onBackPressed() // Call the default back button behavior
        }
    }







} //end of main


