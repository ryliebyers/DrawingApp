package com.example.mydrawingapp



import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUploader {
    private const val BASE_URL = "http://10.0.2.2:8081/images"
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    suspend fun uploadImage(
        context: Context,
        bitmap: Bitmap,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val client = OkHttpClient()
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "uploaded_image.png", byteArray.toRequestBody())
                .addFormDataPart("userId", currentUserEmail)
                .build()

            val request = Request.Builder()
                .url("$BASE_URL?userId=$currentUserEmail")
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    onSuccess("Image uploaded successfully!")
                } else {
                    onError("Upload failed: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("ImageUploader", "Upload failed: ${e.message}")
            withContext(Dispatchers.Main) {
                onError("Upload failed: ${e.message}")
            }
        }
    }
}
