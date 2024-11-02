package com.example.mydrawingapp

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DrawingRepository(private val drawingDao: DrawingDao) {

    val allDrawings: Flow<List<Drawing>> = drawingDao.getAllDrawings()

    suspend fun insert(drawing: Drawing) {
        drawingDao.insert(drawing)
    }

    suspend fun update(drawing: Drawing) {
        drawingDao.update(drawing)
    }

    suspend fun getDrawingById(id: Int): Drawing? {
        return drawingDao.getDrawingById(id)
    }


//    suspend fun getDrawingsByEmail(email: String): Flow<List<Drawing>> {
//        return drawingDao.getDrawingsByEmail(email)
//    }
}






