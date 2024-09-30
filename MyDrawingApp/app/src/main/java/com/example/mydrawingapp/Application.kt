package com.example.mydrawingapp
import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApplication : Application() {
    // Create a coroutine scope tied to the app's lifecycle
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Lazy initialization of the database and repository
    val database by lazy { DrawingDatabase.getDatabase(this) }
    val repository by lazy { DrawingRepository(database.drawingDao()) }
}
