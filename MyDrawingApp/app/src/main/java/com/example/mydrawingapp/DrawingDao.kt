package com.example.mydrawingapp

import androidx.room.*
import com.example.mydrawingapp.Drawing
import kotlinx.coroutines.flow.Flow

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface DrawingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drawing: Drawing)

    @Update
    fun update(drawing: Drawing)

    @Query("SELECT * FROM drawings ORDER BY id DESC")
    fun getAllDrawings(): Flow<List<Drawing>>

    @Query("SELECT * FROM drawings WHERE id = :id")
    fun getDrawingById(id: Int): Drawing?


//    @Query("SELECT * FROM drawings WHERE email = :email ORDER BY id DESC")
//    fun getDrawingsByEmail(email: String): Flow<List<Drawing>>

}





