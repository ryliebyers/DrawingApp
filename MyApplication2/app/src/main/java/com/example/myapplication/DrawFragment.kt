package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentDrawBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawBinding
    private var isSeekBarVisible = false
    private val viewModel: SimpleViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawBinding.inflate(inflater)

        viewModel.bitmap.observe(viewLifecycleOwner) {
            binding.customView.passBitmap(it)
        }

        // Handle Save Button click
        binding.btnSaveDrawing.setOnClickListener {
            saveCurrentDrawing()
        }

        // Handle color picker button click
        binding.btnColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        binding.btnShapePicker.setOnClickListener {
            showShapePickerDialog() // Show the dialog to select shapes
        }

        // Handle Clear Button click (to clear the drawing)
        binding.btnClearDrawing.setOnClickListener {
            clearDrawing()
        }


        // Toggle the visibility of SeekBar when "Size" button is clicked
        binding.btnSize.setOnClickListener {
            isSeekBarVisible = !isSeekBarVisible
            binding.seekBarPenSize.visibility = if (isSeekBarVisible) View.VISIBLE else View.GONE
        }

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

    // Public function to check if the drawing is saved
    fun isDrawingSaved(): Boolean {
        return viewModel.isDrawingSaved
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Intercept the back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()  // Show confirmation dialog before exiting
                }
            }
        )
    }

    // Function to show a custom color picker dialog
    private fun showColorPickerDialog() {
        val colors = arrayOf("Red", "Blue", "Green", "Yellow", "Black", "Purple")
        val colorValues = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.BLACK, Color.parseColor("#9C27B0"))

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(colorValues[position])
                return view
            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pick a Color")
        builder.setAdapter(adapter) { dialog, which ->
            binding.customView.setPenColor(colorValues[which])
        }

        builder.show()
    }

    // Function to show a custom shape picker dialog
    private fun showShapePickerDialog() {
        val shapes = arrayOf("Line", "Rectangle", "Circle")
        val shapeTypes = arrayOf( PenProperties.ShapeType.LINE, PenProperties.ShapeType.RECTANGLE, PenProperties.ShapeType.CIRCLE)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, shapes)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pick a Shape")
        builder.setAdapter(adapter) { dialog, which ->
            // Set the selected shape in the CustomView
            binding.customView.setShape(shapeTypes[which])
        }

        builder.show()
    }

    // Function to save the current drawing
    private fun saveCurrentDrawing(showNamePrompt: Boolean = true) {
        val existingDrawingName = viewModel.currentDrawingName

        if (existingDrawingName != null) {
            val currentBitmap = binding.customView.getBitmap()
            val filePath = saveDrawingToInternalStorage(requireContext(), existingDrawingName, currentBitmap)

            if (filePath != null) {
                Toast.makeText(requireContext(), "Changes saved to $existingDrawingName!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
            }
        } else if (showNamePrompt) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Enter Drawing Name")

            val input = EditText(requireContext())
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, _ ->
                val drawingName = input.text.toString()
                if (drawingName.isNotBlank()) {
                    val currentBitmap = binding.customView.getBitmap()
                    val filePath = saveDrawingToInternalStorage(requireContext(), drawingName, currentBitmap)

                    if (filePath != null) {
                        viewModel.currentDrawingName = drawingName
                        Toast.makeText(requireContext(), "Drawing saved as $drawingName!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    // This function saves a drawing (in the form of a bitmap) to internal storage and returns the file path if successful.
    fun saveDrawingToInternalStorage(context: Context, drawingName: String, bitmap: Bitmap): String? {
        val directory = context.filesDir
        val file = File(directory, "$drawingName.png")

        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPause() {
        super.onPause()
        val currentBitmap = binding.customView.getBitmap()
        viewModel.setBitmap(currentBitmap)
    }

    // Function to clear the drawing
    private fun clearDrawing() {
        val blankBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        binding.customView.passBitmap(blankBitmap)
        viewModel.setBitmap(blankBitmap)
    }

    fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exit Without Saving")
        builder.setMessage("You will lose your unsaved work. Are you sure you want to go back?")

        // If the user clicks "OK", dismiss the dialog and navigate back
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog
            requireActivity().supportFragmentManager.popBackStack() // Go back to the previous fragment
        }

        // If the user clicks "Cancel", just dismiss the dialog and stay on the drawing page
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Close the dialog if "Cancel" is pressed
        }

        builder.show()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.currentLayoutId?.let { layoutId ->
            updateUILayout(layoutId)
        }
    }

    fun updateUILayout(layoutId: Int) {
        viewModel.currentLayoutId = layoutId
        view?.let {
//            // Remove the old view and inflate the new layout
            (it as ViewGroup).removeAllViews()
            LayoutInflater.from(context).inflate(layoutId, it, true)
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                updateUILayout(R.layout.fragment_draw)            }
            Configuration.ORIENTATION_PORTRAIT -> {
                updateUILayout(R.layout.fragment_draw)            }
            }

        }


} //end of fragment class



