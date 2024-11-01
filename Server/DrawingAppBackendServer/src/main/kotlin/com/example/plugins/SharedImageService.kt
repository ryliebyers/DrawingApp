//package com.example.plugins
//
//import kotlinx.serialization.Serializable
//import org.jetbrains.exposed.sql.*
//import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
//import org.jetbrains.exposed.sql.transactions.transaction
//
//@Serializable
//data class SharedImageInfo(val id: Int, val imageId: Int, val ownerId: String, val sharedWithId: String)
//
//object SharedImages : Table() {
//    val id = integer("id").autoIncrement()
//    val imageId = integer("imageId").references(Images.id)
//    val ownerId = varchar("ownerId", 50)
//    val sharedWithId = varchar("sharedWithId", 50)
//
//    override val primaryKey = PrimaryKey(id)
//}
//
//class SharedImageService(private val database: Database) {
//    init {
//        transaction(database) {
//            SchemaUtils.create(SharedImages)
//        }
//    }
//
//    suspend fun shareImage(imageId: Int, ownerId: String, sharedWithId: String): Int = newSuspendedTransaction {
//        SharedImages.insert {
//            it[SharedImages.imageId] = imageId
//            it[SharedImages.ownerId] = ownerId
//            it[SharedImages.sharedWithId] = sharedWithId
//        } get SharedImages.id
//    }
//
////    suspend fun getSharedImagesForUser(userId: String): List<ImageInfo> = newSuspendedTransaction {
////        (Images innerJoin SharedImages)
////            .select { SharedImages.sharedWithId eq userId }
////            .map {
////                ImageInfo(
////                    id = it[Images.id],
////                    name = it[Images.name],
////                    path = it[Images.path],
////                    userId = it[Images.userId]
////                )
////            }
////    }
//}
