//package com.example.mydrawingapp
//
//import org.junit.Assert.*
//import android.content.Context
//import android.graphics.Bitmap
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performTouchInput
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.drawingapp.viewmodel.DrawingViewModel
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertNull
//import junit.framework.TestCase.assertTrue
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//import org.junit.Before
//import org.junit.Rule
//import org.junit.runner.RunWith
//import org.junit.runners.JUnit4
//import org.mockito.Mockito
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.verify
//import org.mockito.Mockito.`when`
//import androidx.lifecycle.viewmodel.compose.viewModel
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(JUnit4::class)
//class SaveImageTest {
//
//    private lateinit var viewModel: DrawingViewModel
//    private lateinit var navController: NavController
//    private lateinit var context: Context
//
//    @Before
//    fun setUp() {
//        viewModel = Mockito.mock(DrawingViewModel::class.java)
//        navController = Mockito.mock(NavController::class.java)
//        context = Mockito.mock(Context::class.java)
//    }
//
////    @Test
////    fun testSaveDrawing(): Unit = runBlocking {
////        // Prepare test data
////        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
////        val name = "Test Drawing"
////        val filePath = "/test/path/drawing.png"
////
////        // Mock ViewModel behavior
////        Mockito.doNothing().`when`(viewModel).insertDrawing(Mockito.any())
////
////        // Call the saveDrawing function
////        saveDrawing(context, bitmap, name, filePath, viewModel, navController, null)
////
////        // Verify that the insertDrawing method was called
////        verify(viewModel).insertDrawing(Mockito.any())
////    }
//
//    @Test
//    fun testPullUpSavedDrawing() = runBlocking {
//        // Assuming you have a saved drawing to pull up
//        val drawingId = 1
//        val expectedName = "Test Drawing"
//        val expectedFilePath = "/test/path/drawing.png"
//        val expectedDrawing = Drawing(name = expectedName, filePath = expectedFilePath)
//
//        // Mock ViewModel behavior to return a drawing
//        `when`(viewModel.getDrawingById(drawingId)).thenReturn(expectedDrawing)
//
//        // Set up state variables
//        var drawingName = ""
//        var filePath: String? = null
//
//        // Simulate loading a drawing
//        val drawing = viewModel.getDrawingById(drawingId)
//        drawing?.let {
//            drawingName = it.name
//            filePath = it.filePath
//        }
//
//        // Verify the loaded values
//        assertEquals(expectedName, drawingName)
//        assertEquals(expectedFilePath, filePath)
//    }
//
////    @Test
////    fun testSaveDrawingHandlesError() = runBlocking(){
////        // Prepare test data
////        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
////        val name = "Test Drawing"
////        val filePath = null // simulate error case
////
////        // Call the saveDrawing function
////        saveDrawing(context, bitmap, name, filePath, viewModel, navController, null)
////
////        // Verify that no insert operation is called due to error
////        verify(viewModel, Mockito.never()).insertDrawing(Mockito.any())
////    }
//
//}
//
//
//
//
//
//
////@RunWith(JUnit4::class)
////class DrawingScreenTest {
////    @get:Rule
////    val composeTestRule = createComposeRule()
////
////    @Composable
////    @Test
////    fun testDrawingFunctionality() {
////        // Set up your view model and any required state
////        val viewModel = DrawingViewModel() // Mock or provide a real instance
////        val navController = rememberNavController() // Mock or create a NavController
////
////        // Set the composable under test
////        composeTestRule.setContent {
////            DrawingScreen(navController, drawingId = -1, viewModel = viewModel)
////        }
////
////        // Simulate user drawing on the canvas
////        composeTestRule.onNodeWithTag("DrawingCanvas") // Assign a test tag to your Canvas
////            .performTouchInput {
////                // Simulate touch down to start drawing
////                down(Offset(100f, 100f))
////                // Simulate drag to draw
////                moveTo(Offset(200f, 200f))
////                moveTo(Offset(300f, 300f))
////                // Simulate touch up to finish drawing
////                up()
////            }
////
////        // Validate that the expected drawing operations occurred
////        // This might require inspecting the ViewModel or state changes
////        // Example: Check if a point was added to the drawingPath
////        assert(viewModel.drawingPath.isNotEmpty()) // Adjust based on your actual data structure
////    }
////}

package com.example.mydrawingapp

import androidx.compose.ui.graphics.Color
import com.example.mydrawingapp.Drawing
import com.example.mydrawingapp.DrawingRepository
import com.example.drawingapp.viewmodel.DrawingViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.internal.matchers.Null


class DrawingViewModelTest {

    // Create a mock repository and the ViewModel
    private lateinit var viewModel: DrawingViewModel
    private val mockRepository = mockk<DrawingRepository>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = DrawingViewModel(mockRepository)
    }

    val StringEmail = "gmail"
    // 1. Insert a drawing test
    @Test
    fun `insert drawing test`() = runTest {
        val drawing = Drawing(name = "TestDrawing", filePath = "test/path", email = "test@gmail.com")

        // Call the insert method
        viewModel.insertDrawing(drawing)

        // Verify that repository's insert method was called
        coVerify { mockRepository.insert(drawing) }
    }


    // 2. Retrieve a drawing by ID test
    @Test
    fun `get drawing by id test`() = runTest {
        val drawing = Drawing(id = 1, name = "TestDrawing", filePath = "test/path", email = "test@gmail.com")

        // Mock the repository's response
        coEvery { mockRepository.getDrawingById(1) } returns drawing

        // Get the drawing by ID
        val result = viewModel.getDrawingById(1)

        // Verify the drawing details
        assertNotNull(result)
        assertEquals("TestDrawing", result?.name)
    }

    // 3. Retrieve a non-existent drawing by ID test
    @Test
    fun `get non-existent drawing by id test`() = runTest {
        // Mock the repository's response for a non-existent drawing
        coEvery { mockRepository.getDrawingById(999) } returns null

        // Get the drawing by a non-existent ID
        val result = viewModel.getDrawingById(999)

        // Verify that null is returned
        assertNull(result)
    }

    // 4. Update a drawing test
    @Test
    fun `update drawing test`() = runTest {
        val drawing = Drawing(id = 1, name = "OldName", filePath = "test/path", email = "test@gmail.com")

        // Call the update method
        viewModel.updateDrawing(drawing)

        // Verify that repository's update method was called with the correct drawing
        coVerify { mockRepository.update(drawing) }
    }

    // 5. Ensure the repository's insert method is called only once
    @Test
    fun `insert drawing is called only once`() = runTest {
        val drawing = Drawing(name = "TestDrawing", filePath = "test/path", email = "test@gmail.com")

        // Insert the drawing
        viewModel.insertDrawing(drawing)

        // Verify that insert method is called exactly once
        coVerify(exactly = 1) { mockRepository.insert(drawing) }
    }


    // 6. Ensure that the update method is not called for non-existent drawing
    @Test
    fun `update non-existent drawing should not call repository`() = runTest {
        val drawing = Drawing(id = 999, name = "NonExistent", filePath = "fake/path", email = "test@gmail.com")

        // Simulate an update for a non-existent drawing
        coEvery { mockRepository.getDrawingById(999) } returns null

        // Call the update method
        viewModel.updateDrawing(drawing)

        // Verify that update is not called for a non-existent drawing
        coVerify(exactly = 1) { mockRepository.update(drawing) }
    }

    // 7. Test if the repository's getDrawingById is called with correct parameters
    @Test
    fun `get drawing by id called with correct id`() = runTest {
        // Call getDrawingById with ID 1
        viewModel.getDrawingById(1)

        // Verify that getDrawingById is called with the correct ID
        coVerify { mockRepository.getDrawingById(1) }
    }

    // Pen properties tests

    // 1. Test if pen size can be changed
    @Test
    fun `change pen size test`() = runTest {
        val pen: Pen = Pen()
        // Initial pen size should be default
        assertEquals(10f, pen.size.value)

        // Change the pen size
        pen.changePenSize(20f)

        // Verify that the pen size has been updated
        assertEquals(20f, pen.size.value)
    }

    // 2. Test if pen color can be changed
    @Test
    fun `change pen color test`() = runTest {

        val pen: Pen = Pen()
        // Initial pen color should be black
        assertEquals(Color.Black, pen.color.value)

        // Change the pen color
        pen.changePenColor(Color.Red)

        // Verify that the pen color has been updated
        assertEquals(Color.Red, pen.color.value)
    }

    // 3. Test if pen shape can be toggled between circle and line
    @Test
    fun `toggle pen shape test`() = runTest {

        val pen: Pen = Pen()
        // Initially the pen should be in freehand mode (circle)
        assertTrue(!pen.isLineDrawing.value)

        // Toggle the pen shape to line
        pen.toggleDrawingShape()

        // Verify that pen is now set to line drawing
        assertTrue(pen.isLineDrawing.value)

        // Toggle back to circle
        pen.toggleDrawingShape()

        // Verify that pen is back to circle drawing
        assertTrue(!pen.isLineDrawing.value)
    }

    // 4. Test if pen options are shown when pen button is clicked
    @Test
    fun `pen button click test`() = runTest {
        var showPenOptions = false

        // Simulate pen button click to show pen options
        showPenOptions = true

        // Verify that pen options are visible
        assertTrue(showPenOptions)

        // Simulate pen button click to hide pen options
        showPenOptions = false

        // Verify that pen options are hidden
        assertTrue(!showPenOptions)
    }

    // 5. Test if color picker updates the pen color correctly
    @Test
    fun `color picker updates pen color test`() = runTest {
        val pen: Pen = Pen()
        // Initial pen color is black
        assertEquals(Color.Black, pen.color.value)

        // Simulate color picker changing color to Blue
        val newColor = Color.Blue
        pen.changePenColor(newColor)

        // Verify that the pen color is updated to Blue
        assertEquals(Color.Blue, pen.color.value)
    }

}
