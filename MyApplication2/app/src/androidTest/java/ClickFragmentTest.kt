package com.example.myapplication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ClickFragmentTest {

    @Before
    fun setUp() {
        // Launch the MainActivity which hosts the ClickFragment
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testCreateNewDrawingButtonClick() {
        // Check if the "Create New Drawing" button is displayed
        onView(withId(R.id.createNewDrawing))
            .check(matches(isDisplayed()))

        // Perform a click on the "Create New Drawing" button
        onView(withId(R.id.createNewDrawing))
            .perform(click())

        // Check if the navigation to DrawFragment happened
        onView(withId(R.id.btnSaveDrawing))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSaveDrawingButtonClick() {
        // Navigate to DrawFragment first by clicking "Create New Drawing"
        onView(withId(R.id.createNewDrawing))
            .perform(click())

        // Check if the "Save Drawing" button is displayed in the DrawFragment
        onView(withId(R.id.btnSaveDrawing))
            .check(matches(isDisplayed()))

        // Perform a click on the "Save Drawing" button
        onView(withId(R.id.btnSaveDrawing))
            .perform(click())

    }


    @Test
    fun testRecentDrawingsDisplayed() {
        // Check if recent drawings are being displayed
        onView(withId(R.id.recentDrawing1Name))
            .check(matches(withText("No Drawing")))

    }
}
