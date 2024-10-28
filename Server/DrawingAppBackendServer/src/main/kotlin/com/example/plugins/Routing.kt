package com.example.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.util.UUID

fun Application.configureRouting() {
    install(Resources)
    val imageService = ImageService(Database.connect(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    ))

    routing {
        // Upload image
        post("/images/upload") {
            val multipart = call.receiveMultipart()
            var imageName: String? = null
            var userId: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "userId") userId = part.value
                    }
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName ?: "unknown.png"
                        val fileBytes = part.streamProvider().readBytes()
                        val file = File("uploads/${UUID.randomUUID()}_$fileName")
                        file.writeBytes(fileBytes)
                        imageName = file.name
                    }
                    else -> Unit
                }
                part.dispose()
            }

            if (imageName != null && userId != null) {
                val id = imageService.saveImage(imageName!!, "uploads/$imageName", userId!!)
                call.respond(HttpStatusCode.Created, mapOf("imageId" to id))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing image or user ID")
            }
        }

        // Download image
        get("/images/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val imageInfo = imageService.getImage(id)
            if (imageInfo != null) {
                val file = File(imageInfo.path)
                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Image not found")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Image metadata not found")
            }
        }

        // Get all images by user
        get("/images/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid User ID")
            val images = imageService.getImagesByUser(userId)
            call.respond(images)
        }

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
