package com.example.myapplication
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
import androidx.activity.OnBackPressedCallback

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

    // Function to save the current drawing
    // This function saves the current drawing to internal storage. If the drawing already has a name,
// it will save directly, otherwise, it will prompt the user to enter a name.
    private fun saveCurrentDrawing(showNamePrompt: Boolean = true) {

        // Get the name of the current drawing from the ViewModel.
        val existingDrawingName = viewModel.currentDrawingName

        // If the drawing has an existing name, save it using that name.
        if (existingDrawingName != null) {
            // Get the current drawing (as a bitmap) from the custom drawing view.
            val currentBitmap = binding.customView.getBitmap()

            // Save the bitmap to internal storage using the existing name.
            val filePath = saveDrawingToInternalStorage(requireContext(), existingDrawingName, currentBitmap)

            // Check if the drawing was saved successfully.
            if (filePath != null) {
                // Display a success message to the user.
                Toast.makeText(requireContext(), "Changes saved to $existingDrawingName!", Toast.LENGTH_SHORT).show()
            } else {
                // Display an error message if the save operation failed.
                Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
            }
        }
        // If the drawing does not have a name, prompt the user to enter one.
        else if (showNamePrompt) {
            // Create an AlertDialog to ask the user for a drawing name.
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Enter Drawing Name")

            // Create an EditText input field where the user can type the name.
            val input = EditText(requireContext())
            builder.setView(input) // Set the EditText as the dialog's view.

            // Handle the "OK" button click, which saves the drawing with the entered name.
            builder.setPositiveButton("OK") { dialog, _ ->
                val drawingName = input.text.toString() // Get the entered name.

                // Ensure the entered name is not blank.
                if (drawingName.isNotBlank()) {
                    // Get the current drawing (as a bitmap) from the custom drawing view.
                    val currentBitmap = binding.customView.getBitmap()

                    // Save the bitmap to internal storage using the entered name.
                    val filePath = saveDrawingToInternalStorage(requireContext(), drawingName, currentBitmap)

                    // If the save operation is successful, store the drawing name in the ViewModel.
                    if (filePath != null) {
                        viewModel.currentDrawingName = drawingName
                        Toast.makeText(requireContext(), "Drawing saved as $drawingName!", Toast.LENGTH_SHORT).show()
                    } else {
                        // If the save operation failed, show an error message.
                        Toast.makeText(requireContext(), "Failed to save drawing.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If the name entered is blank, prompt the user to enter a valid name.
                    Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }

            // Handle the "Cancel" button click, which closes the dialog without saving.
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel() // Close the dialog.
            }

            // Show the name prompt dialog to the user.
            builder.show()
        }
    }


    // This function saves a drawing (in the form of a bitmap) to internal storage and returns the file path if successful.
// The drawing is saved as a PNG file in the app's internal storage.
    fun saveDrawingToInternalStorage(context: Context, drawingName: String, bitmap: Bitmap): String? {

        // Define the directory where the drawing will be saved (internal storage directory).
        val directory = context.filesDir

        // Create a File object representing the PNG file named after the drawing.
        val file = File(directory, "$drawingName.png")

        return try {
            // Create a FileOutputStream to write the bitmap data to the file.
            val outputStream = FileOutputStream(file)

            // Compress the bitmap into PNG format and write it to the file output stream.
            // The second parameter (100) indicates the compression quality (100% for best quality).
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            // Flush and close the output stream to ensure all data is written to the file.
            outputStream.flush()
            outputStream.close()

            // Return the absolute file path of the saved drawing if the process succeeds.
            file.absolutePath

        } catch (e: IOException) {
            // If an error occurs during the file saving process, log the exception and return null.
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
}
