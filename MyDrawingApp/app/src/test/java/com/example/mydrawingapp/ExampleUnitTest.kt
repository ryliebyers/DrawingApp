package com.example.mydrawingapp

import org.junit.Assert.*
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.viewmodel.DrawingViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(JUnit4::class)
class SaveImageTest {

    private lateinit var viewModel: DrawingViewModel
    private lateinit var navController: NavController
    private lateinit var context: Context

    @Before
    fun setUp() {
        viewModel = Mockito.mock(DrawingViewModel::class.java)
        navController = Mockito.mock(NavController::class.java)
        context = Mockito.mock(Context::class.java)
    }

//    @Test
//    fun testSaveDrawing(): Unit = runBlocking {
//        // Prepare test data
//        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
//        val name = "Test Drawing"
//        val filePath = "/test/path/drawing.png"
//
//        // Mock ViewModel behavior
//        Mockito.doNothing().`when`(viewModel).insertDrawing(Mockito.any())
//
//        // Call the saveDrawing function
//        saveDrawing(context, bitmap, name, filePath, viewModel, navController, null)
//
//        // Verify that the insertDrawing method was called
//        verify(viewModel).insertDrawing(Mockito.any())
//    }

    @Test
    fun testPullUpSavedDrawing() = runBlocking {
        // Assuming you have a saved drawing to pull up
        val drawingId = 1
        val expectedName = "Test Drawing"
        val expectedFilePath = "/test/path/drawing.png"
        val expectedDrawing = Drawing(name = expectedName, filePath = expectedFilePath)

        // Mock ViewModel behavior to return a drawing
        `when`(viewModel.getDrawingById(drawingId)).thenReturn(expectedDrawing)

        // Set up state variables
        var drawingName = ""
        var filePath: String? = null

        // Simulate loading a drawing
        val drawing = viewModel.getDrawingById(drawingId)
        drawing?.let {
            drawingName = it.name
            filePath = it.filePath
        }

        // Verify the loaded values
        assertEquals(expectedName, drawingName)
        assertEquals(expectedFilePath, filePath)
    }

//    @Test
//    fun testSaveDrawingHandlesError() = runBlocking(){
//        // Prepare test data
//        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
//        val name = "Test Drawing"
//        val filePath = null // simulate error case
//
//        // Call the saveDrawing function
//        saveDrawing(context, bitmap, name, filePath, viewModel, navController, null)
//
//        // Verify that no insert operation is called due to error
//        verify(viewModel, Mockito.never()).insertDrawing(Mockito.any())
//    }

}






//@RunWith(JUnit4::class)
//class DrawingScreenTest {
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Composable
//    @Test
//    fun testDrawingFunctionality() {
//        // Set up your view model and any required state
//        val viewModel = DrawingViewModel() // Mock or provide a real instance
//        val navController = rememberNavController() // Mock or create a NavController
//
//        // Set the composable under test
//        composeTestRule.setContent {
//            DrawingScreen(navController, drawingId = -1, viewModel = viewModel)
//        }
//
//        // Simulate user drawing on the canvas
//        composeTestRule.onNodeWithTag("DrawingCanvas") // Assign a test tag to your Canvas
//            .performTouchInput {
//                // Simulate touch down to start drawing
//                down(Offset(100f, 100f))
//                // Simulate drag to draw
//                moveTo(Offset(200f, 200f))
//                moveTo(Offset(300f, 300f))
//                // Simulate touch up to finish drawing
//                up()
//            }
//
//        // Validate that the expected drawing operations occurred
//        // This might require inspecting the ViewModel or state changes
//        // Example: Check if a point was added to the drawingPath
//        assert(viewModel.drawingPath.isNotEmpty()) // Adjust based on your actual data structure
//    }
//}