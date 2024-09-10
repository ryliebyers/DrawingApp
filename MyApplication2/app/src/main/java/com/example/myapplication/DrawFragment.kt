package com.example.myapplication

//needs this for setcontentview
import android.content.Context
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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


        //val viewModel: SimpleViewModel by activityViewModels()
        viewModel.bitmap.observe(viewLifecycleOwner) {
            binding.customView.passBitmap(it)
        }

        // Load the current bitmap into the CustomView for drawing
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

        // Handle Clear Button click (to clear the drawing)
        binding.btnClearDrawing.setOnClickListener {
            clearDrawing()
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


    // Function to save current drawing with an option to bypass the name prompt
    private fun saveCurrentDrawing(showNamePrompt: Boolean = true) {
        val existingDrawingName = viewModel.currentDrawingName

        if (existingDrawingName != null) {
            // If the drawing already has a name, just save it without showing any pop-up
            val currentBitmap = binding.customView.getBitmap()
            val filePath = saveDrawingToInternalStorage(requireContext(), existingDrawingName, currentBitmap)

            if (filePath != null) {
                Toast.makeText(requireContext(), "Changes saved to $existingDrawingName!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
            }
        } else if (showNamePrompt) {
            // Show the name prompt if needed
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
        } else {
            // Automatically save with "No-Name" if no name was previously given
            val noName = "No-Name"
            val currentBitmap = binding.customView.getBitmap()
            val filePath = saveDrawingToInternalStorage(requireContext(), noName, currentBitmap)

            if (filePath != null) {
                viewModel.currentDrawingName = noName
                Toast.makeText(requireContext(), "Drawing saved as $noName!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
            }
        }
    }




    // This function saves a drawing (in the form of a bitmap) to internal storage and returns the file path if successful.
    fun saveDrawingToInternalStorage(context: Context, drawingName: String, bitmap: Bitmap): String? {

        // Get the internal storage directory where the app's files are stored.
        // 'context.filesDir' returns the directory where files can be saved within the app's internal storage.
        val directory = context.filesDir

        // Create a new File object representing the PNG file where the drawing will be saved.
        // The file is named after the drawing (e.g., "drawingName.png").
        val file = File(directory, "$drawingName.png")

        // Use a try-catch block to handle potential file writing errors.
        return try {
            // Create a FileOutputStream to write the bitmap to the file.
            val outputStream = FileOutputStream(file)

            // Compress the bitmap into PNG format and write it to the output stream.
            // The second parameter (100) indicates the compression quality (100 = best quality).
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            // Flush and close the output stream to ensure the data is fully written to the file.
            outputStream.flush()
            outputStream.close()

            // Return the absolute path of the saved file (indicating successful saving).
            file.absolutePath

        } catch (e: IOException) {
            // If an IOException occurs during the file writing process, print the stack trace for debugging purposes.
            e.printStackTrace()

            // Return null to indicate that the file could not be saved.
            null
        }
    }


    override fun onPause() {
        super.onPause()
        // Ensure the drawing is saved in the ViewModel before the fragment is paused (e.g., on rotation)
        val currentBitmap = binding.customView.getBitmap()
        viewModel.setBitmap(currentBitmap)
        saveCurrentDrawing()
    }

    // Function to clear the drawing
    private fun clearDrawing() {
        // Create a new blank bitmap to clear the drawing
        val blankBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        binding.customView.passBitmap(blankBitmap) // Pass the new blank bitmap to the custom view

        // Also update the ViewModel to keep it consistent
        viewModel.setBitmap(blankBitmap)
    }



    fun saveNoNameDrawing() {
        // Save the current drawing with "No-Name" directly, without the prompt
        saveCurrentDrawing(showNamePrompt = false)

        // Call the activity's default back behavior to go back
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }


}




