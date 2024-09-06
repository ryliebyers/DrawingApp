package com.example.myapplication

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentDrawBinding

class DrawFragment : Fragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentDrawBinding.inflate(inflater)

        val viewModel : SimpleViewModel by activityViewModels()
        viewModel.bitmap.observe(viewLifecycleOwner){
            binding.customView.passBitmap(it)
        }

//        binding.customView.setOnTouchListener { view: View, event: MotionEvent ->
//            binding.customView.drawLine(viewModel.color.value!!)
//            true
//
//        }




        // Set up the back button to pop the fragment from the back stack
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root




    }
}

