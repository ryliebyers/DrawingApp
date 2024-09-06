
package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentClickBinding




class ClickFragment : Fragment() {
    private var buttonFunction: () -> Unit = {}

    fun setButtonFunction(newFunc: () -> Unit) {
        buttonFunction = newFunc
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentClickBinding.inflate(inflater, container, false)

        binding.clickMe.setOnClickListener {
            buttonFunction() // Call the function to navigate to DrawFragment
        }

        return binding.root
    }
}
