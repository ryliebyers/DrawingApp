package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.SharedImagesTable




object DBSettings {
    //"most recently created" DB will get used by all transactions automatically
    // this is an in-memory DB, but we could connect to mysql or something else
    val db by lazy { Database.connect("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")}

    fun init() {
        transaction(db) {
            SchemaUtils.create(SharedImagesTable)
        }
    }
}


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureRouting()
}
