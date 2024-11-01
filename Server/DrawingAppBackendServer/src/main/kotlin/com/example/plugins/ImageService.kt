package com.example.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

// Data model
@Serializable
data class ImageInfo(val id: Int, val name: String, val path: String, val userId: String)

object Images : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val path = varchar("path", 255)
    val userId = varchar("userId", 50)
    override val primaryKey = PrimaryKey(id)
}

// Service for handling images
class ImageService(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Images)
        }
    }

    // Updated saveImage function with name, path, and userId parameters
    suspend fun saveImage(name: String, path: String, userId: String): Int = newSuspendedTransaction {
        Images.insert {
            it[Images.name] = name
            it[Images.path] = path // Use the path column here
            it[Images.userId] = userId
        } get Images.id
    }

    suspend fun getAllImagesWithUserIds(): List<ImageInfo> = newSuspendedTransaction {
        Images.selectAll().map {
            ImageInfo(
                id = it[Images.id],
                name = it[Images.name],
                path = it[Images.path],
                userId = it[Images.userId]
            )
        }
    }
}

