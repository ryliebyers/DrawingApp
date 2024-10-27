package com.example

import org.jetbrains.exposed.dao.id.IntIdTable

data class SharedImage(
    val id: Int? = null,
    val userId: String,
    val imageUrl: String
)

object SharedImagesTable : IntIdTable() {
    val userId = varchar("userId", 50)
    val imageUrl = text("imageUrl")
}