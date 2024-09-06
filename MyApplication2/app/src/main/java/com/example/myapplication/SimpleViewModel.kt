package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class SimpleViewModel :ViewModel() {

    //Model
    @RequiresApi(Build.VERSION_CODES.O)
    private val _color : MutableLiveData<Color> = MutableLiveData(Color.valueOf(0f, 0f, 0f))

    @RequiresApi(Build.VERSION_CODES.O)
    val color  = _color as LiveData<Color>
    // create and pass bitmap as member variable
    val _bitmap: MutableLiveData<Bitmap> = MutableLiveData(Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888))
    val bitmap = _bitmap as LiveData<Bitmap>
    @RequiresApi(Build.VERSION_CODES.O)
    fun pickColor(){
        with(Random.Default) {
            _color.value = Color.valueOf(nextFloat(), nextFloat(), nextFloat())
        }
    }
}

