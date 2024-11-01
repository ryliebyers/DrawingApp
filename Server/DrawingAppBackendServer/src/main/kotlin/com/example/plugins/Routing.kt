package com.example.plugins
import java.io.File

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.Database
import java.util.*
import io.ktor.server.websocket.WebSockets

fun Application.configureRouting() {
    install(WebSockets)

    val database = Database.connect(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    )
    val imageService = ImageService(database)

    routing {
        // Static route to serve image files from the "uploads" folder
        static("/uploads") {
            files("uploads") // Make sure the "uploads" folder exists in your project root
        }

        post("/images/upload") {
            val multipart = call.receiveMultipart()
            var imageName: String? = null
            var userId: String? = null
            var filePath: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "userId") userId = part.value
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName ?: "unknown.png"
                        val file = File("uploads/${UUID.randomUUID()}_$fileName")
                        file.writeBytes(part.streamProvider().readBytes())
                        imageName = file.name
                        filePath = file.path // Capture the path of the uploaded file
                    }
                    else -> Unit
                }
                part.dispose()
            }

            if (imageName != null && userId != null && filePath != null) {
                val id = imageService.saveImage(imageName, filePath, userId) // Pass name, path, and userId
                call.respond(HttpStatusCode.Created, mapOf("imageId" to id))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing image, path, or user ID")
            }
        }


        static("/static") {
            resources("static")
        }

        get("/images") {
            call.respondRedirect("/static/images.html")
        }

        get("/api/images") {
            val images = imageService.getAllImagesWithUserIds()
            val imageList = images.map {
                mapOf(
                    "userId" to it.userId,
                    "imageUrl" to "http://localhost:8081/${it.path}",
                    "name" to it.name
                )
            }
            call.respond(imageList)
        }


        // API to fetch all images with their user IDs and full URLs
        get("/images/all") {
            val images = imageService.getAllImagesWithUserIds()
            call.respond(HttpStatusCode.OK, images)
        }
    }
}