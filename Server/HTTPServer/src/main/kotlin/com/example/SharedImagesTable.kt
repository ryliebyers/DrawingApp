package com.example
import org.jetbrains.exposed.dao.id.IntIdTable

object SharedImagesTable : IntIdTable() {
    val userId = varchar("userId", 50)
    val imageUrl = text("imageUrl")
}