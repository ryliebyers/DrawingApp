import com.example.ExposedUser
import com.example.plugins.ImageService
import com.example.plugins.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    val userService = UserService(database)
    val imageService = ImageService(database)

    routing {
        // Create user
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }

        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        // Upload image with unique name and save with user ID
        post("/images") {
            try {
                val userId = call.request.queryParameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")

                call.application.log.debug("Received request to upload image for userId: $userId")

                val multipart = call.receiveMultipart()

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        // Generate a unique file name using UUID
                        val uniqueFileName = "${UUID.randomUUID()}_${part.originalFileName ?: "uploaded_image.png"}"
                        call.application.log.debug("Uploading file: $uniqueFileName for user: $userId")

                        // Using ByteReadChannel for reading the file data
                        val fileBytes: ByteArray = withContext(Dispatchers.IO) {
                            part.provider().readRemaining().readBytes()
                        }

                        call.application.log.debug("Read ${fileBytes.size} bytes for file: $uniqueFileName")

                        // Ensure the upload directory exists
                        val uploadDir = File("uploads")
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs()
                            call.application.log.debug("Created upload directory at: ${uploadDir.absolutePath}")
                        }

                        // Define the file path and save the file
                        val file = File(uploadDir, uniqueFileName)
                        file.writeBytes(fileBytes)

                        call.application.log.debug("Saved file to path: ${file.absolutePath}")

                        // Save image details to the database with unique name
                        imageService.saveImage(uniqueFileName, file.path, userId)
                        call.application.log.debug("Saved image details to the database for file: $uniqueFileName")
                    }
                    part.dispose()
                }

                call.respond(HttpStatusCode.OK, "Image uploaded successfully.")
                call.application.log.debug("Successfully uploaded image for userId: $userId")
            } catch (e: Exception) {
                call.application.log.error("Failed to upload image for userId: ${call.request.queryParameters["userId"]}", e)
                call.respond(HttpStatusCode.InternalServerError, "Image upload failed: ${e.message}")
            }
        }

        // Serve images for HTML
        get("/images/all") {
            val images = imageService.getAllImagesWithUserIds()
            call.respond(images)
        }

        // Serve individual image file
        get("/uploads/{fileName}") {
            val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val file = File("uploads/$fileName")
            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
