package com.example.mydrawingapp

import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DrawingRepository(private val drawingDao: DrawingDao) {

    val allDrawings: Flow<List<Drawing>> = drawingDao.getAllDrawings()

    fun insert(drawing: Drawing) {
        drawingDao.insert(drawing)
    }

    fun update(drawing: Drawing) {
        drawingDao.update(drawing)
    }

    fun getDrawingById(id: Int): Drawing? {
        return drawingDao.getDrawingById(id)
    }

//    fun getDrawingsByEmail(currentUserEmail: String): Flow<List<Drawing>> {
//        return drawingDao.getDrawingsByEmail(currentUserEmail)
//    }


}






