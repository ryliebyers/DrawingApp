package com.example

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select


fun Application.configureRouting() {
    routing {
        post("/upload") {
            val userId = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asString()
            if (userId == null) {
                call.respondText("User not authenticated", status = HttpStatusCode.Unauthorized, contentType = ContentType.Text.Plain)
                return@post
            }

            val multipart = call.receiveMultipart()
            var imageUrl: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    // Save the file and get the URL
                    imageUrl = saveFile(part) // Implement this function to save the file and return its URL
                }
                part.dispose()
            }

            if (imageUrl != null && userId != null) {
                transaction(DBSettings.db) {
                    SharedImagesTable.insert {
                        it[SharedImagesTable.userId] = userId
                        it[SharedImagesTable.imageUrl] = imageUrl!!
                    }
                }
                call.respond(HttpStatusCode.Created, imageUrl!!)
            } else {
                call.respond(HttpStatusCode.BadRequest, "User ID or Image URL cannot be null.")
            }

        }

        get("/images") {
            val userId = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asString()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val images = transaction(DBSettings.db) {
                SharedImagesTable.select { SharedImagesTable.userId eq userId }
                    .map { it[SharedImagesTable.imageUrl] }
            }
            call.respond(images)
        }

        delete("/images/{id}") {
            val userId = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asString()
            val id = call.parameters["id"]?.toIntOrNull()

            if (userId == null || id == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            transaction(DBSettings.db) {
                SharedImagesTable.deleteWhere {
                    (SharedImagesTable.id eq id) and (SharedImagesTable.userId eq userId)
                }
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}


