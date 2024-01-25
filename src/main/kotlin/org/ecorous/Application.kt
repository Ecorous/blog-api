package org.ecorous

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.doublereceive.*
import org.ecorous.plugins.DB
import org.ecorous.plugins.configureHTTP
import org.ecorous.plugins.configureRouting
import org.ecorous.plugins.configureSerialization
import kotlin.system.exitProcess

val serverPort = System.getenv("BLOG_PORT")?.toInt() ?: 5662
val serverHost = System.getenv("BLOG_HOST") ?: "0.0.0.0"
val authToken = System.getenv("BLOG_AUTH_TOKEN") ?: throw Exception("BLOG_AUTH_TOKEN not set")

fun main() {
    DB.setupDatabase() // we do this before even touching the server; if there's no database, no point in starting the server
    if (DB.db != null) {
        println("Database connection established.")
    } else {
        println("Database connection failed. Exiting as there's no point in starting the server.")
        exitProcess(-112)
    }
    embeddedServer(Netty, port = serverPort, host = serverHost, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(DoubleReceive)
    configureSerialization()
    configureHTTP()
    configureRouting()
}
