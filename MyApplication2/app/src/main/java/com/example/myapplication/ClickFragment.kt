package com.example.myapplication

import android.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentClickBinding
import java.io.File

class ClickFragment : Fragment() {

    // This creates a ViewModel instance that is shared between activities and fragments.
// The 'by activityViewModels()' delegate will create a ViewModel if one doesn't already exist and store it at the activity level.
    private val viewModel: SimpleViewModel by activityViewModels()

    // This variable holds a lambda function (buttonFunction) that can be set later.
// It will be called when specific user actions require a fragment transaction or other logic.
    private var buttonFunction: () -> Unit = {}

    // This function allows setting the buttonFunction lambda externally.
// It can be used to customize what action happens when a button is clicked, such as navigating to a different fragment.
    fun setButtonFunction(newFunc: () -> Unit) {
        buttonFunction = newFunc
    }


    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): // This function is responsible for inflating the fragment layout, displaying recent drawings, and setting up click listeners.
            View {
        // Inflate the layout for this fragment using View Binding.
        // The 'binding' object provides access to all the views in the layout (e.g., buttons, ImageViews, TextViews).
        val binding = FragmentClickBinding.inflate(inflater, container, false)

        // Call the function that displays the most recent drawings in the UI (up to 3).
        // It uses the binding object to update the ImageView and TextView for each recent drawing.
        displayRecentDrawings(binding)

        // Set up a click listener for the "Create New Drawing" button.
        // When the button is clicked, it triggers the 'createNewDrawing()' function, which starts a new drawing session.
        binding.createNewDrawing.setOnClickListener {
            createNewDrawing()
        }

        // Set up a click listener for the "Edit Existing Drawing" button.
        // When the button is clicked, it triggers the 'editExistingDrawing()' function, allowing the user to select and edit an existing drawing.
        binding.editExistingDrawing.setOnClickListener {
            editExistingDrawing(binding)
        }

        // Return the root view of the inflated layout, which will be displayed on the screen.
        return binding.root
    }


    // This function initializes the process of creating a new drawing by setting up a blank canvas and resetting the current drawing name.
    private fun createNewDrawing() {

        // Create a new blank Bitmap with dimensions of 800x800 pixels and a configuration of ARGB_8888.
        // ARGB_8888 is a standard bitmap format where each pixel has 4 channels (Alpha, Red, Green, Blue) and 8 bits per channel.
        val newDrawingBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)

        // Pass the newly created blank Bitmap to the ViewModel.
        // This sets the ViewModel to use the new blank canvas as the active drawing area in the drawing fragment.
        viewModel.setBitmap(newDrawingBitmap)

        // Set the current drawing name in the ViewModel to null, indicating that this is a new drawing without a name yet.
        // This ensures that if the user saves the drawing, they will be prompted for a new name.
        viewModel.currentDrawingName = null

        // Call the buttonFunction to trigger navigation to the DrawFragment.
        // This is a lambda function that was previously set to handle navigation when creating or editing a drawing.
        buttonFunction()
    }



    // This function allows the user to select and edit an existing drawing.
    // It retrieves all saved drawings, presents them in a dialog for selection, and loads the selected drawing for editing.
    private fun editExistingDrawing(binding: FragmentClickBinding) {

        // Fetch all the saved drawings from internal storage via the ViewModel.
        // This returns a list of pairs, where the first element is the drawing name and the second is the drawing's bitmap.
        val allDrawings = viewModel.getAllDrawings(requireContext())

        // Extract the names of all the drawings (first element of the pairs) into a string array.
        // These names will be displayed in a dialog for the user to select from.
        val drawingNames = allDrawings.map { it.first }.toTypedArray()

        // Check if there are any drawings available to edit.
        if (drawingNames.isNotEmpty()) {

            // Create an AlertDialog to show a list of drawing names for the user to choose from.
            AlertDialog.Builder(requireContext())
                .setTitle("Select a Drawing to Edit") // Set the dialog title
                .setItems(drawingNames) { dialog, which -> // Set up the list of drawing names

                    // When the user selects a drawing, get the name of the selected drawing.
                    val selectedDrawingName = drawingNames[which]

                    // Use the selected drawing name to fetch the corresponding bitmap from the ViewModel.
                    val selectedDrawing = viewModel.getDrawingByName(requireContext(), selectedDrawingName)

                    // Check if the selected drawing was successfully loaded from storage.
                    if (selectedDrawing != null) {

                        // Create a mutable copy of the bitmap for editing purposes.
                        // The copy allows changes to be made to the bitmap without affecting the original.
                        val editedBitmap = selectedDrawing.copy(Bitmap.Config.ARGB_8888, true)

                        // Set the copied bitmap in the ViewModel as the current drawing to be displayed and edited.
                        viewModel.setBitmap(editedBitmap)

                        // Trigger the buttonFunction to navigate to the DrawFragment, where the user can edit the drawing.
                        buttonFunction()
                    } else {

                        // If the drawing could not be found (e.g., it was deleted or corrupted), show a message to the user.
                        Toast.makeText(requireContext(), "Drawing not found, removing from list.", Toast.LENGTH_SHORT).show()

                        // Remove the missing drawing from the ViewModel's records and storage.
                        viewModel.removeDrawingByName(selectedDrawingName)

                        // Refresh the recent drawings displayed in the UI to reflect the removal of the corrupt drawing.
                        displayRecentDrawings(binding)
                    }
                }
                .show() // Display the AlertDialog to the user
        } else {

            // If no drawings are available, display a message informing the user.
            Toast.makeText(context, "No drawings available to edit", Toast.LENGTH_SHORT).show()
        }
    }
    
    // This function updates the UI to display the most recent drawings in the respective ImageViews and TextViews.
    private fun displayRecentDrawings(binding: FragmentClickBinding) {

        // Fetch all the saved drawings from the ViewModel using the context (internal storage).
        // This returns a list of all drawings saved in the app's storage, with their names and bitmap previews.
        val recentDrawings = viewModel.getAllDrawings(requireContext())

        // Take the last 3 drawings from the list (the most recent ones) and reverse their order.
        // 'takeLast(3)' gets the last 3 elements from the list, and 'reversed()' ensures they appear in reverse order
        // (so the most recently saved drawing is shown first).
        val sortedRecentDrawings = recentDrawings.takeLast(3).reversed()

        // Update the preview for the first recent drawing (or "No Drawing" if it doesn't exist).
        // 'getOrNull(0)' gets the first drawing in the sorted list (if it exists) or returns null.
        updateRecentDrawingPreview(binding.recentDrawing1Preview, binding.recentDrawing1Name, sortedRecentDrawings.getOrNull(0))

        // Update the preview for the second recent drawing (or "No Drawing" if it doesn't exist).
        updateRecentDrawingPreview(binding.recentDrawing2Preview, binding.recentDrawing2Name, sortedRecentDrawings.getOrNull(1))

        // Update the preview for the third recent drawing (or "No Drawing" if it doesn't exist).
        updateRecentDrawingPreview(binding.recentDrawing3Preview, binding.recentDrawing3Name, sortedRecentDrawings.getOrNull(2))
    }



    // This function updates the ImageView and TextView to display a drawing preview and its name.
    private fun updateRecentDrawingPreview(imageView: ImageView, textView: TextView, drawing: Pair<String, Bitmap>?) {

        // If a valid drawing (non-null) is passed, display the bitmap and set the drawing name.
        if (drawing != null) {
            // Set the ImageView to display the Bitmap (the visual representation of the drawing).
            // "drawing.second" accesses the Bitmap in the Pair, as the second element holds the image.
            imageView.setImageBitmap(drawing.second)

            // Set the TextView to show the name of the drawing.
            // "drawing.first" accesses the drawing name in the Pair, as the first element holds the name.
            textView.text = drawing.first

        } else {
            // If no valid drawing is passed ( it's null), indicate that no drawing is available.
            // Set the TextView to show "No Drawing" as a fallback message when no drawing is found.
            textView.text = "No Drawing"

            // Clear the ImageView, setting it to null, to ensure no previous images are shown.
            // This ensures that the image view is empty when no drawing exists.
            imageView.setImageBitmap(null)
        }
    }
}
