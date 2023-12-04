package com.homato.plugins

import com.homato.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        login()
        register()
        authenticate()
        secretInfo()
        submitSpotRoute()
        unknownSpots()
        visitedSpots()
        submittedSpots()
        changeUsername()
    }
}
