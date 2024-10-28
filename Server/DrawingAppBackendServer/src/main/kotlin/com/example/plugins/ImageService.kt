package com.example.plugins

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Image

@Serializable
data class ImageInfo(val id: Int, val name: String, val path: String, val userId: String)

object Images : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val path = varchar("path", 255)
    val userId = varchar("userId", 50)

    override val primaryKey = PrimaryKey(id)
}

class ImageService(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Images)
        }
    }

    suspend fun saveImage(name: String, path: String, userId: String): Int = newSuspendedTransaction {
        Images.insert {
            it[Images.name] = name
            it[Images.path] = path
            it[Images.userId] = userId
        } get Images.id
    }

    suspend fun getImage(id: Int): ImageInfo? = newSuspendedTransaction {
        Images.select { Images.id eq id }
            .map { ImageInfo(it[Images.id], it[Images.name], it[Images.path], it[Images.userId]) }
            .singleOrNull()
    }

    suspend fun getImagesByUser(userId: String): List<ImageInfo> = newSuspendedTransaction {
        Images.select { Images.userId eq userId }
            .map { ImageInfo(it[Images.id], it[Images.name], it[Images.path], it[Images.userId]) }
    }
    fun getImagesByUserId(userId: String): List<Image> = transaction {
        Images.select { Images.userId eq userId }
            .map { Image(it[Images.id], it[Images.name], it[Images.path], it[Images.userId]) }
    }

    data class Image(
        val id: Int,
        val name: String,
        val path: String,
        val userId: String
    )
}
