package com.homato

import com.homato.di.configureKoin
import com.homato.plugins.*
import io.ktor.server.application.*


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureKoin()
    configureSecurity()
    configureRouting()
    configureTemplating()
    configureSerialization()
    configureMonitoring()
}
