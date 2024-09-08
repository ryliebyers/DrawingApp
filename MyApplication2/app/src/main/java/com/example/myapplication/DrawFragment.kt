package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentDrawBinding
import android.widget.ArrayAdapter
import android.widget.TextView

class DrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawBinding
    private var isSeekBarVisible = false  // Flag to track if SeekBar is visible

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawBinding.inflate(inflater)

        val viewModel: SimpleViewModel by activityViewModels()
        viewModel.bitmap.observe(viewLifecycleOwner) {
            binding.customView.passBitmap(it)
        }


        // Handle color picker button click
        binding.btnColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        // Toggle between STROKE and FILL pen style
        binding.btnToggleStyle.setOnClickListener {
            binding.customView.togglePenStyle()
        }

        // Toggle the visibility of SeekBar when "Size" button is clicked
        binding.btnSize.setOnClickListener {
            isSeekBarVisible = !isSeekBarVisible  // Toggle the visibility flag
            binding.seekBarPenSize.visibility = if (isSeekBarVisible) View.VISIBLE else View.GONE
        }

        // SeekBar to adjust pen size
        binding.seekBarPenSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.customView.setPenSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return binding.root
    }

    // Function to show a custom color picker dialog
    private fun showColorPickerDialog() {
        val colors = arrayOf(
            "Red", "Blue", "Green", "Yellow", "Black", "Purple"
        )
        val colorValues = arrayOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.parseColor("#9C27B0")
        )

        // Create an ArrayAdapter with custom text color
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                // Set the text color to match the corresponding color value
                view.setTextColor(colorValues[position])
                return view
            }
        }

        // Build an AlertDialog to show the color options
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pick a Color")
        builder.setAdapter(adapter) { dialog, which ->
            // Set the selected color to the pen
            binding.customView.setPenColor(colorValues[which])
        }

        builder.show()
    }
}