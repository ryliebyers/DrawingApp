package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels

import com.example.myapplication.databinding.ActivityMainBinding




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}


