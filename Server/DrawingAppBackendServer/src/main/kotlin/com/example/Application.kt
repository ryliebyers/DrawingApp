package com.example

import com.example.plugins.*
import configureDatabases
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main() {
    val imageUploadDir = "uploads" // Directory to save images
    File(imageUploadDir).mkdir() // Create the directory if it doesn't exist
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureRouting()
    configureMonitoring()
}

