import android.content.Context
import com.example.myapplication.SimpleViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock


@RunWith(JUnit4::class)
class MethodUnitTesting {
    private lateinit var viewModel: SimpleViewModel

    @Before
    fun setUp() {
        viewModel = SimpleViewModel()
    }

    @Test
    fun testGetDrawingByName_NotFound() {
        val context = mock(Context::class.java)
        assertNull("Expect null if drawing name does not exist", viewModel.getDrawingByName(context, "nonexistent"))
    }

    @Test
    fun testRemoveDrawingByName() {
        val name = "testDrawing"
        viewModel.removeDrawingByName(name)
        assertNull("Drawing should be removed from records", viewModel.getDrawingByName(mock(Context::class.java), name))
    }

    @Test
    fun testCurrentDrawingName() {
        assertNull("Drawing name should be null initially", viewModel.currentDrawingName)
        viewModel.currentDrawingName = "testDrawing"
        assertEquals("Drawing name should be updated correctly", "testDrawing", viewModel.currentDrawingName)
    }

    @Test
    fun testIsDrawingSavedFlag() {
        assertFalse("Drawing should not be marked as saved initially", viewModel.isDrawingSaved)
        viewModel.isDrawingSaved = true
        assertTrue("Drawing should be marked as saved after setting the flag", viewModel.isDrawingSaved)
    }

    @Test
    fun testRemoveNonExistentDrawing() {
        val name = "nonExistentDrawing"
        viewModel.removeDrawingByName(name)
        assertNull("No drawing should be found after trying to remove a non-existent drawing", viewModel.getDrawingByName(mock(Context::class.java), name))
    }
}



